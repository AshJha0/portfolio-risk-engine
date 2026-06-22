package com.riskengine.analytics.cvar;

import com.riskengine.var.VaRResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Expected Shortfall (CVaR / Conditional VaR): the regulatory standard risk measure under
 * Basel (FRTB), replacing VaR as the primary capital metric.
 *
 * <p>Unlike VaR, which only reports the loss at a given percentile, Expected Shortfall
 * reports the *average* loss given that the VaR threshold has been breached — i.e. the
 * average of the worst (1-confidence) fraction of outcomes. This makes it more sensitive
 * to tail risk / fat tails than VaR alone.</p>
 */
public class ExpectedShortfallCalculator {

    /**
     * @param pnl        historical or simulated P&amp;L series (signed; will not be mutated)
     * @param confidence e.g. 0.99 for the average of the worst 1% of outcomes
     * @return Expected Shortfall as a positive loss amount
     */
    public double calculate(List<Double> pnl, double confidence) {
        if (pnl == null || pnl.isEmpty()) {
            throw new IllegalArgumentException("pnl must not be empty");
        }
        if (confidence <= 0 || confidence >= 1) {
            throw new IllegalArgumentException("confidence must be between 0 and 1");
        }

        List<Double> sorted = new ArrayList<>(pnl);
        Collections.sort(sorted); // ascending: worst losses first

        int cutoff = (int) Math.floor((1 - confidence) * sorted.size());
        cutoff = Math.max(1, cutoff); // always average at least the single worst observation

        double sum = 0;
        for (int i = 0; i < cutoff; i++) {
            sum += sorted.get(i);
        }

        return Math.abs(sum / cutoff);
    }

    public VaRResult calculateResult(List<Double> pnl, double confidence) {
        return new VaRResult("ExpectedShortfall", confidence, calculate(pnl, confidence));
    }
}
