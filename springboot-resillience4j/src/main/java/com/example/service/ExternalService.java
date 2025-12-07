package com.example.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 외부 서비스 API 호출을 시뮬레이션하는 클래스
 * 실제 운영 환경에서는 HTTP Client, Feign Client 등을 사용하여 외부 API를 호출합니다.
 *
 * 이 클래스는 교육 목적으로 다음 3가지 시나리오를 시뮬레이션합니다:
 * 1. 정상 응답 (NORMAL)
 * 2. 느린 응답 (SLOW) - 타임아웃 발생 가능
 * 3. 실패 응답 (FAILURE) - 서비스 다운 상황
 */
@Slf4j
@Component
public class ExternalService {

    /**
     * 시나리오 1: 정상적인 외부 API 응답
     * - 실제 환경: 외부 서비스가 정상 동작 중
     * - 예시: 결제 시스템, 재고 시스템 등이 정상 응답
     */
    public String callNormalExternalApi() {
        log.info("[외부 API 호출] 정상 API 호출 시작");

        // 실제로는 여기서 HTTP 요청을 보냅니다
        // 예: restTemplate.getForObject("https://external-api.com/data", String.class)

        simulateNetworkDelay(100); // 정상적인 네트워크 지연 (100ms)

        String response = "외부 시스템 응답: 정상 처리 완료";
        log.info("[외부 API 응답] 정상 응답 수신 - {}", response);

        return response;
    }

    /**
     * 시나리오 2: 느린 외부 API 응답 (타임아웃 테스트용)
     * - 실제 환경: 외부 서비스 과부하, 네트워크 지연 등
     * - Circuit Breaker의 타임아웃 설정을 초과하면 실패로 간주
     */
    public String callSlowExternalApi() {
        log.warn("[외부 API 호출] 느린 API 호출 시작 - 3초 지연 예상");

        // 실제로는 여기서 응답이 느린 외부 API를 호출합니다
        // 예: 과부하 상태의 레거시 시스템, 느린 데이터베이스 쿼리 등

        simulateNetworkDelay(3000); // 3초 지연 (타임아웃 발생 시나리오)

        String response = "외부 시스템 응답: 느린 처리 완료 (3초 소요)";
        log.info("[외부 API 응답] 느린 응답 수신 - {}", response);

        return response;
    }

    /**
     * 시나리오 3: 실패하는 외부 API (Circuit Open 테스트용)
     * - 실제 환경: 외부 서비스 장애, 네트워크 오류, 500 에러 등
     * - 이런 실패가 반복되면 Circuit Breaker가 OPEN 상태로 전환
     */
    public String callFailingExternalApi() {
        log.error("[외부 API 호출] 실패 API 호출 시작");

        // 실제로는 여기서 장애가 발생한 외부 API를 호출합니다
        // 예: HTTP 500 에러, 네트워크 타임아웃, 연결 거부 등

        simulateNetworkDelay(100);

        log.error("[외부 API 에러] 외부 시스템 장애 발생 - 503 Service Unavailable");

        // 실제 환경에서 발생할 수 있는 예외들:
        // - RestClientException (HTTP 통신 실패)
        // - TimeoutException (타임아웃)
        // - IOException (네트워크 오류)
        throw new RuntimeException("외부 시스템 장애: 서비스 이용 불가 (503 Service Unavailable)");
    }

    /**
     * 네트워크 지연 시뮬레이션
     * 실제 외부 API 호출 시 발생하는 네트워크 레이턴시를 재현합니다.
     */
    private void simulateNetworkDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("네트워크 지연 중 인터럽트 발생", e);
        }
    }
}
