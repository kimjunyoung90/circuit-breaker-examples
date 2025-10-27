package com.example.service;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 외부 API 호출을 시뮬레이션하며 Hystrix 데모 시나리오를 제공
 */
@Component
public class OuterService {

    private final Random random = new Random();

    /**
     * 항상 실패하는 API 호출 시뮬레이션 (Circuit Breaker 테스트용)
     */
    public String callFailingApi() {
        simulateDelay(50, 200);

        // 매우 낮은 확률로만 성공 (5%)
        if (random.nextInt(100) < 5) {
            return "{\"message\":\"Miracle! Failing API succeeded\", \"timestamp\":" + System.currentTimeMillis() + "}";
        } else {
            throw new RuntimeException("Failing API intentionally failed");
        }
    }

    /**
     * 느린 API 호출 시뮬레이션 (타임아웃 테스트용)
     */
    public String callSlowApi() {
        simulateDelay(3000, 6000); // 3-6초 지연 (타임아웃 초과)
        return "{\"message\":\"Slow API response\", \"processingTime\":\"" + (3000 + random.nextInt(3000)) + "ms\", \"timestamp\":" + System.currentTimeMillis() + "}";
    }

    /**
     * 네트워크 지연 시뮬레이션을 위한 헬퍼 메서드
     */
    private void simulateDelay(int minMs, int maxMs) {
        try {
            int delay = minMs + random.nextInt(maxMs - minMs);
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API call interrupted", e);
        }
    }

}
