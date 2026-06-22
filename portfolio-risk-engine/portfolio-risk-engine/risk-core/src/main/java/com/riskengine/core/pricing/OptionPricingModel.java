package com.riskengine.core.pricing;

import com.riskengine.core.model.OptionType;

/**
 * Common contract for option pricing models used by the Greeks engine.
 *
 * <p>Implementations: {@link BlackScholesModel} (equity options),
 * {@link GarmanKohlhagenModel} (FX options). Heston, SABR, and Dupire local
 * vol are stochastic/local-vol extensions referenced in the spec's "Advanced
 * Extensions" section — see {@link HestonModel}, {@link SabrModel}, and
 * {@link DupireLocalVolModel} for their model contracts and integration points.</p>
 */
public interface OptionPricingModel {

    /** Theoretical fair value of the option. */
    double price(OptionType type, double spot, double strike, double timeToExpiry,
                 double riskFreeRate, double volatility);

    /** d(Price)/d(Spot) */
    double delta(OptionType type, double spot, double strike, double timeToExpiry,
                 double riskFreeRate, double volatility);

    /** d(Delta)/d(Spot) = d^2(Price)/d(Spot)^2 */
    double gamma(double spot, double strike, double timeToExpiry,
                 double riskFreeRate, double volatility);

    /** d(Price)/d(Volatility), scaled per 1 vol point (1%) by convention in this engine. */
    double vega(double spot, double strike, double timeToExpiry,
                double riskFreeRate, double volatility);

    /** d(Price)/d(Time), i.e. time decay, scaled per calendar day. */
    double theta(OptionType type, double spot, double strike, double timeToExpiry,
                 double riskFreeRate, double volatility);

    /** d(Price)/d(RiskFreeRate), scaled per 1% rate move. */
    double rho(OptionType type, double spot, double strike, double timeToExpiry,
               double riskFreeRate, double volatility);
}
