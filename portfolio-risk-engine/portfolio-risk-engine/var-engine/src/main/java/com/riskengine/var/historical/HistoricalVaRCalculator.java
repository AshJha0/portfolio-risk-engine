package com.riskengine.var.historical;

import com.riskengine.var.VaRResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Historical VaR: the most widely used approach in practice.
 *
 * <p>Given a series of historical (or simulated historical) portfolio P&amp;L observations,
 * VaR at confidence level c is the loss at the (1-c) percentile of the empirical distribution.
 * E.g. at 95% confidence, VaR is the loss such that only 5% of historical days were worse.</p>
 *
 * <p>Convention: pnlSeries values are signed (negative = loss, positive = gain). The returned
 * VaR is reported as a positive number representing the magnitude of loss.</p>
 */
public class HistoricalVaRCalculator {

    /**
     * @param pnlSeries  historical P&amp;L observations (signed; will not be mutated)
     * @param confidence e.g. 0.95 or 0.99
     * @return VaR as a positive loss amount
     */
    public double calculate(List<Double> pnlSeries, double confidence) {
        if (pnlSeries == null || pnlSeries.isEmpty()) {
            throw new IllegalArgumentException("pnlSeries must not be empty");
        }
        if (confidence <= 0 || confidence >= 1) {
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        }

        List<Double> sorted = new ArrayList<>(pnlSeries);
        Collections.sort(sorted); // ascending: worst losses first

        int index = (int) Math.floor((1 - confidence) * sorted.size());
        index = Math.max(0, Math.min(index, sorted.size() - 1));

        return Math.abs(sorted.get(index));
    }

    /** Convenience overload returning a structured {@link VaRResult}. */
    public VaRResult calculateResult(List<Double> pnlSeries, double confidence) {
        return new VaRResult("Historical", confidence, calculate(pnlSeries, confidence));
    }

    /**
     * Converts a series of portfolio returns (as fractions, e.g. -0.05 for -5%) into a
     * P&amp;L series scaled by the current portfolio value, then computes Historical VaR.
     * This mirrors the worked example in the spec (returns -> 5th percentile -> VaR amount).
     */
    public double calculateFromReturns(List<Double> returns, double confidence, double portfolioValue) {
        List<Double> pnl = new ArrayList<>(returns.size());
        for (double r : returns) {
            pnl.add(r * portfolioValue);
        }
        return calculate(pnl, confidence);
    }
}
