package com.riskengine.core.pricing;

/**
 * Extension point for Dupire's local volatility model, which derives a deterministic
 * volatility surface sigma(S, t) from observed market option prices such that the model
 * exactly reproduces the full implied volatility surface.
 *
 * <p><b>Not implemented in this module.</b> A production implementation needs:</p>
 * <ul>
 *   <li>A smooth, arbitrage-free implied volatility surface as input (typically fit with
 *       SVI or a spline across strikes/expiries)</li>
 *   <li>Dupire's formula to convert that surface into local volatility:
 *       sigma_loc(K,T)^2 = 2 * (dC/dT + r*K*dC/dK) / (K^2 * d2C/dK2)</li>
 *   <li>A finite-difference PDE solver (Crank-Nicolson is standard) to price exotic /
 *       path-dependent payoffs under the resulting local vol surface</li>
 * </ul>
 *
 * <p>This sits alongside {@link HestonModel} and {@link SabrModel} as a smile-consistent
 * pricing extension; none of the three are required for the core VaR/CVaR/stress engines,
 * which operate on quoted/interpolated implied vols directly.</p>
 */
public interface DupireLocalVolModel {

    /**
     * Returns the local volatility at the given strike/expiry point, derived from the
     * implied volatility surface via Dupire's formula.
     */
    double localVolatility(double strike, double timeToExpiry);
}
