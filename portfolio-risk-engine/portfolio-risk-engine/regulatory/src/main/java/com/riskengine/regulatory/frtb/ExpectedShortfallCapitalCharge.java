package com.riskengine.regulatory.frtb;

/**
 * Simplified FRTB Expected Shortfall capital charge calculation, Internal Models Approach.
 * "Simplified" in the sense that it does not implement the full liquidity-horizon-bucketed
 * cross-correlation aggregation across all five FRTB horizons — that requires modeling
 * cross-bucket risk factor correlations across each horizon bucket, which is a substantial
 * undertaking beyond this engine's scope. This class implements the core regulatory formula
 * structure: a base (stress-period-calibrated) ES scaled by liquidity horizon, with the
 * standard FRTB blend between current and stressed ES.
 *
 * <p>Core formula (BCBS d457 paragraph 33.4, simplified single-horizon form):</p>
 * <pre>
 *   IMCC = ES_reduced_set * (ES_full_stress / ES_full_current)
 * </pre>
 * <p>Here we expose the building blocks (liquidity-horizon scaling, and the stress multiplier)
 * so a desk can compose the full multi-horizon formula once cross-horizon correlations are
 * available; for a single dominant liquidity horizon, {@link #capitalCharge} gives the
 * regulatory capital number directly.</p>
 */
public class ExpectedShortfallCapitalCharge {

    /**
     * Scales a 10-day-base Expected Shortfall figure up to the prescribed liquidity horizon
     * for the given risk class using square-root-of-time scaling.
     */
    public double scaleToLiquidityHorizon(double tenDayBaseES, LiquidityHorizon horizon) {
        return tenDayBaseES * horizon.scalingFactorFromTenDayBase();
    }

    /**
     * Computes the FRTB-style stressed capital charge: the current-period ES scaled by the
     * ratio of stressed-period ES to current-period ES (the regulatory "stress multiplier"),
     * then scaled to the risk class's liquidity horizon.
     *
     * @param currentPeriodES   ES computed on the most recent 12-month observation window
     * @param stressedPeriodES  ES computed on the worst 12-month stress window in the
     *                          observation history (as required by FRTB)
     * @param horizon           liquidity horizon for the dominant risk class in the portfolio
     */
    public double capitalCharge(double currentPeriodES, double stressedPeriodES, LiquidityHorizon horizon) {
        if (currentPeriodES <= 0) {
            throw new IllegalArgumentException("currentPeriodES must be positive");
        }
        double stressMultiplier = stressedPeriodES / currentPeriodES;
        double stressedCapital = currentPeriodES * stressMultiplier;
        return scaleToLiquidityHorizon(stressedCapital, horizon);
    }
}
