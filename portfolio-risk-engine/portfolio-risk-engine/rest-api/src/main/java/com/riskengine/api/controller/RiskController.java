package com.riskengine.api.controller;

import com.riskengine.api.dto.VarRequest;
import com.riskengine.api.dto.VarResponse;
import com.riskengine.api.service.RiskCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes {@code POST /risk/var} as specified:
 * <pre>
 * Request:  VarRequest (portfolio + market data + historical P&amp;L + volatility)
 * Response: { "historicalVar": ..., "parametricVar": ..., "monteCarloVar": ..., "expectedShortfall": ... }
 * </pre>
 */
@RestController
@RequestMapping("/risk")
public class RiskController {

    private final RiskCalculationService riskCalculationService;

    public RiskController(RiskCalculationService riskCalculationService) {
        this.riskCalculationService = riskCalculationService;
    }

    @PostMapping("/var")
    public ResponseEntity<VarResponse> calculateVar(@RequestBody VarRequest request) {
        return ResponseEntity.ok(riskCalculationService.calculateVar(request));
    }
}
