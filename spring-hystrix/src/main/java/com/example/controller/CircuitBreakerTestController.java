package com.example.controller;

import com.example.service.ExternalApiService;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/test")
public class CircuitBreakerTestController {

    @Autowired
    private ExternalApiService externalApiService;

    /**
     * 1. 정상 API 호출 (항상 성공)
     */
    @RequestMapping(value = "/normal/{data}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callNormalApi(@PathVariable String data) {
        try {
            String result = externalApiService.callNormalApi(data);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * 2. 랜덤 API 호출 (50% 실패)
     */
    @RequestMapping(value = "/random", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callRandomApi() {
        try {
            String result = externalApiService.callRandomApi();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * 3. 실패 API 호출 (항상 실패)
     */
    @RequestMapping(value = "/failing", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callFailingApi() {
        try {
            String result = externalApiService.callFailingApi();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * 4. 느린 API 호출 (3초 지연)
     */
    @RequestMapping(value = "/slow", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callSlowApi() {
        try {
            String result = externalApiService.callSlowApi();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * Circuit Breaker 상태 확인 (간단한 모니터링)
     */
    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 각 Command의 Circuit Breaker 상태 확인
        status.put("normalApi", getCircuitBreakerInfo("callNormalApi"));
        status.put("randomApi", getCircuitBreakerInfo("callRandomApi"));
        status.put("failingApi", getCircuitBreakerInfo("callFailingApi"));
        status.put("slowApi", getCircuitBreakerInfo("callSlowApi"));
        
        return ResponseEntity.ok(status);
    }

    private Map<String, Object> getCircuitBreakerInfo(String commandKey) {
        Map<String, Object> info = new HashMap<>();
        HystrixCommandKey key = HystrixCommandKey.Factory.asKey(commandKey);
        HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(key);
        
        if (circuitBreaker != null) {
            info.put("isOpen", circuitBreaker.isOpen());
            info.put("allowRequest", circuitBreaker.allowRequest());
        } else {
            info.put("isOpen", "N/A");
            info.put("allowRequest", "N/A");
        }
        
        return info;
    }
}