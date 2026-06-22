package com.riskengine.api.dto;

import com.riskengine.core.marketdata.MarketData;
import com.riskengine.core.model.Position;
import com.riskengine.core.portfolio.Portfolio;

import java.util.ArrayList;
import java.util.List;

/**
 * Request body for {@code POST /risk/var}.
 *
 * <p>Example:</p>
 * <pre>
 * {
 *   "portfolioName": "Global Macro Book",
 *   "positions": [
 *     {"symbol":"AAPL","assetClass":"EQUITY","quantity":1000,"marketPrice":200.0}
 *   ],
 *   "marketData": [
 *     {"symbol":"AAPL","price":200.0,"volatility":0.02,"interestRate":0.04}
 *   ],
 *   "historicalPnL": [-50000, -30000, -10000, 0, 10000, 30000, 50000],
 *   "portfolioVolatility": 0.015,
 *   "monteCarloScenarios": 10000
 * }
 * </pre>
 */
public class VarRequest {

    public String portfolioName;
    public List<PositionDto> positions = new ArrayList<>();
    public List<MarketData> marketData = new ArrayList<>();
    public List<Double> historicalPnL = new ArrayList<>();
    public double portfolioVolatility;
    public int monteCarloScenarios = 10_000;

    public Portfolio toPortfolio() {
        List<Position> built = new ArrayList<>();
        for (PositionDto dto : positions) {
            built.add(dto.toPosition());
        }
        return new Portfolio(portfolioName, built);
    }
}
