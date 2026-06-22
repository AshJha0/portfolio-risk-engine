package com.riskengine.core.pricing;

import com.riskengine.core.model.OptionType;
import org.apache.commons.math3.distribution.NormalDistribution;

/**
 * Garman-Kohlhagen model: the standard Black-Scholes extension for FX options, treating the
 * "dividend yield" as the foreign risk-free rate, so the underlying drifts at
 * (domestic rate - foreign rate) rather than the domestic rate alone.
 *
 * <p>This is the FX option model referenced under "Advanced Extensions" in the spec
 * (the engine's existing FX option pricing models referenced there).</p>
 */
public class GarmanKohlhagenModel {

    private static final NormalDistribution N = new NormalDistribution();

    private double d1(double spot, double strike, double t, double rd, double rf, double vol) {
        return (Math.log(spot / strike) + (rd - rf + 0.5 * vol * vol) * t) / (vol * Math.sqrt(t));
    }

    private double d2(double d1, double vol, double t) {
        return d1 - vol * Math.sqrt(t);
    }

    /**
     * @param spot       spot FX rate (domestic per unit foreign)
     * @param strike     strike rate
     * @param t          time to expiry in years
     * @param rd         domestic risk-free rate
     * @param rf         foreign risk-free rate
     * @param vol        FX volatility
     */
    public double price(OptionType type, double spot, double strike, double t,
                         double rd, double rf, double vol) {
        if (t <= 0) {
            return type == OptionType.CALL ? Math.max(spot - strike, 0) : Math.max(strike - spot, 0);
        }
        double d1 = d1(spot, strike, t, rd, rf, vol);
        double d2 = d2(d1, vol, t);
        double domesticDiscount = Math.exp(-rd * t);
        double foreignDiscount = Math.exp(-rf * t);

        if (type == OptionType.CALL) {
            return spot * foreignDiscount * N.cumulativeProbability(d1)
                    - strike * domesticDiscount * N.cumulativeProbability(d2);
        } else {
            return strike * domesticDiscount * N.cumulativeProbability(-d2)
                    - spot * foreignDiscount * N.cumulativeProbability(-d1);
        }
    }

    public double delta(OptionType type, double spot, double strike, double t,
                         double rd, double rf, double vol) {
        if (t <= 0) {
            return type == OptionType.CALL ? (spot > strike ? 1 : 0) : (spot < strike ? -1 : 0);
        }
        double d1 = d1(spot, strike, t, rd, rf, vol);
        double foreignDiscount = Math.exp(-rf * t);
        return type == OptionType.CALL
                ? foreignDiscount * N.cumulativeProbability(d1)
                : foreignDiscount * (N.cumulativeProbability(d1) - 1);
    }

    public double gamma(double spot, double strike, double t, double rd, double rf, double vol) {
        if (t <= 0) return 0;
        double d1 = d1(spot, strike, t, rd, rf, vol);
        double foreignDiscount = Math.exp(-rf * t);
        return foreignDiscount * N.density(d1) / (spot * vol * Math.sqrt(t));
    }

    public double vega(double spot, double strike, double t, double rd, double rf, double vol) {
        if (t <= 0) return 0;
        double d1 = d1(spot, strike, t, rd, rf, vol);
        double foreignDiscount = Math.exp(-rf * t);
        return spot * foreignDiscount * N.density(d1) * Math.sqrt(t) * 0.01;
    }
}
