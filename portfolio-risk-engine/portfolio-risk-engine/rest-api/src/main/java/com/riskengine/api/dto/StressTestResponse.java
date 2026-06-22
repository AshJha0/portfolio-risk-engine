package com.riskengine.api.dto;

/**
 * Response body for {@code POST /risk/stress}, matching the spec's exact shape:
 * <pre>
 * {
 *   "scenario": "2008 Crisis",
 *   "loss": -12500000
 * }
 * </pre>
 */
public class StressTestResponse {

    public String scenario;
    public double loss;

    public StressTestResponse() {
    }

    public StressTestResponse(String scenario, double loss) {
        this.scenario = scenario;
        this.loss = loss;
    }
}
