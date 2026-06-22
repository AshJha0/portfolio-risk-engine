package com.riskengine.api.service;

import com.riskengine.analytics.cvar.ExpectedShortfallCalculator;
import com.riskengine.api.dto.VarRequest;
import com.riskengine.api.dto.VarResponse;
import com.riskengine.core.marketdata.MarketDataCache;
import com.riskengine.core.portfolio.Portfolio;
import com.riskengine.var.ConfidenceLevel;
import com.riskengine.var.historical.HistoricalVaRCalculator;
import com.riskengine.var.montecarlo.MonteCarloVaREngine;
import com.riskengine.var.parametric.ParametricVaRCalculator;
import org.springframework.stereotype.Service;

/**
 * Service layer backing {@code POST /risk/var}: builds the portfolio and market data cache
 * from the request DTO and runs all three VaR methodologies plus Expected Shortfall.
 */
@Service
public class RiskCalculationService {

    private final HistoricalVaRCalculator historicalVaRCalculator = new HistoricalVaRCalculator();
    private final ParametricVaRCalculator parametricVaRCalculator = new ParametricVaRCalculator();
    private final MonteCarloVaREngine monteCarloVaREngine = new MonteCarloVaREngine();
    private final ExpectedShortfallCalculator expectedShortfallCalculator = new ExpectedShortfallCalculator();

    public VarResponse calculateVar(VarRequest request) {
        Portfolio portfolio = request.toPortfolio();

        MarketDataCache cache = new MarketDataCache();
        cache.putAll(request.marketData);

        double historicalVar = request.historicalPnL.isEmpty()
                ? 0.0
                : historicalVaRCalculator.calculate(request.historicalPnL, 0.99);

        double parametricVar = parametricVaRCalculator.calculate(
                portfolio.getTotalValue(), request.portfolioVolatility, ConfidenceLevel.NINETY_NINE);

        double monteCarloVar = (!portfolio.getPositions().isEmpty() && cache.size() > 0)
                ? monteCarloVaREngine.calculate(portfolio, cache, request.monteCarloScenarios, 0.99).getVarAmount()
                : 0.0;

        double expectedShortfall = request.historicalPnL.isEmpty()
                ? 0.0
                : expectedShortfallCalculator.calculate(request.historicalPnL, 0.99);

        return new VarResponse(historicalVar, parametricVar, monteCarloVar, expectedShortfall);
    }
}
