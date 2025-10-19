package com.example.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    /**
     * 모든 Circuit Breaker의 상세 메트릭 정보
     */
    @GetMapping("/circuit-breakers")
    public ResponseEntity<Map<String, Object>> getAllCircuitBreakerMetrics() {
        Map<String, Object> allMetrics = new HashMap<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> metrics = new HashMap<>();
            CircuitBreaker.Metrics cbMetrics = cb.getMetrics();
            
            // 기본 상태 정보
            metrics.put("state", cb.getState().toString());
            metrics.put("config", getCircuitBreakerConfig(cb));
            
            // 상세 메트릭
            Map<String, Object> detailedMetrics = new HashMap<>();
            detailedMetrics.put("failureRate", cbMetrics.getFailureRate());
            detailedMetrics.put("slowCallRate", cbMetrics.getSlowCallRate());
            detailedMetrics.put("numberOfBufferedCalls", cbMetrics.getNumberOfBufferedCalls());
            detailedMetrics.put("numberOfFailedCalls", cbMetrics.getNumberOfFailedCalls());
            detailedMetrics.put("numberOfSuccessfulCalls", cbMetrics.getNumberOfSuccessfulCalls());
            detailedMetrics.put("numberOfNotPermittedCalls", cbMetrics.getNumberOfNotPermittedCalls());
            detailedMetrics.put("numberOfSlowCalls", cbMetrics.getNumberOfSlowCalls());
            detailedMetrics.put("numberOfSlowFailedCalls", cbMetrics.getNumberOfSlowFailedCalls());
            detailedMetrics.put("numberOfSlowSuccessfulCalls", cbMetrics.getNumberOfSlowSuccessfulCalls());
            
            metrics.put("metrics", detailedMetrics);
            allMetrics.put(cb.getName(), metrics);
        });
        
        return ResponseEntity.ok(allMetrics);
    }

    /**
     * Circuit Breaker 설정 정보 조회를 위한 간단한 헬퍼 메소드
     */
    private Map<String, Object> getCircuitBreakerConfig(CircuitBreaker circuitBreaker) {
        Map<String, Object> config = new HashMap<>();
        
        // 주요 설정 정보만 포함 (실제 설정 객체에 접근하는 것이 제한적이므로 기본적인 정보만 제공)
        config.put("name", circuitBreaker.getName());
        config.put("description", "Circuit breaker configuration for " + circuitBreaker.getName());
        
        return config;
    }

    /**
     * 시스템 전체 상태 요약
     */
    @GetMapping("/health-summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        Map<String, Object> summary = new HashMap<>();
        
        long totalCircuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers().stream().count();
        long openCircuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .mapToLong(cb -> cb.getState() == CircuitBreaker.State.OPEN ? 1 : 0)
                .sum();
        
        long halfOpenCircuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .mapToLong(cb -> cb.getState() == CircuitBreaker.State.HALF_OPEN ? 1 : 0)
                .sum();
        
        long closedCircuitBreakers = circuitBreakerRegistry.getAllCircuitBreakers()
                .stream()
                .mapToLong(cb -> cb.getState() == CircuitBreaker.State.CLOSED ? 1 : 0)
                .sum();
        
        summary.put("totalCircuitBreakers", totalCircuitBreakers);
        summary.put("openCircuitBreakers", openCircuitBreakers);
        summary.put("halfOpenCircuitBreakers", halfOpenCircuitBreakers);
        summary.put("closedCircuitBreakers", closedCircuitBreakers);
        summary.put("systemHealth", openCircuitBreakers == 0 ? "HEALTHY" : "DEGRADED");
        
        return ResponseEntity.ok(summary);
    }
}