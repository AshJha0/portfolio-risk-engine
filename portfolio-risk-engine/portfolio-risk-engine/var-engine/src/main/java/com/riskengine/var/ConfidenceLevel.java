package com.riskengine.var;

/**
 * Standard VaR confidence levels and their corresponding one-tailed normal z-scores,
 * used by {@link com.riskengine.var.parametric.ParametricVaRCalculator}.
 */
public enum ConfidenceLevel {

    NINETY_FIVE(0.95, 1.65),
    NINETY_NINE(0.99, 2.33);

    private final double confidence;
    private final double zScore;

    ConfidenceLevel(double confidence, double zScore) {
        this.confidence = confidence;
        this.zScore = zScore;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getZScore() {
        return zScore;
    }
}
