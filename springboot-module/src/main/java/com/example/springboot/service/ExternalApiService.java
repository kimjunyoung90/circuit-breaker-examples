package com.example.springboot.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class ExternalApiService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalApiService.class);
    private final Random random = new Random();

    /**
     * 1. 정상적인 API 호출 (항상 성공)
     */
    @CircuitBreaker(name = "normalApi", fallbackMethod = "fallbackNormal")
    public String callNormalApi(String data) {
        logger.info("Calling normal API with data: {}", data);
        String response = "Normal API Response: " + data;
        logger.info("Normal API succeeded: {}", response);
        return response;
    }

    public String fallbackNormal(String data, Exception ex) {
        logger.warn("Normal API fallback called for: {} - Exception: {}", data, ex.getMessage());
        return "Fallback: Cached data for " + data;
    }

    /**
     * 2. 랜덤 실패 API (50% 확률로 실패)
     */
    @CircuitBreaker(name = "randomApi", fallbackMethod = "fallbackRandom")
    public String callRandomApi() {
        logger.info("Calling random API");
        
        // 50% 확률로 실패 시뮬레이션
        if (random.nextBoolean()) {
            logger.error("Random API call failed");
            throw new RuntimeException("Random API failed");
        }
        
        String response = "Random API Response: " + System.currentTimeMillis();
        logger.info("Random API succeeded: {}", response);
        return response;
    }

    public String fallbackRandom(Exception ex) {
        logger.warn("Random API fallback called - Exception: {}", ex.getMessage());
        return "Fallback: Random service temporarily unavailable";
    }

    /**
     * 3. 항상 실패하는 API (Circuit Breaker Open 테스트용)
     */
    @CircuitBreaker(name = "failingApi", fallbackMethod = "fallbackFailing")
    public String callFailingApi() {
        logger.info("Calling failing API");
        logger.error("Failing API always fails");
        throw new RuntimeException("Failing API is always down");
    }

    public String fallbackFailing(Exception ex) {
        logger.warn("Failing API fallback called - Exception: {}", ex.getMessage());
        return "Fallback: Service is under maintenance";
    }

    /**
     * 4. 느린 API (타임아웃 테스트용)
     */
    @CircuitBreaker(name = "slowApi", fallbackMethod = "fallbackSlow")
    public String callSlowApi() {
        logger.info("Calling slow API");
        
        try {
            // 3초 지연 시뮬레이션
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Slow API interrupted");
        }
        
        String response = "Slow API Response: completed after 3 seconds";
        logger.info("Slow API succeeded: {}", response);
        return response;
    }

    public String fallbackSlow(Exception ex) {
        logger.warn("Slow API fallback called - Exception: {}", ex.getMessage());
        return "Fallback: Quick response instead of slow service";
    }
}