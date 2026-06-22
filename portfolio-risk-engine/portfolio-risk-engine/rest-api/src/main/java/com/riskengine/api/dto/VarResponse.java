package com.riskengine.api.dto;

/**
 * Response body for {@code POST /risk/var}, matching the exact field names from the spec:
 * <pre>
 * {
 *   "historicalVar": 4100000,
 *   "parametricVar": 3900000,
 *   "monteCarloVar": 4300000,
 *   "expectedShortfall": 6100000
 * }
 * </pre>
 */
public class VarResponse {

    public double historicalVar;
    public double parametricVar;
    public double monteCarloVar;
    public double expectedShortfall;

    public VarResponse() {
    }

    public VarResponse(double historicalVar, double parametricVar, double monteCarloVar, double expectedShortfall) {
        this.historicalVar = historicalVar;
        this.parametricVar = parametricVar;
        this.monteCarloVar = monteCarloVar;
        this.expectedShortfall = expectedShortfall;
    }
}
