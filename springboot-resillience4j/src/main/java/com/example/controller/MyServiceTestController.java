package com.example.controller;

import com.example.service.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Circuit Breaker 테스트를 위한 REST API 컨트롤러
 *
 * 주니어 개발자 학습 가이드:
 * 1. 각 엔드포인트를 호출하면서 로그를 관찰하세요
 * 2. Circuit Breaker의 상태 변화를 실시간으로 확인할 수 있습니다
 * 3. Fallback 메서드가 언제 호출되는지 주목하세요
 *
 * 모니터링 방법:
 * - 터미널에서 애플리케이션 로그 확인
 * - Actuator: http://localhost:8080/actuator/circuitbreakers
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class MyServiceTestController {

    private final MyService myService;

    /**
     * 시나리오 1: 정상 동작하는 외부 API 호출
     *
     * 테스트:
     * curl http://localhost:8080/api/test/normal
     *
     * 예상 결과:
     * - 항상 성공
     * - Circuit 상태: CLOSED 유지
     * - 외부 시스템 응답 그대로 반환
     */
    @GetMapping("/normal")
    public ResponseEntity<String> callNormalApi() {
        log.info("[테스트] Normal API 호출");

        String response = myService.callNormalApi();

        log.info("[테스트 완료] Normal API - 응답: {}", response);

        return ResponseEntity.ok(response);
    }

    /**
     * 시나리오 2: 항상 실패하는 외부 API 호출 (Circuit Open 체험)
     *
     * 테스트:
     * for i in {1..5}; do
     *   echo "===== 요청 $i ====="
     *   curl http://localhost:8080/api/test/failing
     *   echo ""
     *   sleep 1
     * done
     *
     * 예상 결과:
     * - 1~3번 요청: 외부 API 호출 → 실패 → Fallback
     * - 4번째 요청: Circuit OPEN → 즉시 Fallback (외부 API 호출 안 함)
     * - 로그에서 "Circuit 열림!" 메시지 확인
     *
     * 핵심 학습 포인트:
     * - Circuit이 OPEN되면 외부 시스템을 호출하지 않음
     * - 장애가 전파되지 않아 시스템 보호
     */
    @GetMapping("/failing")
    public ResponseEntity<String> callFailingApi() {
        log.info("[테스트] Failing API 호출 (항상 실패)");

        String response = myService.callFailingApi();

        log.info("[테스트 완료] Failing API - 응답: {}", response);

        return ResponseEntity.ok(response);
    }

    /**
     * 시나리오 3: 느린 외부 API 호출 (타임아웃 테스트)
     *
     * 테스트:
     * curl http://localhost:8080/api/test/slow
     *
     * 예상 결과:
     * - 외부 API는 3초 소요
     * - Circuit Breaker 타임아웃 1초 설정
     * - 1초 후 타임아웃 → Fallback 응답
     * - 빠른 응답으로 사용자 경험 개선
     *
     * 핵심 학습 포인트:
     * - 느린 외부 서비스가 우리 서비스를 느리게 만들지 않음
     * - 타임아웃으로 빠르게 Fallback 제공
     */
    @GetMapping("/slow")
    public ResponseEntity<String> callSlowApi() {
        log.info("[테스트] Slow API 호출 (3초 지연)");

        long startTime = System.currentTimeMillis();
        String response = myService.callSlowApi();
        long endTime = System.currentTimeMillis();

        log.info("[테스트 완료] Slow API - 응답: {}, 소요 시간: {}ms", response, (endTime - startTime));

        return ResponseEntity.ok(response);
    }
}
