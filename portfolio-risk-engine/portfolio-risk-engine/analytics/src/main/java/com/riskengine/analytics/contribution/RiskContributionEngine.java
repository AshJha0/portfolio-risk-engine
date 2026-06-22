package com.riskengine.analytics.contribution;

import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.var.historical.HistoricalVaRCalculator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Computes how individual positions contribute to total portfolio VaR, answering the
 * management question from the spec: "Which position contributes most to VaR?"
 *
 * <p>Three related but distinct measures are provided:</p>
 * <ul>
 *   <li><b>Component VaR</b> — decomposes total VaR into additive per-position contributions
 *       that sum back to the total (using the standard scaled-marginal-contribution method).</li>
 *   <li><b>Incremental VaR</b> — the exact change in total VaR from fully removing one
 *       position: {@code Portfolio VaR - Portfolio VaR without position}. Not generally
 *       additive across positions due to diversification effects.</li>
 *   <li><b>Marginal VaR</b> — the sensitivity of VaR to a small change in a position's size
 *       (a numerical derivative), useful for "what if I add $1 more of this" questions.</li>
 * </ul>
 *
 * <p>This engine is VaR-methodology-agnostic: callers supply a {@code Function&lt;Portfolio,Double&gt;}
 * that computes total portfolio VaR however they like (Historical, Parametric, or Monte Carlo),
 * and the engine handles the position-level decomposition around it.</p>
 */
public class RiskContributionEngine {

    private final HistoricalVaRCalculator historicalVaRCalculator = new HistoricalVaRCalculator();

    /**
     * Computes Component VaR for each position using historical P&amp;L series per symbol.
     * Each position's contribution is its share of total variance-weighted P&amp;L at the
     * tail, scaled so contributions sum to total portfolio VaR.
     *
     * @param positionPnlSeries map of symbol -> historical P&amp;L series for that position alone
     * @param portfolioPnlSeries the portfolio's combined historical P&amp;L series (sum across positions per date)
     * @param confidence VaR confidence level, e.g. 0.99
     */
    public List<RiskContribution> componentVaR(Map<String, List<Double>> positionPnlSeries,
                                                List<Double> portfolioPnlSeries,
                                                double confidence) {
        double totalVaR = historicalVaRCalculator.calculate(portfolioPnlSeries, confidence);

        // Identify the tail dates: the worst (1-confidence) fraction of portfolio outcomes.
        int n = portfolioPnlSeries.size();
        int cutoff = Math.max(1, (int) Math.floor((1 - confidence) * n));

        List<Integer> sortedIndices = new ArrayList<>();
        for (int i = 0; i < n; i++) sortedIndices.add(i);
        sortedIndices.sort((a, b) -> Double.compare(portfolioPnlSeries.get(a), portfolioPnlSeries.get(b)));
        List<Integer> tailIndices = sortedIndices.subList(0, cutoff);

        // Each position's raw contribution = average of its own P&L on the portfolio's tail days.
        Map<String, Double> rawContribution = new LinkedHashMap<>();
        double rawTotal = 0.0;
        for (Map.Entry<String, List<Double>> entry : positionPnlSeries.entrySet()) {
            double avg = 0.0;
            for (int idx : tailIndices) {
                avg += entry.getValue().get(idx);
            }
            avg /= tailIndices.size();
            rawContribution.put(entry.getKey(), Math.abs(avg));
            rawTotal += Math.abs(avg);
        }

        // Scale so contributions sum exactly to total portfolio VaR.
        List<RiskContribution> result = new ArrayList<>();
        for (Map.Entry<String, Double> entry : rawContribution.entrySet()) {
            double scaled = rawTotal > 0 ? (entry.getValue() / rawTotal) * totalVaR : 0.0;
            double pct = totalVaR > 0 ? (scaled / totalVaR) * 100.0 : 0.0;
            result.add(new RiskContribution(entry.getKey(), scaled, pct));
        }
        return result;
    }

    /**
     * Incremental VaR for a single position: Portfolio VaR - Portfolio VaR without that position.
     *
     * @param fullPortfolio    the complete portfolio
     * @param positionToTest   the position whose incremental contribution is being measured
     * @param varFunction      a function computing total VaR for any given portfolio
     */
    public double incrementalVaR(Portfolio fullPortfolio, Position positionToTest,
                                  Function<Portfolio, Double> varFunction) {
        double fullVaR = varFunction.apply(fullPortfolio);
        double reducedVaR = varFunction.apply(fullPortfolio.withoutPosition(positionToTest));
        return fullVaR - reducedVaR;
    }

    /** Computes Incremental VaR for every position in the portfolio. */
    public Map<String, Double> incrementalVaRAll(Portfolio fullPortfolio,
                                                   Function<Portfolio, Double> varFunction) {
        Map<String, Double> results = new LinkedHashMap<>();
        for (Position p : fullPortfolio.getPositions()) {
            results.put(p.getSymbol(), incrementalVaR(fullPortfolio, p, varFunction));
        }
        return results;
    }

    /**
     * Marginal VaR: the numerical sensitivity of total VaR to a small change in a position's
     * quantity, i.e. d(VaR)/d(quantity), estimated by a symmetric finite difference.
     *
     * @param bumpFraction  fractional size bump to apply, e.g. 0.01 for a 1% change in quantity
     */
    public double marginalVaR(Portfolio portfolio, Position position,
                               Function<Portfolio, Double> varFunction, double bumpFraction) {
        double originalQty = position.getQuantity();
        double bump = Math.abs(originalQty) * bumpFraction;
        if (bump == 0) bump = bumpFraction; // fallback for zero-quantity edge case

        position.setQuantity(originalQty + bump);
        double varUp = varFunction.apply(portfolio);

        position.setQuantity(originalQty - bump);
        double varDown = varFunction.apply(portfolio);

        position.setQuantity(originalQty); // restore

        return (varUp - varDown) / (2 * bump);
    }
}
