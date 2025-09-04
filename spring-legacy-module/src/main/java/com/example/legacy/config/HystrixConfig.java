package com.example.legacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;

/**
 * Hystrix Configuration
 * @HystrixCommand 어노테이션 사용을 위한 AOP 설정
 * 개별 Command 설정은 각 서비스의 @HystrixCommand 어노테이션에서 관리
 */
@Configuration
@EnableAspectJAutoProxy
public class HystrixConfig {
    
    /**
     * HystrixCommandAspect Bean을 등록하여 @HystrixCommand 어노테이션 처리
     * 이 Bean이 없으면 @HystrixCommand가 동작하지 않음
     */
    @Bean
    public HystrixCommandAspect hystrixCommandAspect() {
        return new HystrixCommandAspect();
    }
}