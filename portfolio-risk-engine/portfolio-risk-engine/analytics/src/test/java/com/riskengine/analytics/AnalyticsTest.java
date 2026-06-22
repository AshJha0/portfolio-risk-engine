package com.riskengine.analytics;

import com.riskengine.analytics.cvar.ExpectedShortfallCalculator;
import com.riskengine.analytics.greeks.GreeksEngine;
import com.riskengine.core.model.AssetClass;
import com.riskengine.core.model.OptionType;
import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AnalyticsTest {

    @Test
    void expectedShortfallIsAverageOfWorstLosses() {
        ExpectedShortfallCalculator calc = new ExpectedShortfallCalculator();
        // 10 observations, worst 10% (cutoff=1) at 99%... use a clean 100-sample-like set instead
        List<Double> pnl = List.of(-100.0, -80.0, -60.0, -40.0, -20.0, 0.0, 20.0, 40.0, 60.0, 80.0);

        // At 90% confidence, cutoff = floor(0.1*10) = 1 -> average of single worst (-100) = 100
        double es90 = calc.calculate(pnl, 0.90);
        assertEquals(100.0, es90, 1e-6);
    }

    @Test
    void greeksEngineComputesPositiveDeltaForLongCall() {
        Position option = new Position("AAPL_CALL", AssetClass.OPTION, 10, 200.0);
        option.setOptionType(OptionType.CALL);
        option.setStrike(200.0);
        option.setTimeToExpiryYears(0.5);
        option.setImpliedVolatility(0.25);
        option.setRiskFreeRate(0.04);

        GreeksEngine engine = new GreeksEngine();
        engine.computeGreeks(option);

        assertTrue(option.getDelta() > 0 && option.getDelta() < 1);
        assertTrue(option.getGamma() > 0);
        assertTrue(option.getVega() > 0);
    }

    @Test
    void portfolioDollarDeltaScalesWithQuantity() {
        Position option = new Position("AAPL_CALL", AssetClass.OPTION, 10, 200.0);
        option.setOptionType(OptionType.CALL);
        option.setStrike(200.0);
        option.setTimeToExpiryYears(0.5);
        option.setImpliedVolatility(0.25);
        option.setRiskFreeRate(0.04);

        GreeksEngine engine = new GreeksEngine();
        engine.computeGreeks(option);

        Portfolio portfolio = new Portfolio();
        portfolio.addPosition(option);

        double dollarDelta = engine.portfolioDollarDelta(portfolio);
        assertEquals(option.getQuantity() * option.getDelta() * option.getMarketPrice(), dollarDelta, 1e-9);
    }
}
