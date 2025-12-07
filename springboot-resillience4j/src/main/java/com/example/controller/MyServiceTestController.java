package com.example.controller;

import com.example.service.MyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Circuit Breaker 테스트 컨트롤러
 */
@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class MyServiceTestController {

    private final MyService myService;

    /**
     * 1. 정상 API 호출 (항상 성공)
     */
    @GetMapping("/normal")
    public ResponseEntity<String> callNormalApi() {
        log.info("[테스트] Normal API 호출");

        String response = myService.callNormalApi();

        log.info("[테스트 완료] Normal API - 응답: {}", response);

        return ResponseEntity.ok(response);
    }

    /**
     * 2. 실패 API 호출 (항상 실패)
     */
    @GetMapping("/failing")
    public ResponseEntity<String> callFailingApi() {
        log.info("[테스트] Failing API 호출 (항상 실패)");

        String response = myService.callFailingApi();

        log.info("[테스트 완료] Failing API - 응답: {}", response);

        return ResponseEntity.ok(response);
    }

    /**
     * 3. 느린 API 호출 (3초 지연)
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
