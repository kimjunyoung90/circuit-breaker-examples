package com.example.legacy.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalApiService {
    @Autowired
    private MockApiClient mockApiClient;

    /**
     * 빠른 응답이 필요한 외부 API 호출
     */
    @HystrixCommand(
            commandKey = "getUserData",
            groupKey = "UserService",
            fallbackMethod = "getUserDataFallback",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "5"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")
            }
    )
    public String getUserData(String userId) {
        return mockApiClient.callUserApi(userId);
    }

    public String getUserDataFallback(String userId) {
        return "{\"userId\":\"" + userId + "\", \"name\":\"Unknown User\", \"status\":\"fallback\"}";
    }

    /**
     * 느린 외부 API 호출
     */
    @HystrixCommand(
            commandKey = "getProfileData",
            groupKey = "ProfileService",
            fallbackMethod = "getProfileDataFallback",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "15000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "5000")
            }
    )
    public String getProfileData(String userId) {
        return mockApiClient.callProfileApi(userId);
    }

    public String getProfileDataFallback(String userId) {
        return "{\"userId\":\"" + userId + "\", \"profile\":\"Default Profile\", \"status\":\"fallback\"}";
    }

    /**
     * 랜덤하게 실패하는 외부 API 호출 (테스트용)
     */
    @HystrixCommand(
            commandKey = "getRandomData",
            groupKey = "RandomService",
            fallbackMethod = "getRandomDataFallback",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "4"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "40"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "8000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
            }
    )
    public String getRandomData() {
        return mockApiClient.callRandomApi();
    }

    public String getRandomDataFallback() {
        return "{\"message\":\"Random service is currently unavailable\", \"status\":\"fallback\", \"timestamp\":\"" + System.currentTimeMillis() + "\"}";
    }

    /**
     * 항상 실패하는 API 호출 (Circuit Breaker 테스트용)
     */
    @HystrixCommand(
            commandKey = "getFailingData",
            groupKey = "FailingService",
            fallbackMethod = "getFailingDataFallback",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "2"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "30"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "5000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "1000")
            }
    )
    public String getFailingData() {
        return mockApiClient.callFailingApi();
    }

    public String getFailingDataFallback() {
        return "{\"message\":\"Failing service fallback response\", \"status\":\"fallback\", \"error\":\"Service intentionally failed\"}";
    }

    /**
     * 타임아웃 테스트용 API 호출
     */
    @HystrixCommand(
            commandKey = "getSlowData",
            groupKey = "SlowService",
            fallbackMethod = "getSlowDataFallback",
            commandProperties = {
                    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "3"),
                    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "50"),
                    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "12000"),
                    @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "2000")
            }
    )
    public String getSlowData() {
        return mockApiClient.callSlowApi();
    }

    public String getSlowDataFallback() {
        return "{\"message\":\"Slow service timed out\", \"status\":\"fallback\", \"timeout\":\"2000ms\"}";
    }
}
