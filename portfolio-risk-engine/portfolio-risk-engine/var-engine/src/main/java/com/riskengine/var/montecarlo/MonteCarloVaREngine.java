package com.riskengine.var.montecarlo;

import com.riskengine.core.marketdata.MarketData;
import com.riskengine.core.marketdata.MarketDataCache;
import com.riskengine.core.math.CorrelationMatrix;
import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.var.VaRResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Monte Carlo VaR engine.
 *
 * <p>Flow (matching the spec's architecture diagram):</p>
 * <pre>
 *   Generate random returns -> Simulate future prices -> Revalue portfolio
 *   -> Generate P&amp;L distribution -> Take quantile (e.g. 99%)
 * </pre>
 *
 * <p>Two modes are supported:</p>
 * <ul>
 *   <li>{@link #calculate} — single risk-factor / independent-shock simulation per symbol</li>
 *   <li>{@link #calculateCorrelated} — multi-asset simulation using a {@link CorrelationMatrix}
 *       so that, e.g., equity index and FX shocks move together realistically</li>
 * </ul>
 */
public class MonteCarloVaREngine {

    private final MonteCarloSimulator simulator;

    public MonteCarloVaREngine() {
        this.simulator = new MonteCarloSimulator();
    }

    public MonteCarloVaREngine(long seed) {
        this.simulator = new MonteCarloSimulator(seed);
    }

    /**
     * Runs independent-shock Monte Carlo across the whole portfolio: each position's
     * underlying symbol is shocked independently using its own volatility from the
     * market data cache, the portfolio is revalued under each scenario, and VaR is
     * read off the resulting P&amp;L distribution at the given confidence level.
     */
    public List<Double> simulatePnL(Portfolio portfolio, MarketDataCache marketDataCache, int scenarios) {
        List<Double> pnlDistribution = new ArrayList<>(scenarios);
        double baseValue = portfolio.getTotalValue();

        // Pre-fetch shocked price paths per unique symbol so each position revalues consistently
        // scenario-by-scenario rather than independently re-drawing per position.
        Map<String, List<Double>> simulatedPricesBySymbol = new HashMap<>();
        for (Position p : portfolio.getPositions()) {
            simulatedPricesBySymbol.computeIfAbsent(p.getSymbol(), symbol -> {
                MarketData md = marketDataCache.getOrThrow(symbol);
                return simulator.simulate(md.getPrice(), md.getVolatility(), scenarios);
            });
        }

        for (int scenario = 0; scenario < scenarios; scenario++) {
            double scenarioValue = 0.0;
            for (Position p : portfolio.getPositions()) {
                double simulatedPrice = simulatedPricesBySymbol.get(p.getSymbol()).get(scenario);
                scenarioValue += p.getQuantity() * simulatedPrice;
            }
            pnlDistribution.add(scenarioValue - baseValue);
        }

        return pnlDistribution;
    }

    /**
     * Same as {@link #simulatePnL} but shocks are drawn jointly via the Cholesky factor of the
     * supplied correlation matrix, so correlated instruments (e.g. SPX/NDX) move together.
     * Uses the simple multiplicative shock model: price * (1 + correlatedShock).
     */
    public List<Double> simulateCorrelatedPnL(Portfolio portfolio, MarketDataCache marketDataCache,
                                               CorrelationMatrix correlationMatrix, int scenarios) {
        List<String> symbols = correlationMatrix.getSymbols();
        List<Double> pnlDistribution = new ArrayList<>(scenarios);
        double baseValue = portfolio.getTotalValue();

        for (int scenario = 0; scenario < scenarios; scenario++) {
            double[] independent = simulator.nextIndependentNormals(symbols.size());
            Map<String, Double> correlatedShocks = correlationMatrix.correlateToMap(independent);

            double scenarioValue = 0.0;
            for (Position p : portfolio.getPositions()) {
                MarketData md = marketDataCache.getOrThrow(p.getSymbol());
                Double z = correlatedShocks.get(p.getSymbol());
                double shock = (z != null) ? z * md.getVolatility() : 0.0;
                double simulatedPrice = md.getPrice() * (1 + shock);
                scenarioValue += p.getQuantity() * simulatedPrice;
            }
            pnlDistribution.add(scenarioValue - baseValue);
        }

        return pnlDistribution;
    }

    /** Reads VaR off a pre-computed P&amp;L distribution at the given confidence level. */
    public double quantileVaR(List<Double> pnlDistribution, double confidence) {
        List<Double> sorted = new ArrayList<>(pnlDistribution);
        Collections.sort(sorted);
        int index = (int) Math.floor((1 - confidence) * sorted.size());
        index = Math.max(0, Math.min(index, sorted.size() - 1));
        return Math.abs(sorted.get(index));
    }

    public VaRResult calculate(Portfolio portfolio, MarketDataCache marketDataCache,
                                int scenarios, double confidence) {
        List<Double> pnl = simulatePnL(portfolio, marketDataCache, scenarios);
        return new VaRResult("MonteCarlo", confidence, quantileVaR(pnl, confidence));
    }

    public VaRResult calculateCorrelated(Portfolio portfolio, MarketDataCache marketDataCache,
                                          CorrelationMatrix correlationMatrix,
                                          int scenarios, double confidence) {
        List<Double> pnl = simulateCorrelatedPnL(portfolio, marketDataCache, correlationMatrix, scenarios);
        return new VaRResult("MonteCarlo-Correlated", confidence, quantileVaR(pnl, confidence));
    }
}
