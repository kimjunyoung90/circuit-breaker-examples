package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 서비스 API 호출 시뮬레이션
 *  1. 정상 응답 (NORMAL)
 *  2. 느린 응답 (SLOW) - 타임아웃 발생 가능
 *  3. 실패 응답 (FAILURE) - 서비스 다운 상황
 */
@Slf4j
@Component
public class ExternalService {

    /**
     * 1. 정상적인 외부 API 응답
     */
    public String callNormalExternalApi() {
        simulateNetworkDelay(100);
        return "External API Response: Success";
    }

    /**
     * 2. 느린 외부 API 응답 (3초 지연)
     */
    public String callSlowExternalApi() {
        simulateNetworkDelay(3000);
        return "External API Response: Slow (3s)";
    }

    /**
     * 3. 실패하는 외부 API
     */
    public String callFailingExternalApi() {
        simulateNetworkDelay(100);
        throw new RuntimeException("External Service Failure: 503 Service Unavailable");
    }

    private void simulateNetworkDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Network delay interrupted", e);
        }
    }
}
