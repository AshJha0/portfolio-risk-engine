package com.riskengine.core.pricing;

/**
 * Extension point for the Heston (1993) stochastic volatility model.
 *
 * <p><b>Not implemented in this module.</b> A production Heston implementation needs:</p>
 * <ul>
 *   <li>Calibration of (kappa, theta, sigma, rho, v0) to a market vol surface, typically via
 *       least-squares against the semi-closed-form characteristic function and an optimizer
 *       (Levenberg-Marquardt or similar)</li>
 *   <li>Pricing via the Heston characteristic function and numerical Fourier inversion
 *       (e.g. Carr-Madan FFT or COS method) for speed, since there is no closed-form price</li>
 *   <li>Greeks via bumping the characteristic-function inputs or analytic derivatives of the
 *       characteristic function</li>
 * </ul>
 *
 * <p>This is intentionally left as an interface: wire a real implementation in here when the
 * vol-surface calibration pipeline exists upstream. Until then, {@link BlackScholesModel} or
 * {@link GarmanKohlhagenModel} with an interpolated implied vol is the pragmatic fallback used
 * by the rest of this engine (e.g. the Greeks engine in the analytics module).</p>
 */
public interface HestonModel {

    /**
     * @param spot          current underlying price
     * @param strike        option strike
     * @param t             time to expiry in years
     * @param r             risk-free rate
     * @param kappa         mean reversion speed of variance
     * @param theta         long-run variance level
     * @param sigma         volatility of volatility
     * @param rho            correlation between asset and variance Brownian motions
     * @param v0            initial variance
     * @return theoretical option price under the Heston model
     */
    double price(double spot, double strike, double t, double r,
                 double kappa, double theta, double sigma, double rho, double v0);
}
