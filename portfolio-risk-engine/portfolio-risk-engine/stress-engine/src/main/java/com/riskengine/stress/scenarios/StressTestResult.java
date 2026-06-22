package com.riskengine.stress.scenarios;

/**
 * Result of running a {@link StressScenario} against a portfolio.
 */
public class StressTestResult {

    private final String scenarioName;
    private final double pnl;

    public StressTestResult(String scenarioName, double pnl) {
        this.scenarioName = scenarioName;
        this.pnl = pnl;
    }

    public String getScenarioName() {
        return scenarioName;
    }

    /** Signed P&amp;L impact of the scenario; negative = loss. */
    public double getPnl() {
        return pnl;
    }

    @Override
    public String toString() {
        return "StressTestResult{scenario='" + scenarioName + "', pnl=" + pnl + '}';
    }
}
