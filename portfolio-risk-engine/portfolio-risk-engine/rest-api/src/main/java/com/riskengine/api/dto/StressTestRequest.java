package com.riskengine.api.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for {@code POST /risk/stress}.
 *
 * <p>Accepts either a named preset scenario ("2008 Crisis" or "COVID Crash") or fully
 * custom shock parameters.</p>
 * <pre>
 * {
 *   "portfolioName": "Global Macro Book",
 *   "positions": [...],
 *   "scenario": "2008 Crisis"
 * }
 * </pre>
 */
public class StressTestRequest {

    public String portfolioName;
    public List<PositionDto> positions = new ArrayList<>();

    /** Named preset: "2008 Crisis" or "COVID Crash". If set, custom shock fields below are ignored. */
    public String scenario;

    // Custom scenario fields, used only if `scenario` above is null/unrecognized
    public Double equityShock;
    public Double fxShock;
    public Double volShock;
    public Double rateShockBps;
}
