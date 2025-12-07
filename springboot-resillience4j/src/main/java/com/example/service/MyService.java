package com.example.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyService {

    private final ExternalService externalSystem;

    /**
     * 1. 정상적인 API 호출 (항상 성공)
     */
    @CircuitBreaker(name = "normalApi", fallbackMethod = "fallbackNormal")
    public String callNormalApi() {
        return externalSystem.callNormalExternalApi();
    }

    public String fallbackNormal(Exception ex) {
        return "[Fallback] Cached data";
    }

    /**
     * 2. 항상 실패하는 API
     */
    @CircuitBreaker(name = "failingApi", fallbackMethod = "fallbackFailing")
    public String callFailingApi() {
        return externalSystem.callFailingExternalApi();
    }

    public String fallbackFailing(Exception ex) {
        return "[Fallback] Service is under maintenance";
    }

    /**
     * 3. 느린 API (타임아웃 테스트용)
     */
    @CircuitBreaker(name = "slowApi", fallbackMethod = "fallbackSlow")
    public String callSlowApi() {
        return externalSystem.callSlowExternalApi();
    }

    public String fallbackSlow(Exception ex) {
        return "[Fallback] Quick response instead of slow service";
    }
}
