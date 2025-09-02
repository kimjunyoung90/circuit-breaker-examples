package com.example.legacy.controller;

import com.example.legacy.service.ExternalApiService;
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
@RequestMapping("/hystrix-test")
public class CircuitBreakerTestController {

    @Autowired
    private ExternalApiService externalApiService;

    /**
     * 사용자 데이터 조회 (정상 케이스)
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getUserData(@PathVariable String userId) {
        try {
            String result = externalApiService.getUserData(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 프로필 데이터 조회 (느린 응답)
     */
    @RequestMapping(value = "/profile/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getProfileData(@PathVariable String userId) {
        try {
            String result = externalApiService.getProfileData(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 랜덤 데이터 조회 (50% 확률로 실패)
     */
    @RequestMapping(value = "/random", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getRandomData() {
        try {
            String result = externalApiService.getRandomData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 항상 실패하는 API (Circuit Breaker 테스트용)
     */
    @RequestMapping(value = "/failing", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getFailingData() {
        try {
            String result = externalApiService.getFailingData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 느린 API (타임아웃 테스트용)
     */
    @RequestMapping(value = "/slow", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> getSlowData() {
        try {
            String result = externalApiService.getSlowData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * Circuit Breaker 상태 확인
     */
    @RequestMapping(value = "/circuit-status", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCircuitBreakerStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 각 Command의 Circuit Breaker 상태 확인
        status.put("getUserData", getCircuitBreakerInfo("getUserData"));
        status.put("getProfileData", getCircuitBreakerInfo("getProfileData"));
        status.put("getRandomData", getCircuitBreakerInfo("getRandomData"));
        status.put("getFailingData", getCircuitBreakerInfo("getFailingData"));
        status.put("getSlowData", getCircuitBreakerInfo("getSlowData"));
        
        return ResponseEntity.ok(status);
    }

    /**
     * Circuit Breaker 리셋 (강제로 CLOSED 상태로 변경)
     */
    @RequestMapping(value = "/circuit-reset/{commandKey}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<String> resetCircuitBreaker(@PathVariable String commandKey) {
        try {
            HystrixCommandKey key = HystrixCommandKey.Factory.asKey(commandKey);
            HystrixCircuitBreaker circuitBreaker = HystrixCircuitBreaker.Factory.getInstance(key);
            
            if (circuitBreaker != null) {
                // Circuit Breaker를 강제로 닫기 (실제로는 새로운 요청을 통해 자연스럽게 복구되어야 함)
                return ResponseEntity.ok("{\"message\":\"Circuit breaker reset attempted for " + commandKey + "\"}");
            } else {
                return ResponseEntity.badRequest().body("{\"error\":\"Circuit breaker not found for " + commandKey + "\"}");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /**
     * 부하 테스트용 엔드포인트
     */
    @RequestMapping(value = "/load-test/{commandType}/{count}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> loadTest(@PathVariable String commandType, @PathVariable int count) {
        Map<String, Object> results = new HashMap<>();
        int successCount = 0;
        int failureCount = 0;
        int fallbackCount = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < count; i++) {
            try {
                String result = null;
                switch (commandType.toLowerCase()) {
                    case "user":
                        result = externalApiService.getUserData("user" + i);
                        break;
                    case "random":
                        result = externalApiService.getRandomData();
                        break;
                    case "failing":
                        result = externalApiService.getFailingData();
                        break;
                    case "slow":
                        result = externalApiService.getSlowData();
                        break;
                    default:
                        return ResponseEntity.badRequest().body(Map.of("error", "Invalid command type: " + commandType));
                }
                
                if (result.contains("\"status\":\"fallback\"")) {
                    fallbackCount++;
                } else {
                    successCount++;
                }
            } catch (Exception e) {
                failureCount++;
            }
        }
        
        long endTime = System.currentTimeMillis();
        
        results.put("commandType", commandType);
        results.put("totalRequests", count);
        results.put("successCount", successCount);
        results.put("failureCount", failureCount);
        results.put("fallbackCount", fallbackCount);
        results.put("executionTimeMs", endTime - startTime);
        results.put("circuitBreakerStatus", getCircuitBreakerInfo(getCommandKeyByType(commandType)));
        
        return ResponseEntity.ok(results);
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
    
    private String getCommandKeyByType(String commandType) {
        switch (commandType.toLowerCase()) {
            case "user": return "getUserData";
            case "random": return "getRandomData";
            case "failing": return "getFailingData";
            case "slow": return "getSlowData";
            default: return "getUserData";
        }
    }
}