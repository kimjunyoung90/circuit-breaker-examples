package com.example.config;

import com.netflix.config.*;
import com.netflix.config.sources.URLConfigurationSource;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@EnableAspectJAutoProxy
public class HystrixConfig {
    @PostConstruct
    public void init() {
        try {

            //설정 파일 명칭이 hystrix가 기본적으로 읽는 config.properties가 아닌 경우 명시적으로 설정을 로드해야함
            // 단, 설정파일 확장자(.properties) 생략
//            ConfigurationManager.loadCascadedPropertiesFromResources("config");


            //스케쥴링은 명시적으로 선언해야함.
            PolledConfigurationSource source= new URLConfigurationSource("classpath:config.properties");
            FixedDelayPollingScheduler scheduler = new FixedDelayPollingScheduler(1000, 5000, true);

            DynamicConfiguration configuration = new DynamicConfiguration(source, scheduler);
            ConfigurationManager.install(configuration);

        } catch (Exception e) {
            throw new RuntimeException(e);
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