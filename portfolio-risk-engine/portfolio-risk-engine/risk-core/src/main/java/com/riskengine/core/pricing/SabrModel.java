package com.riskengine.core.pricing;

/**
 * Extension point for the SABR (Stochastic Alpha Beta Rho) model, commonly used to
 * parameterize the implied volatility smile for interest rate and FX options.
 *
 * <p><b>Not implemented in this module.</b> A production SABR implementation needs:</p>
 * <ul>
 *   <li>Hagan et al. (2002) asymptotic implied-volatility formula as a function of
 *       (alpha, beta, rho, nu) and strike</li>
 *   <li>Calibration of (alpha, rho, nu) per expiry to the observed market smile, usually
 *       with beta fixed by convention (e.g. 0.5 for rates, ~1.0 for FX)</li>
 *   <li>Interpolation/extrapolation across the smile and term structure</li>
 * </ul>
 *
 * <p>Intended integration point: implement this interface, then plug the resulting smile
 * vol into {@link BlackScholesModel} / {@link GarmanKohlhagenModel} for pricing and Greeks
 * (the standard "SABR for vol, Black-Scholes for price" approach).</p>
 */
public interface SabrModel {

    /**
     * Returns the SABR-implied Black volatility for the given strike/expiry, suitable for
     * feeding into a Black-Scholes / Garman-Kohlhagen pricer.
     *
     * @param forward current forward price
     * @param strike  option strike
     * @param t       time to expiry in years
     * @param alpha   initial volatility level
     * @param beta    CEV exponent (controls backbone shape, typically fixed by convention)
     * @param rho     correlation between forward and volatility
     * @param nu      volatility of volatility
     */
    double impliedVolatility(double forward, double strike, double t,
                              double alpha, double beta, double rho, double nu);
}
