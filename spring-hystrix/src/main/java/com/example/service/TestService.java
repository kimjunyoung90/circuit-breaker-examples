package com.example.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestService {
    @Autowired
    private OuterService outerService;

    /**
     * 1. 정상적인 API 호출 (항상 성공)
     */
    @HystrixCommand(
            commandKey = "callNormalApi",
            groupKey = "NormalService",
            fallbackMethod = "fallbackNormal"
    )
    public String callNormalApi() {
        return "Normal API Response: Success";
    }

    public String fallbackNormal() {
        return "Fallback: Cached data";
    }

    /**
     * 2. 항상 실패하는 API
     */
    @HystrixCommand(
            commandKey = "callFailingApi",
            groupKey = "FailingService",
            fallbackMethod = "fallbackFailing"
    )
    public String callFailingApi() {
        return outerService.callFailingApi();
    }

    public String fallbackFailing() {
        return "Fallback: Service is under maintenance";
    }

    /**
     * 3. 느린 API (타임아웃 테스트용)
     */
    @HystrixCommand(
            commandKey = "callSlowApi",
            groupKey = "SlowService",
            fallbackMethod = "fallbackSlow"
    )
    public String callSlowApi() {
        return outerService.callSlowApi();
    }

    public String fallbackSlow() {
        return "Fallback: Quick response instead of slow service";
    }
}
