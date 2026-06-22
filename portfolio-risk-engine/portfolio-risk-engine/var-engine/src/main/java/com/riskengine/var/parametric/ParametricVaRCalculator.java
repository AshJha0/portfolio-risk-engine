package com.riskengine.var.parametric;

import com.riskengine.var.ConfidenceLevel;
import com.riskengine.var.VaRResult;

/**
 * Parametric (variance-covariance) VaR: assumes portfolio returns are normally distributed.
 *
 * <p>Formula: VaR = z * sigma * PortfolioValue, where z is the one-tailed normal quantile
 * for the chosen confidence level (1.65 for 95%, 2.33 for 99%) and sigma is the portfolio's
 * return volatility over the VaR horizon.</p>
 */
public class ParametricVaRCalculator {

    /**
     * @param portfolioValue current mark-to-market value of the portfolio
     * @param volatility     portfolio return volatility over the VaR horizon (e.g. daily stdev)
     * @param zScore         one-tailed normal quantile, e.g. 1.65 (95%) or 2.33 (99%)
     * @return VaR as a positive loss amount
     */
    public double calculate(double portfolioValue, double volatility, double zScore) {
        return portfolioValue * volatility * zScore;
    }

    /** Convenience overload using a standard {@link ConfidenceLevel}. */
    public double calculate(double portfolioValue, double volatility, ConfidenceLevel level) {
        return calculate(portfolioValue, volatility, level.getZScore());
    }

    public VaRResult calculateResult(double portfolioValue, double volatility, ConfidenceLevel level) {
        double var = calculate(portfolioValue, volatility, level);
        return new VaRResult("Parametric", level.getConfidence(), var);
    }

    /**
     * Scales a known volatility from one horizon to another using the square-root-of-time rule
     * (e.g. daily volatility -> 10-day volatility for regulatory capital horizons).
     */
    public double scaleVolatility(double volatility, double fromDays, double toDays) {
        return volatility * Math.sqrt(toDays / fromDays);
    }
}
