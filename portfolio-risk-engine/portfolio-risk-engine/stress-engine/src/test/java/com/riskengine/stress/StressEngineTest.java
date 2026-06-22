package com.riskengine.stress;

import com.riskengine.core.model.AssetClass;
import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.stress.engine.ScenarioAnalysisEngine;
import com.riskengine.stress.engine.StressTestEngine;
import com.riskengine.stress.scenarios.ScenarioDefinition;
import com.riskengine.stress.scenarios.StressScenario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StressEngineTest {

    @Test
    void financialCrisis2008AppliesNegative40PercentToEquities() {
        Portfolio portfolio = new Portfolio();
        portfolio.addPosition(new Position("SPX", AssetClass.EQUITY, 100, 5000.0)); // 500,000 notional

        StressTestEngine engine = new StressTestEngine();
        double pnl = engine.run(portfolio, StressScenario.financialCrisis2008());

        assertEquals(-200_000.0, pnl, 1e-6);
    }

    @Test
    void fxShockOnlyAppliesToFxPositions() {
        Portfolio portfolio = new Portfolio();
        portfolio.addPosition(new Position("EURUSD", AssetClass.FX, 1_000_000, 1.10));

        StressScenario scenario = new StressScenario("FX Crisis", -0.40, -0.10, 0, 0);
        StressTestEngine engine = new StressTestEngine();
        double pnl = engine.run(portfolio, scenario);

        assertEquals(1_000_000 * 1.10 * -0.10, pnl, 1e-6);
    }

    @Test
    void scenarioAnalysisAppliesNamedFactorShockToMatchingSymbol() {
        Portfolio portfolio = new Portfolio();
        portfolio.addPosition(new Position("EURUSD", AssetClass.FX, 1_000_000, 1.10));

        ScenarioAnalysisEngine engine = new ScenarioAnalysisEngine();
        double pnl = engine.run(portfolio, ScenarioDefinition.eurUsdDown8Percent());

        assertEquals(1_000_000 * 1.10 * -0.08, pnl, 1e-6);
    }

    @Test
    void unmatchedSymbolInScenarioContributesZeroPnl() {
        Portfolio portfolio = new Portfolio();
        portfolio.addPosition(new Position("AAPL", AssetClass.EQUITY, 100, 200.0));

        ScenarioAnalysisEngine engine = new ScenarioAnalysisEngine();
        double pnl = engine.run(portfolio, ScenarioDefinition.ratesUp50bps());

        assertTrue(pnl == 0.0);
    }
}
