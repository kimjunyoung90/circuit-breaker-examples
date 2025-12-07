package com.example.service;

import org.springframework.stereotype.Component;

/**
 * 외부 API 호출을 시뮬레이션하며 Hystrix 데모 시나리오를 제공
 */
@Component
public class ExternalService {

    /**
     * 1. 정상적인 외부 API 응답
     */
    public String callNormalExternalApi() {
        simulateDelay(100);
        return "External API Response: Success";
    }

    /**
     * 2. 느린 외부 API 응답 (3초 지연)
     */
    public String callSlowExternalApi() {
        simulateDelay(3000);
        return "External API Response: Slow (3s)";
    }

    /**
     * 3. 실패하는 외부 API
     */
    public String callFailingExternalApi() {
        simulateDelay(100);
        throw new RuntimeException("External Service Failure: 503 Service Unavailable");
    }

    private void simulateDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API call interrupted", e);
        }
    }
}
