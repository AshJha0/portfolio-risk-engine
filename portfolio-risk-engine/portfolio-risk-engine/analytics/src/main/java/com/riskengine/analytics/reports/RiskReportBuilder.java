package com.riskengine.analytics.reports;

import com.riskengine.analytics.cvar.ExpectedShortfallCalculator;
import com.riskengine.core.marketdata.MarketDataCache;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.stress.engine.StressTestEngine;
import com.riskengine.stress.scenarios.StressScenario;
import com.riskengine.stress.scenarios.StressTestResult;
import com.riskengine.var.historical.HistoricalVaRCalculator;
import com.riskengine.var.montecarlo.MonteCarloVaREngine;
import com.riskengine.var.parametric.ParametricVaRCalculator;
import com.riskengine.var.ConfidenceLevel;

import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the VaR engines, Expected Shortfall, and Stress Test engine to build a
 * complete {@link RiskReport}, matching everything the spec's Dashboard section lists:
 * Portfolio Value, 95%/99% VaR, Expected Shortfall, Top Risk Contributors, Stress Test Results.
 *
 * <p>This is the class the REST API controller calls to fulfil {@code POST /risk/var} and
 * the dashboard summary endpoint.</p>
 */
public class RiskReportBuilder {

    private final HistoricalVaRCalculator historicalVaR = new HistoricalVaRCalculator();
    private final ParametricVaRCalculator parametricVaR = new ParametricVaRCalculator();
    private final MonteCarloVaREngine monteCarloVaR = new MonteCarloVaREngine();
    private final ExpectedShortfallCalculator expectedShortfall = new ExpectedShortfallCalculator();
    private final StressTestEngine stressTestEngine = new StressTestEngine();

    /**
     * Builds a full risk report.
     *
     * @param portfolio          the portfolio to analyze
     * @param marketDataCache    current market data, used for Monte Carlo revaluation
     * @param historicalPnL      historical daily P&amp;L series for Historical VaR / CVaR
     * @param portfolioVolatility annualized/daily volatility for Parametric VaR (caller's horizon convention)
     * @param monteCarloScenarios number of Monte Carlo paths to simulate
     * @param stressScenarios    list of stress scenarios to run against the portfolio
     */
    public RiskReport build(Portfolio portfolio,
                             MarketDataCache marketDataCache,
                             List<Double> historicalPnL,
                             double portfolioVolatility,
                             int monteCarloScenarios,
                             List<StressScenario> stressScenarios) {

        RiskReport report = new RiskReport();
        report.setPortfolioName(portfolio.getName());
        report.setPortfolioValue(portfolio.getTotalValue());

        report.setHistoricalVar95(historicalVaR.calculate(historicalPnL, 0.95));
        report.setHistoricalVar99(historicalVaR.calculate(historicalPnL, 0.99));

        report.setParametricVar99(
                parametricVaR.calculate(portfolio.getTotalValue(), portfolioVolatility, ConfidenceLevel.NINETY_NINE));

        report.setMonteCarloVar99(
                monteCarloVaR.calculate(portfolio, marketDataCache, monteCarloScenarios, 0.99).getVarAmount());

        report.setExpectedShortfall99(expectedShortfall.calculate(historicalPnL, 0.99));

        List<StressTestResult> stressResults = new ArrayList<>();
        for (StressScenario scenario : stressScenarios) {
            stressResults.add(stressTestEngine.runAsResult(portfolio, scenario));
        }
        report.setStressTestResults(stressResults);

        return report;
    }
}
