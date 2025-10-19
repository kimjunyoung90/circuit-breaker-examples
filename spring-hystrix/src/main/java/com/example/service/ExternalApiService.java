package com.example.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalApiService {
    @Autowired
    private MockApiClient mockApiClient;

    /**
     * 1. 정상적인 API 호출 (항상 성공)
     */
    @HystrixCommand(
            commandKey = "callNormalApi",
            groupKey = "NormalService",
            fallbackMethod = "fallbackNormal",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")
            }
    )
    public String callNormalApi(String data) {
        return "Normal API Response: " + data;
    }

    public String fallbackNormal(String data) {
        return "Fallback: Cached data for " + data;
    }


    /**
     * 2. 랜덤 실패 API (50% 확률로 실패)
     */
    @HystrixCommand(
            commandKey = "callRandomApi",
            groupKey = "RandomService", 
            fallbackMethod = "fallbackRandom",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")
            }
    )
    public String callRandomApi() {
        return mockApiClient.callRandomApi();
    }

    public String fallbackRandom() {
        return "Fallback: Random service temporarily unavailable";
    }

    /**
     * 3. 항상 실패하는 API (Circuit Breaker Open 테스트용)
     */
    @HystrixCommand(
            commandKey = "callFailingApi",
            groupKey = "FailingService",
            fallbackMethod = "fallbackFailing",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "30"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")
            }
    )
    public String callFailingApi() {
        return mockApiClient.callFailingApi();
    }

    public String fallbackFailing() {
        return "Fallback: Service is under maintenance";
    }

    /**
     * 4. 느린 API (타임아웃 테스트용)
     */
    @HystrixCommand(
            commandKey = "callSlowApi",
            groupKey = "SlowService",
            fallbackMethod = "fallbackSlow",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000")
            }
    )
    public String callSlowApi() {
        return mockApiClient.callSlowApi();
    }

    public String fallbackSlow() {
        return "Fallback: Quick response instead of slow service";
    }
}
