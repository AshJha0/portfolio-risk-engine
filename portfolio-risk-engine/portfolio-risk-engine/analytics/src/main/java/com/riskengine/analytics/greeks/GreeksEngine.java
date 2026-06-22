package com.riskengine.analytics.greeks;

import com.riskengine.core.model.AssetClass;
import com.riskengine.core.model.OptionType;
import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.core.pricing.BlackScholesModel;
import com.riskengine.core.pricing.OptionPricingModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Greeks-based risk engine for option positions ("Advanced Extensions" in the spec):
 * computes per-position Greeks (via an {@link OptionPricingModel}, defaulting to
 * Black-Scholes) and aggregates Delta/Gamma/Vega exposures and their associated VaR.
 *
 * <p>Delta-Gamma-Vega VaR approximates the change in an option's value for a given
 * underlying move (dS) and vol move (dVol) using a second-order Taylor expansion:</p>
 * <pre>
 *   dV ≈ Delta * dS + 0.5 * Gamma * dS^2 + Vega * dVol
 * </pre>
 * <p>This is a fast analytic approximation, cheaper than full Monte Carlo revaluation,
 * and is the standard approach used for intraday/real-time option risk.</p>
 */
public class GreeksEngine {

    private final OptionPricingModel pricingModel;

    public GreeksEngine() {
        this.pricingModel = new BlackScholesModel();
    }

    public GreeksEngine(OptionPricingModel pricingModel) {
        this.pricingModel = pricingModel;
    }

    /**
     * Populates delta/gamma/vega/theta/rho on the position using its option fields
     * (strike, timeToExpiryYears, impliedVolatility, riskFreeRate) and current market price
     * as the underlying spot. No-op for non-option positions.
     */
    public void computeGreeks(Position position) {
        if (position.getAssetClass() != AssetClass.OPTION) {
            return;
        }
        double spot = position.getMarketPrice();
        double strike = position.getStrike();
        double t = position.getTimeToExpiryYears();
        double r = position.getRiskFreeRate();
        double vol = position.getImpliedVolatility();
        OptionType type = position.getOptionType();

        position.setDelta(pricingModel.delta(type, spot, strike, t, r, vol));
        position.setGamma(pricingModel.gamma(spot, strike, t, r, vol));
        position.setVega(pricingModel.vega(spot, strike, t, r, vol));
        position.setTheta(pricingModel.theta(type, spot, strike, t, r, vol));
        position.setRho(pricingModel.rho(type, spot, strike, t, r, vol));
    }

    public void computeGreeksForPortfolio(Portfolio portfolio) {
        portfolio.getPositions().forEach(this::computeGreeks);
    }

    /** Portfolio-level dollar delta: sum of (quantity * delta * spot) across option positions. */
    public double portfolioDollarDelta(Portfolio portfolio) {
        double total = 0;
        for (Position p : portfolio.getPositions()) {
            if (p.getAssetClass() == AssetClass.OPTION) {
                total += p.getQuantity() * p.getDelta() * p.getMarketPrice();
            }
        }
        return total;
    }

    public double portfolioDollarGamma(Portfolio portfolio) {
        double total = 0;
        for (Position p : portfolio.getPositions()) {
            if (p.getAssetClass() == AssetClass.OPTION) {
                total += p.getQuantity() * p.getGamma() * p.getMarketPrice() * p.getMarketPrice();
            }
        }
        return total;
    }

    public double portfolioVega(Portfolio portfolio) {
        double total = 0;
        for (Position p : portfolio.getPositions()) {
            if (p.getAssetClass() == AssetClass.OPTION) {
                total += p.getQuantity() * p.getVega();
            }
        }
        return total;
    }

    /**
     * Delta VaR: VaR contribution purely from first-order (delta) underlying exposure.
     * Treats each option's delta exposure as an equivalent underlying position and applies
     * a parametric-style shock: |delta * quantity * spot| * volatility * zScore.
     */
    public double deltaVaR(Portfolio portfolio, double underlyingVolatility, double zScore) {
        return Math.abs(portfolioDollarDelta(portfolio)) * underlyingVolatility * zScore;
    }

    /**
     * Gamma VaR: second-order convexity contribution to VaR, approximated as
     * 0.5 * |dollar gamma| * (volatility * zScore)^2 — i.e. the P&amp;L impact of curvature
     * over the same stress move used for Delta VaR.
     */
    public double gammaVaR(Portfolio portfolio, double underlyingVolatility, double zScore) {
        double move = underlyingVolatility * zScore;
        return 0.5 * Math.abs(portfolioDollarGamma(portfolio)) * move * move;
    }

    /**
     * Vega VaR: VaR contribution from a shock to implied volatility itself, e.g. a vol-of-vol
     * stress (volShock expressed in vol points, matching the scaling convention used in
     * {@link OptionPricingModel#vega}).
     */
    public double vegaVaR(Portfolio portfolio, double volShockInPoints) {
        return Math.abs(portfolioVega(portfolio)) * volShockInPoints;
    }

    /**
     * Delta-Gamma-Vega approximation of P&amp;L for a given underlying move and vol move,
     * applied per option position and summed across the portfolio.
     */
    public double deltaGammaVegaPnl(Portfolio portfolio, double underlyingMove, double volMoveInPoints) {
        double pnl = 0;
        for (Position p : portfolio.getPositions()) {
            if (p.getAssetClass() != AssetClass.OPTION) continue;
            double dS = p.getMarketPrice() * underlyingMove;
            double positionPnl = p.getQuantity() * (
                    p.getDelta() * dS
                            + 0.5 * p.getGamma() * dS * dS
                            + p.getVega() * volMoveInPoints
            );
            pnl += positionPnl;
        }
        return pnl;
    }

    /** Returns the list of option positions in the portfolio, for reporting/filtering. */
    public List<Position> optionPositions(Portfolio portfolio) {
        List<Position> options = new ArrayList<>();
        for (Position p : portfolio.getPositions()) {
            if (p.getAssetClass() == AssetClass.OPTION) {
                options.add(p);
            }
        }
        return options;
    }
}
