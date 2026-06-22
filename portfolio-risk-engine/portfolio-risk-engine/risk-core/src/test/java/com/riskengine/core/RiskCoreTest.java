package com.riskengine.core;

import com.riskengine.core.marketdata.MarketDataCache;
import com.riskengine.core.marketdata.MarketData;
import com.riskengine.core.math.CorrelationMatrix;
import com.riskengine.core.model.AssetClass;
import com.riskengine.core.model.OptionType;
import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.core.pricing.BlackScholesModel;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RiskCoreTest {

    @Test
    void portfolioTotalValueSumsPositions() {
        Portfolio portfolio = new Portfolio();
        portfolio.addPosition(new Position("AAPL", AssetClass.EQUITY, 100, 200.0));
        portfolio.addPosition(new Position("MSFT", AssetClass.EQUITY, 50, 400.0));

        assertEquals(100 * 200.0 + 50 * 400.0, portfolio.getTotalValue(), 1e-9);
    }

    @Test
    void withoutPositionExcludesGivenPosition() {
        Position aapl = new Position("AAPL", AssetClass.EQUITY, 100, 200.0);
        Position msft = new Position("MSFT", AssetClass.EQUITY, 50, 400.0);
        Portfolio portfolio = new Portfolio("test", List.of(aapl, msft));

        Portfolio reduced = portfolio.withoutPosition(aapl);

        assertEquals(1, reduced.size());
        assertEquals(50 * 400.0, reduced.getTotalValue(), 1e-9);
    }

    @Test
    void marketDataCacheStoresAndRetrieves() {
        MarketDataCache cache = new MarketDataCache();
        cache.put(new MarketData("AAPL", 200.0, 0.25, 0.04));

        assertTrue(cache.contains("AAPL"));
        assertEquals(200.0, cache.getOrThrow("AAPL").getPrice(), 1e-9);
    }

    @Test
    void blackScholesCallPriceIsPositiveAndDeltaBetweenZeroAndOne() {
        BlackScholesModel model = new BlackScholesModel();
        double price = model.price(OptionType.CALL, 100, 100, 1.0, 0.03, 0.20);
        double delta = model.delta(OptionType.CALL, 100, 100, 1.0, 0.03, 0.20);

        assertTrue(price > 0);
        assertTrue(delta > 0 && delta < 1);
    }

    @Test
    void choleskyDecompositionReproducesCorrelationStructure() {
        List<String> symbols = List.of("SPX", "NDX");
        double[][] corr = {
                {1.0, 0.8},
                {0.8, 1.0}
        };
        CorrelationMatrix matrix = new CorrelationMatrix(symbols, corr);

        double[] independent = {1.0, 0.0};
        double[] correlated = matrix.correlate(independent);

        // L * L^T should reconstruct the original correlation matrix
        var l = matrix.getCholeskyLower();
        var reconstructed = l.multiply(l.transpose());
        assertEquals(corr[0][1], reconstructed.getEntry(0, 1), 1e-9);
        assertEquals(2, correlated.length);
    }
}
