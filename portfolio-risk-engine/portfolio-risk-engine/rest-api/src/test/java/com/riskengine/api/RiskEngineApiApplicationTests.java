package com.riskengine.api;

import com.riskengine.api.dto.PositionDto;
import com.riskengine.api.dto.StressTestRequest;
import com.riskengine.api.dto.VarRequest;
import com.riskengine.core.marketdata.MarketData;
import com.riskengine.core.model.AssetClass;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RiskEngineApiApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void varEndpointReturnsAllFourMetrics() throws Exception {
        VarRequest request = new VarRequest();
        request.portfolioName = "Test Book";
        request.portfolioVolatility = 0.015;
        request.monteCarloScenarios = 1000;

        PositionDto position = new PositionDto();
        position.symbol = "AAPL";
        position.assetClass = AssetClass.EQUITY;
        position.quantity = 1000;
        position.marketPrice = 200.0;
        request.positions = List.of(position);

        request.marketData = List.of(new MarketData("AAPL", 200.0, 0.02, 0.04));
        request.historicalPnL = List.of(-50000.0, -30000.0, -10000.0, 0.0, 10000.0, 30000.0, 50000.0);

        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/risk/var")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.historicalVar").exists())
                .andExpect(jsonPath("$.parametricVar").exists())
                .andExpect(jsonPath("$.monteCarloVar").exists())
                .andExpect(jsonPath("$.expectedShortfall").exists());
    }

    @Test
    void stressEndpointReturns2008CrisisLoss() throws Exception {
        StressTestRequest request = new StressTestRequest();
        request.portfolioName = "Test Book";
        request.scenario = "2008 Crisis";

        PositionDto position = new PositionDto();
        position.symbol = "SPX";
        position.assetClass = AssetClass.EQUITY;
        position.quantity = 100;
        position.marketPrice = 5000.0;
        request.positions = List.of(position);

        String json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(request);

        mockMvc.perform(post("/risk/stress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scenario").value("2008 Crisis"))
                .andExpect(jsonPath("$.loss").value(-200000.0));
    }
}
