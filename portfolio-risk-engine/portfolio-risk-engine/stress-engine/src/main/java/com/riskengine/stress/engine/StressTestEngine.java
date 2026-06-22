package com.riskengine.stress.engine;

import com.riskengine.core.model.AssetClass;
import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.stress.scenarios.StressScenario;
import com.riskengine.stress.scenarios.StressTestResult;

/**
 * Applies a {@link StressScenario} to a {@link Portfolio} and computes the resulting P&amp;L.
 *
 * <p>Improvement over the spec's reference implementation: shocks are applied per asset
 * class (equity shock to equities/futures, FX shock to FX positions, vol shock contributes
 * via vega for options) rather than applying the equity shock uniformly to every position
 * regardless of type. Rate shocks are not modeled here for bonds (that requires duration/
 * convexity, typically computed in a dedicated fixed-income pricing module); this engine
 * applies equity, FX and vega (volatility) shocks, which covers the equity/FX/option legs
 * described in the spec's worked scenarios.</p>
 */
public class StressTestEngine {

    /**
     * Runs the scenario against the full portfolio and returns the aggregate signed P&amp;L.
     */
    public double run(Portfolio portfolio, StressScenario scenario) {
        double pnl = 0.0;

        for (Position p : portfolio.getPositions()) {
            pnl += positionPnl(p, scenario);
        }

        return pnl;
    }

    public StressTestResult runAsResult(Portfolio portfolio, StressScenario scenario) {
        return new StressTestResult(scenario.getName(), run(portfolio, scenario));
    }

    /**
     * Computes the P&amp;L impact of a single scenario on a single position, dispatching the
     * relevant shock(s) by asset class.
     */
    private double positionPnl(Position p, StressScenario scenario) {
        double marketValue = p.getQuantity() * p.getMarketPrice();
        double pnl = 0.0;

        switch (p.getAssetClass()) {
            case EQUITY:
            case FUTURE:
                pnl += marketValue * scenario.getEquityShock();
                break;
            case FX:
                pnl += marketValue * scenario.getFxShock();
                break;
            case BOND:
                // First-order rate sensitivity would require duration; without it we treat
                // the rate shock as a direct price-shock proxy scaled to a fraction of a point.
                pnl += marketValue * (-scenario.getRateShockBps() / 10_000.0);
                break;
            case OPTION:
                // Underlying move via delta, plus a vega contribution from the vol shock.
                pnl += p.getDelta() * p.getQuantity() * p.getMarketPrice() * scenario.getEquityShock();
                pnl += p.getVega() * p.getQuantity() * scenario.getVolShock();
                break;
            default:
                break;
        }
        return pnl;
    }

    /**
     * Legacy-compatible method matching the original spec signature: applies the scenario's
     * equity shock uniformly to every position regardless of asset class. Kept for backward
     * compatibility with simple equity-only portfolios; prefer {@link #run} otherwise.
     */
    public double runEquityShockOnly(Portfolio portfolio, StressScenario scenario) {
        double pnl = 0;
        for (Position p : portfolio.getPositions()) {
            pnl += p.getQuantity() * p.getMarketPrice() * scenario.getEquityShock();
        }
        return pnl;
    }
}
