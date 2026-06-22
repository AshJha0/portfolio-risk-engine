package com.riskengine.stress.engine;

import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.stress.scenarios.ScenarioDefinition;

/**
 * Runs a {@link ScenarioDefinition} (what-if analysis) against a portfolio. Positions are
 * matched to factor shocks by symbol; any symbol not present in the scenario's factor map
 * is assumed unaffected (shock = 0).
 */
public class ScenarioAnalysisEngine {

    /**
     * @return signed portfolio P&amp;L under the scenario
     */
    public double run(Portfolio portfolio, ScenarioDefinition scenario) {
        double pnl = 0.0;
        for (Position p : portfolio.getPositions()) {
            double shock = scenario.getShock(p.getSymbol());
            pnl += p.getQuantity() * p.getMarketPrice() * shock;
        }
        return pnl;
    }
}
