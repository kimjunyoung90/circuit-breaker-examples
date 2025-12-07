package com.example.service;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Circuit Breaker 패턴을 적용한 외부 API 호출 서비스
 *
 * 🔄 Circuit Breaker 동작 흐름:
 * 1. CLOSED (정상) → 모든 요청이 외부 API로 전달됨
 * 2. 실패율이 임계값을 초과하면 → OPEN (차단)
 * 3. OPEN (차단) → 모든 요청이 즉시 차단되고 Fallback 실행 (외부 API 호출 X)
 * 4. 일정 시간 후 → HALF_OPEN (반개방)
 * 5. HALF_OPEN → 제한된 요청으로 테스트, 성공하면 CLOSED로 복구
 *
 * 📚 학습 포인트:
 * - @CircuitBreaker 어노테이션으로 보호 대상 메서드 지정
 * - fallbackMethod로 실패 시 대체 로직 정의
 * - 외부 시스템 장애 시 우리 시스템 보호 (cascading failure 방지)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MyService {

    private final ExternalService externalSystem;

    // ============================================================================
    // 시나리오 1: 정상 응답 (Circuit Breaker 정상 동작 확인)
    // ============================================================================

    /**
     * 정상적으로 동작하는 외부 API 호출
     *
     * Circuit Breaker 상태: CLOSED (정상)
     * - 모든 요청이 외부 시스템으로 전달됩니다
     * - 실패가 없으므로 Circuit은 계속 CLOSED 상태 유지
     *
     * 테스트: curl http://localhost:8080/api/test/normal
     */
    @CircuitBreaker(name = "normalApi", fallbackMethod = "fallbackNormal")
    public String callNormalApi() {
        log.info("[Circuit: CLOSED] Normal API 호출 시작");

        // 실제 외부 시스템 호출 (정상 동작)
        String response = externalSystem.callNormalExternalApi();

        log.info("[Circuit: CLOSED] Normal API 성공");
        return response;
    }

    /**
     * Normal API Fallback 메서드
     *
     * 호출 시점:
     * - 외부 API 호출 실패 시
     * - Circuit이 OPEN 상태일 때
     */
    public String fallbackNormal(Exception ex) {
        log.warn("[Fallback 실행] Normal API 실패 - Fallback 응답");
        log.warn("  원인: {}", ex.getMessage());

        // Fallback 전략: 캐시된 데이터나 기본값 반환
        return "[Fallback] 기본 응답 데이터";
    }

    // ============================================================================
    // 시나리오 2: 느린 응답 (타임아웃으로 인한 Circuit Open)
    // ============================================================================

    /**
     * 응답이 느린 외부 API 호출 (3초 지연)
     *
     * Circuit Breaker 동작:
     * 1. 처음: 요청이 외부 시스템으로 전달되지만 3초 지연
     * 2. 타임아웃 설정(1초)을 초과하면 실패로 간주
     * 3. 반복되면 Circuit이 OPEN → 이후 요청은 즉시 Fallback
     *
     * 테스트: curl http://localhost:8080/api/test/slow
     */
    @CircuitBreaker(name = "slowApi", fallbackMethod = "fallbackSlow")
    public String callSlowApi() {
        log.info("[Circuit: CLOSED] Slow API 호출 시작 (3초 소요 예정)");

        // 외부 시스템 호출 (3초 지연 - 타임아웃 발생 가능)
        String response = externalSystem.callSlowExternalApi();

        log.info("[Circuit: CLOSED] Slow API 성공 (3초 후)");
        return response;
    }

    /**
     * Slow API Fallback 메서드
     *
     * 호출 시점:
     * - 타임아웃 발생 시 (1초 초과)
     * - Circuit이 OPEN 상태일 때
     */
    public String fallbackSlow(Exception ex) {
        if (ex instanceof CallNotPermittedException) {
            log.error("[Circuit: OPEN] Slow API - Circuit이 열려있어 즉시 차단됨");
        } else {
            log.warn("[Timeout] Slow API - 타임아웃 발생, Fallback 응답");
        }
        log.warn("  원인: {}", ex.getMessage());

        // Fallback 전략: 빠른 대체 응답 제공
        return "[Fallback] 빠른 대체 응답 (외부 시스템 느림)";
    }

    // ============================================================================
    // 시나리오 3: 계속 실패 (Circuit Open 체험용)
    // ============================================================================

    /**
     * 항상 실패하는 외부 API 호출
     *
     * Circuit Breaker 동작 과정:
     * 1. 1~3번 요청: CLOSED 상태 → 외부 API 호출 → 실패 → Fallback
     * 2. 실패율 30% 초과 감지 → Circuit이 OPEN으로 전환
     * 3. 4번째 요청부터: OPEN 상태 → 외부 API 호출 안 함 → 즉시 Fallback
     * 4. 10초 대기 후: HALF_OPEN → 테스트 요청으로 복구 시도
     *
     * 테스트:
     * for i in {1..5}; do
     *   curl http://localhost:8080/api/test/failing
     *   echo ""
     *   sleep 1
     * done
     */
    @CircuitBreaker(name = "failingApi", fallbackMethod = "fallbackFailing")
    public String callFailingApi() {
        log.info("[Circuit: CLOSED] Failing API 호출 시작");

        // 외부 시스템 호출 (항상 실패)
        String response = externalSystem.callFailingExternalApi();

        // 이 라인은 실행되지 않음 (위에서 예외 발생)
        log.info("[Circuit: CLOSED] Failing API 성공 (도달 불가)");
        return response;
    }

    /**
     * Failing API Fallback 메서드
     *
     * 중요: 로그를 통해 Circuit 상태 확인
     * - CallNotPermittedException: Circuit이 OPEN 상태 (외부 API 호출 안 함)
     * - 기타 Exception: 외부 API 호출했지만 실패
     */
    public String fallbackFailing(Exception ex) {
        if (ex instanceof CallNotPermittedException) {
            log.error("[Circuit: OPEN] Failing API - Circuit 열림! 외부 API 호출 차단됨");
            log.error("  Circuit이 열려서 요청이 즉시 차단되었습니다");
            log.error("  10초 후 HALF_OPEN 상태로 전환되어 복구를 시도합니다");
        } else {
            log.error("[Circuit: CLOSED] Failing API - 외부 API 호출 실패");
            log.error("  원인: {}", ex.getMessage());
            log.error("  실패가 반복되면 Circuit이 OPEN으로 전환됩니다");
        }

        // Fallback 전략: 서비스 점검 안내 메시지
        return "[Fallback] 서비스 점검 중 - 잠시 후 다시 이용해주세요";
    }

}
