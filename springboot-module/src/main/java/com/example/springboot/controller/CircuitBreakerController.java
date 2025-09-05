package com.example.springboot.controller;

import com.example.springboot.service.ExternalApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class CircuitBreakerController {

    @Autowired
    private ExternalApiService externalApiService;

    /**
     * 1. 정상 API 호출 (항상 성공)
     */
    @GetMapping("/normal/{data}")
    public ResponseEntity<String> callNormalApi(@PathVariable String data) {
        try {
            String response = externalApiService.callNormalApi(data);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * 2. 랜덤 API 호출 (50% 실패)
     */
    @GetMapping("/random")
    public ResponseEntity<String> callRandomApi() {
        try {
            String response = externalApiService.callRandomApi();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * 3. 실패 API 호출 (항상 실패)
     */
    @GetMapping("/failing")
    public ResponseEntity<String> callFailingApi() {
        try {
            String response = externalApiService.callFailingApi();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * 4. 느린 API 호출 (3초 지연)
     */
    @GetMapping("/slow")
    public ResponseEntity<String> callSlowApi() {
        try {
            String response = externalApiService.callSlowApi();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error: " + e.getMessage());
        }
    }
}