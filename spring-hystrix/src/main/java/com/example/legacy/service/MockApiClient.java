package com.example.legacy.service;

import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 외부 API 호출을 시뮬레이션하는 Mock 클라이언트
 * Circuit Breaker 테스트를 위한 다양한 시나리오 제공
 */
@Component
public class MockApiClient {

    private final Random random = new Random();

    /**
     * 사용자 API 호출 시뮬레이션 (80% 성공률)
     */
    public String callUserApi(String userId) {
        // 80% 성공률
        if (random.nextInt(100) < 80) {
            simulateDelay(100, 300); // 100-300ms 지연
            return "{\"userId\":\"" + userId + "\", \"name\":\"User " + userId + "\", \"email\":\"" + userId + "@example.com\", \"status\":\"active\"}";
        } else {
            throw new RuntimeException("User API call failed for userId: " + userId);
        }
    }

    /**
     * 프로필 API 호출 시뮬레이션 (느린 응답, 70% 성공률)
     */
    public String callProfileApi(String userId) {
        // 70% 성공률
        if (random.nextInt(100) < 70) {
            simulateDelay(1000, 3000); // 1-3초 지연
            return "{\"userId\":\"" + userId + "\", \"profile\":\"Profile " + userId + "\", \"bio\":\"User bio for " + userId + "\", \"preferences\":{\"theme\":\"dark\",\"language\":\"ko\"}}";
        } else {
            simulateDelay(500, 1500); // 실패해도 지연 발생
            throw new RuntimeException("Profile API call failed for userId: " + userId);
        }
    }

    /**
     * 랜덤 API 호출 시뮬레이션 (50% 성공률)
     */
    public String callRandomApi() {
        // 50% 성공률
        if (random.nextBoolean()) {
            simulateDelay(200, 800);
            int randomValue = random.nextInt(1000);
            return "{\"randomValue\":" + randomValue + ", \"timestamp\":" + System.currentTimeMillis() + ", \"status\":\"success\"}";
        } else {
            simulateDelay(100, 500);
            throw new RuntimeException("Random API call failed with random error");
        }
    }

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
        // 60% 확률로 타임아웃을 초과하는 지연
        if (random.nextInt(100) < 60) {
            simulateDelay(3000, 6000); // 3-6초 지연 (타임아웃 초과)
        } else {
            simulateDelay(500, 1500); // 정상 응답 시간
        }
        
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

    /**
     * 부하 테스트용 특별한 시나리오
     */
    public String callLoadTestApi(String testType, int requestNumber) {
        switch (testType.toLowerCase()) {
            case "burst":
                // 처음 몇 개 요청은 성공, 나머지는 실패
                if (requestNumber <= 3) {
                    simulateDelay(100, 200);
                    return "{\"testType\":\"burst\", \"requestNumber\":" + requestNumber + ", \"status\":\"success\"}";
                } else {
                    simulateDelay(50, 150);
                    throw new RuntimeException("Burst test API failed for request: " + requestNumber);
                }
                
            case "degraded":
                // 점진적으로 성능 저하
                int successRate = Math.max(20, 90 - (requestNumber * 10));
                if (random.nextInt(100) < successRate) {
                    simulateDelay(100 + requestNumber * 50, 200 + requestNumber * 100);
                    return "{\"testType\":\"degraded\", \"requestNumber\":" + requestNumber + ", \"successRate\":" + successRate + "}";
                } else {
                    throw new RuntimeException("Degraded test API failed for request: " + requestNumber);
                }
                
            case "recovery":
                // 점진적으로 복구
                int recoveryRate = Math.min(90, 10 + (requestNumber * 10));
                if (random.nextInt(100) < recoveryRate) {
                    simulateDelay(500 - requestNumber * 20, 1000 - requestNumber * 50);
                    return "{\"testType\":\"recovery\", \"requestNumber\":" + requestNumber + ", \"recoveryRate\":" + recoveryRate + "}";
                } else {
                    throw new RuntimeException("Recovery test API failed for request: " + requestNumber);
                }
                
            default:
                return callRandomApi();
        }
    }
}