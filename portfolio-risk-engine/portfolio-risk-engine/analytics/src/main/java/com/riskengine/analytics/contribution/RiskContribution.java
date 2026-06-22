package com.riskengine.analytics.contribution;

/**
 * A single position's contribution to total portfolio VaR.
 */
public class RiskContribution {

    private final String symbol;
    private final double componentVaR;
    private final double percentOfTotal;

    public RiskContribution(String symbol, double componentVaR, double percentOfTotal) {
        this.symbol = symbol;
        this.componentVaR = componentVaR;
        this.percentOfTotal = percentOfTotal;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getComponentVaR() {
        return componentVaR;
    }

    /** Percentage (0-100) of total portfolio VaR attributable to this position. */
    public double getPercentOfTotal() {
        return percentOfTotal;
    }

    @Override
    public String toString() {
        return String.format("%-10s %6.2f%%  (Component VaR: %.2f)", symbol, percentOfTotal, componentVaR);
    }
}
