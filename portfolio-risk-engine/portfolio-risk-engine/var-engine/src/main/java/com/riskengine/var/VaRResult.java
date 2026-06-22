package com.riskengine.var;

/**
 * Result of a VaR calculation: the methodology used, the confidence level, and the
 * resulting VaR figure (expressed as a positive loss amount in the portfolio's base currency).
 */
public class VaRResult {

    private final String methodology;
    private final double confidence;
    private final double varAmount;

    public VaRResult(String methodology, double confidence, double varAmount) {
        this.methodology = methodology;
        this.confidence = confidence;
        this.varAmount = varAmount;
    }

    public String getMethodology() {
        return methodology;
    }

    public double getConfidence() {
        return confidence;
    }

    public double getVarAmount() {
        return varAmount;
    }

    @Override
    public String toString() {
        return methodology + " VaR @ " + (int) (confidence * 100) + "%: " + varAmount;
    }
}
