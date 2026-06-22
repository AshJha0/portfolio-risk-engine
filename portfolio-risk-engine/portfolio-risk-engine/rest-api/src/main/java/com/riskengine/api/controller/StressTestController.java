package com.riskengine.api.controller;

import com.riskengine.api.dto.StressTestRequest;
import com.riskengine.api.dto.StressTestResponse;
import com.riskengine.api.service.StressTestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes {@code POST /risk/stress} as specified:
 * <pre>
 * Request:  { "scenario": "2008 Crisis", "positions": [...] }
 * Response: { "scenario": "2008 Crisis", "loss": -12500000 }
 * </pre>
 */
@RestController
@RequestMapping("/risk")
public class StressTestController {

    private final StressTestService stressTestService;

    public StressTestController(StressTestService stressTestService) {
        this.stressTestService = stressTestService;
    }

    @PostMapping("/stress")
    public ResponseEntity<StressTestResponse> runStressTest(@RequestBody StressTestRequest request) {
        return ResponseEntity.ok(stressTestService.runStressTest(request));
    }
}
