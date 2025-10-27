package com.example.config;

import com.netflix.config.ConfigurationManager;
import com.netflix.config.DynamicConfiguration;
import com.netflix.config.FixedDelayPollingScheduler;
import com.netflix.config.PolledConfigurationSource;
import com.netflix.config.sources.URLConfigurationSource;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;

@Configuration
@EnableAspectJAutoProxy
public class HystrixConfig {
    @PostConstruct
    public void init() {
        //2 가지 방식 중 원하는 방식 택 1
//        loadStaticHystrixConfiguration();
        startDynamicHystrixPolling();
    }

    /**
     * Hystrix 설정을 정적으로 로드합니다.
     * 'hystrix.properties' 파일의 내용을 로드합니다.
     */
    private void loadStaticHystrixConfiguration() {
        try {
            // 설정 파일 명칭이 hystrix가 기본적으로 읽는 config.properties가 아닌 경우 명시적으로 설정을 로드해야함
            // 단, 설정파일 확장자(.properties) 생략
            ConfigurationManager.loadCascadedPropertiesFromResources("hystrix");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Hystrix configuration", e);
        }
    }

    /**
     * Polling 방식으로 Hystrix 설정을 동적으로 로드하고 업데이트합니다.
     * 'hystrix.properties' 파일의 변경을 감지하여 런타임에 적용합니다.
     */
    private void startDynamicHystrixPolling() {
        try {
            // 폴링 방식으로 동적 설정 변경을 위해서는 DynamicConfiguration 사용
            PolledConfigurationSource source = new URLConfigurationSource("classpath:hystrix.properties");
            // 1초 초기 지연 후 5초 간격으로 폴링
            FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(1000, 5000, true);
            DynamicConfiguration configuration = new DynamicConfiguration(source, scheduler);
            ConfigurationManager.install(configuration);
        } catch (Exception e) {
            throw new RuntimeException("Failed to configure dynamic Hystrix configuration", e);
        }
    }

    /**
     * HystrixCommandAspect Bean을 등록하여 @HystrixCommand 어노테이션 처리
     * 이 Bean이 없으면 @HystrixCommand가 동작하지 않음
     */
    @Bean
    public HystrixCommandAspect hystrixCommandAspect() {
        return new HystrixCommandAspect();
    }
}
