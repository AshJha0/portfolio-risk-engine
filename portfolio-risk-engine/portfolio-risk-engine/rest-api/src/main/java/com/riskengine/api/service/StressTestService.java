package com.riskengine.api.service;

import com.riskengine.api.dto.StressTestRequest;
import com.riskengine.api.dto.StressTestResponse;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.stress.engine.StressTestEngine;
import com.riskengine.stress.scenarios.StressScenario;
import org.springframework.stereotype.Service;

/**
 * Service layer backing {@code POST /risk/stress}: resolves the requested scenario (named
 * preset or custom shocks) and runs it against the portfolio built from the request.
 */
@Service
public class StressTestService {

    private final StressTestEngine stressTestEngine = new StressTestEngine();

    public StressTestResponse runStressTest(StressTestRequest request) {
        Portfolio portfolio = buildPortfolio(request);
        StressScenario scenario = resolveScenario(request);

        double pnl = stressTestEngine.run(portfolio, scenario);
        return new StressTestResponse(scenario.getName(), pnl);
    }

    private Portfolio buildPortfolio(StressTestRequest request) {
        var positions = request.positions.stream().map(p -> p.toPosition()).toList();
        return new Portfolio(request.portfolioName, positions);
    }

    private StressScenario resolveScenario(StressTestRequest request) {
        if (request.scenario != null) {
            if (request.scenario.equalsIgnoreCase("2008 Crisis")) {
                return StressScenario.financialCrisis2008();
            }
            if (request.scenario.equalsIgnoreCase("COVID Crash")) {
                return StressScenario.covidCrash2020();
            }
        }
        // Custom scenario, defaulting any unset shock to 0
        return new StressScenario(
                request.scenario != null ? request.scenario : "Custom Scenario",
                request.equityShock != null ? request.equityShock : 0.0,
                request.fxShock != null ? request.fxShock : 0.0,
                request.volShock != null ? request.volShock : 0.0,
                request.rateShockBps != null ? request.rateShockBps : 0.0
        );
    }
}
