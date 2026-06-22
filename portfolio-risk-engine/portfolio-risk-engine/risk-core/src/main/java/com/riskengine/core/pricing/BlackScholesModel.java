package com.riskengine.core.pricing;

import com.riskengine.core.model.OptionType;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Black-Scholes-Merton model for European equity options (no dividend yield variant).
 * For dividend-paying equities or FX, see {@link GarmanKohlhagenModel}.
 */
public class BlackScholesModel implements OptionPricingModel {

    private static final NormalDistribution N = new NormalDistribution();

    private double d1(double spot, double strike, double t, double r, double vol) {
        return (Math.log(spot / strike) + (r + 0.5 * vol * vol) * t) / (vol * Math.sqrt(t));
    }

    private double d2(double d1, double vol, double t) {
        return d1 - vol * Math.sqrt(t);
    }

    @Override
    public double price(OptionType type, double spot, double strike, double t, double r, double vol) {
        if (t <= 0) {
            return type == OptionType.CALL ? Math.max(spot - strike, 0) : Math.max(strike - spot, 0);
        }
        double d1 = d1(spot, strike, t, r, vol);
        double d2 = d2(d1, vol, t);
        double discount = Math.exp(-r * t);

        if (type == OptionType.CALL) {
            return spot * N.cumulativeProbability(d1) - strike * discount * N.cumulativeProbability(d2);
        } else {
            return strike * discount * N.cumulativeProbability(-d2) - spot * N.cumulativeProbability(-d1);
        }
    }

    @Override
    public double delta(OptionType type, double spot, double strike, double t, double r, double vol) {
        if (t <= 0) {
            return type == OptionType.CALL ? (spot > strike ? 1 : 0) : (spot < strike ? -1 : 0);
        }
        double d1 = d1(spot, strike, t, r, vol);
        return type == OptionType.CALL ? N.cumulativeProbability(d1) : N.cumulativeProbability(d1) - 1;
    }

    @Override
    public double gamma(double spot, double strike, double t, double r, double vol) {
        if (t <= 0) return 0;
        double d1 = d1(spot, strike, t, r, vol);
        return N.density(d1) / (spot * vol * Math.sqrt(t));
    }

    @Override
    public double vega(double spot, double strike, double t, double r, double vol) {
        if (t <= 0) return 0;
        double d1 = d1(spot, strike, t, r, vol);
        // Scaled to price change per 1 volatility point (1% = 0.01)
        return spot * N.density(d1) * Math.sqrt(t) * 0.01;
    }

    @Override
    public double theta(OptionType type, double spot, double strike, double t, double r, double vol) {
        if (t <= 0) return 0;
        double d1 = d1(spot, strike, t, r, vol);
        double d2 = d2(d1, vol, t);
        double term1 = -(spot * N.density(d1) * vol) / (2 * Math.sqrt(t));

        double annualTheta;
        if (type == OptionType.CALL) {
            annualTheta = term1 - r * strike * Math.exp(-r * t) * N.cumulativeProbability(d2);
        } else {
            annualTheta = term1 + r * strike * Math.exp(-r * t) * N.cumulativeProbability(-d2);
        }
        // Scaled to price change per calendar day
        return annualTheta / 365.0;
    }

    @Override
    public double rho(OptionType type, double spot, double strike, double t, double r, double vol) {
        if (t <= 0) return 0;
        double d1 = d1(spot, strike, t, r, vol);
        double d2 = d2(d1, vol, t);
        double discount = Math.exp(-r * t);

        double annualRho;
        if (type == OptionType.CALL) {
            annualRho = strike * t * discount * N.cumulativeProbability(d2);
        } else {
            annualRho = -strike * t * discount * N.cumulativeProbability(-d2);
        }
        // Scaled to price change per 1% rate move
        return annualRho * 0.01;
    }
}
