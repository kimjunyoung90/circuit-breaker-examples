package com.example.legacy.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Hystrix Configuration
 * @HystrixCommand 어노테이션 사용을 위한 AOP 설정
 * 개별 Command 설정은 각 서비스의 @HystrixCommand 어노테이션에서 관리
 */
@Configuration
@EnableAspectJAutoProxy
public class HystrixConfig {
    
    // @HystrixCommand 어노테이션에서 개별 설정을 사용하므로 
    // Bean 설정은 제거하고 AOP 활성화만 유지
}