package com.riskengine.analytics.reports;

import com.riskengine.analytics.contribution.RiskContribution;
import com.riskengine.stress.scenarios.StressTestResult;

import java.time.Instant;
import java.util.List;

/**
 * Aggregate risk report combining all metrics the spec's Dashboard section calls for:
 * Portfolio Value, 95%/99% VaR, Expected Shortfall, Top Risk Contributors, and Stress
 * Test / Scenario Results. This is the object the REST API and dashboard module serialize.
 */
public class RiskReport {

    private Instant generatedAt = Instant.now();
    private String portfolioName;
    private double portfolioValue;

    private double historicalVar95;
    private double historicalVar99;
    private double parametricVar99;
    private double monteCarloVar99;
    private double expectedShortfall99;

    private List<RiskContribution> topRiskContributors;
    private List<StressTestResult> stressTestResults;

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public void setPortfolioName(String portfolioName) {
        this.portfolioName = portfolioName;
    }

    public double getPortfolioValue() {
        return portfolioValue;
    }

    public void setPortfolioValue(double portfolioValue) {
        this.portfolioValue = portfolioValue;
    }

    public double getHistoricalVar95() {
        return historicalVar95;
    }

    public void setHistoricalVar95(double historicalVar95) {
        this.historicalVar95 = historicalVar95;
    }

    public double getHistoricalVar99() {
        return historicalVar99;
    }

    public void setHistoricalVar99(double historicalVar99) {
        this.historicalVar99 = historicalVar99;
    }

    public double getParametricVar99() {
        return parametricVar99;
    }

    public void setParametricVar99(double parametricVar99) {
        this.parametricVar99 = parametricVar99;
    }

    public double getMonteCarloVar99() {
        return monteCarloVar99;
    }

    public void setMonteCarloVar99(double monteCarloVar99) {
        this.monteCarloVar99 = monteCarloVar99;
    }

    public double getExpectedShortfall99() {
        return expectedShortfall99;
    }

    public void setExpectedShortfall99(double expectedShortfall99) {
        this.expectedShortfall99 = expectedShortfall99;
    }

    public List<RiskContribution> getTopRiskContributors() {
        return topRiskContributors;
    }

    public void setTopRiskContributors(List<RiskContribution> topRiskContributors) {
        this.topRiskContributors = topRiskContributors;
    }

    public List<StressTestResult> getStressTestResults() {
        return stressTestResults;
    }

    public void setStressTestResults(List<StressTestResult> stressTestResults) {
        this.stressTestResults = stressTestResults;
    }
}
