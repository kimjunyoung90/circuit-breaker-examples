package com.example.controller;

import com.example.service.MyService;
import com.netflix.hystrix.HystrixCircuitBreaker;
import com.netflix.hystrix.HystrixCommandKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class MyServiceController {

    @Autowired
    private MyService myService;

    /**
     * 1. 정상 API 호출 (항상 성공)
     */
    @RequestMapping(value = "/normal", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callNormalApi() {
        try {
            String result = myService.callNormalApi();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * 2. 실패 API 호출 (항상 실패)
     */
    @RequestMapping(value = "/failing", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callFailingApi() {
        try {
            String result = myService.callFailingApi();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    /**
     * 3. 느린 API 호출 (3초 지연)
     */
    @RequestMapping(value = "/slow", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callSlowApi() {
        try {
            String result = myService.callSlowApi();
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
