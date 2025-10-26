package com.example.controller;

import com.netflix.config.ConfigurationManager;
import com.netflix.hystrix.HystrixCommandKey;
import com.netflix.hystrix.HystrixCommandProperties;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/config")
public class HystrixConfigController {

    /**
     * Hystrix Command 설정 정보 조회
     * config.properties에 정의된 설정 값들을 확인
     */
    @RequestMapping(value = "/{commandKey}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCommandConfig(@PathVariable String commandKey) {
        HystrixCommandKey key = HystrixCommandKey.Factory.asKey(commandKey);
        HystrixCommandProperties.Setter setter = HystrixCommandProperties.Setter(); // 필요 시
        HystrixCommandProperties properties = HystrixPropertiesFactory.getCommandProperties(key, setter);

        Map<String, Object> config = new HashMap<>();

        // Circuit Breaker 설정
        Map<String, Object> circuitBreakerConfig = new HashMap<>();
        circuitBreakerConfig.put("enabled", properties.circuitBreakerEnabled().get());
        circuitBreakerConfig.put("requestVolumeThreshold", properties.circuitBreakerRequestVolumeThreshold().get());
        circuitBreakerConfig.put("sleepWindowInMilliseconds", properties.circuitBreakerSleepWindowInMilliseconds().get());
        circuitBreakerConfig.put("errorThresholdPercentage", properties.circuitBreakerErrorThresholdPercentage().get());
        circuitBreakerConfig.put("forceOpen", properties.circuitBreakerForceOpen().get());
        circuitBreakerConfig.put("forceClosed", properties.circuitBreakerForceClosed().get());
        config.put("circuitBreaker", circuitBreakerConfig);

        // Execution 설정
        Map<String, Object> executionConfig = new HashMap<>();
        executionConfig.put("isolationStrategy", properties.executionIsolationStrategy().get().name());
        executionConfig.put("timeoutEnabled", properties.executionTimeoutEnabled().get());
        executionConfig.put("timeoutInMilliseconds", properties.executionTimeoutInMilliseconds().get());
        executionConfig.put("interruptOnTimeout", properties.executionIsolationThreadInterruptOnTimeout().get());
        executionConfig.put("interruptOnCancel", properties.executionIsolationThreadInterruptOnFutureCancel().get());
        config.put("execution", executionConfig);

        // Fallback 설정
        Map<String, Object> fallbackConfig = new HashMap<>();
        fallbackConfig.put("enabled", properties.fallbackEnabled().get());
        fallbackConfig.put("maxConcurrentRequests", properties.fallbackIsolationSemaphoreMaxConcurrentRequests().get());
        config.put("fallback", fallbackConfig);

        // Metrics 설정
        Map<String, Object> metricsConfig = new HashMap<>();
        metricsConfig.put("rollingStatsTimeInMilliseconds", properties.metricsRollingStatisticalWindowInMilliseconds().get());
        metricsConfig.put("rollingStatsNumBuckets", properties.metricsRollingStatisticalWindowBuckets().get());
        metricsConfig.put("rollingPercentileEnabled", properties.metricsRollingPercentileEnabled().get());
        metricsConfig.put("healthSnapshotIntervalInMilliseconds", properties.metricsHealthSnapshotIntervalInMilliseconds().get());
        config.put("metrics", metricsConfig);

        // Request Context 설정
        Map<String, Object> requestConfig = new HashMap<>();
        requestConfig.put("cacheEnabled", properties.requestCacheEnabled().get());
        requestConfig.put("logEnabled", properties.requestLogEnabled().get());
        config.put("request", requestConfig);

        return ResponseEntity.ok(config);
    }

    /**
     * Hystrix Command 설정 동적 변경
     * Runtime에 Circuit Breaker, Timeout 등의 설정을 변경
     *
     * @param commandKey Hystrix Command 이름
     * @param configMap 변경할 설정 값들
     * @return 변경 결과
     */
    @RequestMapping(value = "/{commandKey}", method = RequestMethod.PUT)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCommandConfig(
            @PathVariable String commandKey,
            @RequestBody Map<String, Object> configMap) {

        Map<String, Object> result = new HashMap<>();
        Map<String, String> updatedProperties = new HashMap<>();

        try {
            // Circuit Breaker 설정
            if (configMap.containsKey("circuitBreaker")) {
                Map<String, Object> cbConfig = (Map<String, Object>) configMap.get("circuitBreaker");

                if (cbConfig.containsKey("requestVolumeThreshold")) {
                    String key = "hystrix.command." + commandKey + ".circuitBreaker.requestVolumeThreshold";
                    String value = String.valueOf(cbConfig.get("requestVolumeThreshold"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }

                if (cbConfig.containsKey("errorThresholdPercentage")) {
                    String key = "hystrix.command." + commandKey + ".circuitBreaker.errorThresholdPercentage";
                    String value = String.valueOf(cbConfig.get("errorThresholdPercentage"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }

                if (cbConfig.containsKey("sleepWindowInMilliseconds")) {
                    String key = "hystrix.command." + commandKey + ".circuitBreaker.sleepWindowInMilliseconds";
                    String value = String.valueOf(cbConfig.get("sleepWindowInMilliseconds"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }

                if (cbConfig.containsKey("enabled")) {
                    String key = "hystrix.command." + commandKey + ".circuitBreaker.enabled";
                    String value = String.valueOf(cbConfig.get("enabled"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }
            }

            // Execution 설정
            if (configMap.containsKey("execution")) {
                Map<String, Object> execConfig = (Map<String, Object>) configMap.get("execution");

                if (execConfig.containsKey("timeoutInMilliseconds")) {
                    String key = "hystrix.command." + commandKey + ".execution.isolation.thread.timeoutInMilliseconds";
                    String value = String.valueOf(execConfig.get("timeoutInMilliseconds"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }

                if (execConfig.containsKey("timeoutEnabled")) {
                    String key = "hystrix.command." + commandKey + ".execution.timeout.enabled";
                    String value = String.valueOf(execConfig.get("timeoutEnabled"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }
            }

            // Fallback 설정
            if (configMap.containsKey("fallback")) {
                Map<String, Object> fbConfig = (Map<String, Object>) configMap.get("fallback");

                if (fbConfig.containsKey("enabled")) {
                    String key = "hystrix.command." + commandKey + ".fallback.enabled";
                    String value = String.valueOf(fbConfig.get("enabled"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }
            }

            // Metrics 설정
            if (configMap.containsKey("metrics")) {
                Map<String, Object> metricsConfig = (Map<String, Object>) configMap.get("metrics");

                if (metricsConfig.containsKey("rollingStatsTimeInMilliseconds")) {
                    String key = "hystrix.command." + commandKey + ".metrics.rollingStats.timeInMilliseconds";
                    String value = String.valueOf(metricsConfig.get("rollingStatsTimeInMilliseconds"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }

                if (metricsConfig.containsKey("healthSnapshotIntervalInMilliseconds")) {
                    String key = "hystrix.command." + commandKey + ".metrics.healthSnapshot.intervalInMilliseconds";
                    String value = String.valueOf(metricsConfig.get("healthSnapshotIntervalInMilliseconds"));
                    ConfigurationManager.getConfigInstance().setProperty(key, value);
                    updatedProperties.put(key, value);
                }
            }

            result.put("success", true);
            result.put("commandKey", commandKey);
            result.put("updatedProperties", updatedProperties);
            result.put("message", "Configuration updated successfully. Note: New settings apply to new command instances.");

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}