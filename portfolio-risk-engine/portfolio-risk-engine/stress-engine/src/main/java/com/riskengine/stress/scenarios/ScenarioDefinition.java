package com.riskengine.stress.scenarios;

import java.util.HashMap;
import java.util.Map;

/**
 * A "what-if" scenario as distinguished from {@link StressScenario} in the spec:
 * <pre>
 *   Stress Test       -&gt; Extreme Event   (e.g. 2008 Crisis, COVID Crash)
 *   Scenario Analysis  -&gt; What-if Analysis (e.g. "S&amp;P +5%, USD -2%, Vol +10%")
 * </pre>
 *
 * <p>Unlike {@link StressScenario}, which has fixed equity/FX/vol/rate fields, a
 * ScenarioDefinition holds an arbitrary, named map of factor shocks (e.g. "SPX" -&gt; +0.05,
 * "RATES_BPS" -&gt; 50, "EURUSD" -&gt; -0.08), letting callers express any combination of
 * what-if moves without being constrained to one shock per category.</p>
 */
public class ScenarioDefinition {

    private String name;
    private final Map<String, Double> factorShocks = new HashMap<>();

    public ScenarioDefinition(String name) {
        this.name = name;
    }

    public ScenarioDefinition withShock(String factor, double shock) {
        factorShocks.put(factor, shock);
        return this;
    }

    public double getShock(String factor) {
        return factorShocks.getOrDefault(factor, 0.0);
    }

    public Map<String, Double> getFactorShocks() {
        return factorShocks;
    }

    public String getName() {
        return name;
    }

    /** Spec example: Scenario 1 - S&amp;P +5%, USD -2%, Vol +10%. */
    public static ScenarioDefinition spxRallyWithWeakDollar() {
        return new ScenarioDefinition("S&P +5% / USD -2% / Vol +10%")
                .withShock("SPX", 0.05)
                .withShock("USD", -0.02)
                .withShock("VOL", 0.10);
    }

    /** Spec example: Scenario 2 - Rates +50bps. */
    public static ScenarioDefinition ratesUp50bps() {
        return new ScenarioDefinition("Rates +50bps")
                .withShock("RATES_BPS", 50);
    }

    /** Spec example: Scenario 3 - EUR/USD -8%. */
    public static ScenarioDefinition eurUsdDown8Percent() {
        return new ScenarioDefinition("EUR/USD -8%")
                .withShock("EURUSD", -0.08);
    }
}
