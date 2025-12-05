# Config Server - Spring Cloud Config

Spring Cloud Config Server를 사용하여 서킷브레이커 설정을 동적으로 관리하는 중앙 설정 서버입니다.

## 🎯 목적

- Circuit Breaker 설정을 중앙에서 관리
- 애플리케이션 재시작 없이 동적으로 설정 변경 가능
- Resilience4j와 Hystrix 설정을 모두 지원

## 📁 프로젝트 구조

```
config-server/
├── src/main/
│   ├── java/com/example/
│   │   └── ConfigServerApplication.java    # Config Server 메인 애플리케이션
│   └── resources/
│       ├── application.yml                  # Config Server 설정
│       └── config/                          # 클라이언트 설정 파일
│           ├── resilience4j-application.yml # Resilience4j 설정
│           └── hystrix-application.properties # Hystrix 설정
├── build.gradle
└── README.md
```

## ⚙️ 설정 파일 설명

### Config Server 설정 (application.yml)

```yaml
server:
  port: 8888  # Config Server 포트

spring:
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config  # 로컬 파일 시스템 경로
  profiles:
    active: native  # Native profile 활성화
```

### 제공되는 설정 파일

#### 1. Resilience4j 설정 (resilience4j-application.yml)
Spring Boot 3.2 + Resilience4j 모듈용 Circuit Breaker 설정

**주요 설정값:**
- `failure-rate-threshold`: 실패율 임계값 (50%)
- `wait-duration-in-open-state`: Circuit Open 상태 대기 시간 (10초)
- `sliding-window-size`: 슬라이딩 윈도우 크기 (5개 호출)
- `minimum-number-of-calls`: 최소 호출 수 (3번)

**인스턴스별 설정:**
- `normalApi`: 기본 설정 사용
- `randomApi`: 기본 설정 사용
- `failingApi`: 30% 실패율로 빠르게 Open
- `slowApi`: 1초 이상 느린 호출 30%로 Open

#### 2. Hystrix 설정 (hystrix-application.properties)
Spring 4.3 + Hystrix 모듈용 Circuit Breaker 설정

**주요 설정값:**
- `requestVolumeThreshold`: 최소 요청 수 (5개)
- `errorThresholdPercentage`: 에러 임계값 (50%)
- `sleepWindowInMilliseconds`: Circuit Open 대기 시간 (10초)
- `timeoutInMilliseconds`: 실행 타임아웃 (3초)

## 🚀 실행 방법

### 1. Config Server 시작

```bash
cd config-server
./gradlew bootRun
```

서버는 `http://localhost:8888`에서 실행됩니다.

### 2. 설정 확인

Config Server가 제공하는 설정을 확인할 수 있습니다:

```bash
# Resilience4j 설정 확인
curl http://localhost:8888/resilience4j-application/default

# Hystrix 설정 확인
curl http://localhost:8888/hystrix-application/default
```

### 3. 설정 구조

Config Server는 다음과 같은 URL 패턴으로 설정을 제공합니다:

```
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{application}-{profile}.properties
```

**예시:**
- `http://localhost:8888/resilience4j-application/default`
- `http://localhost:8888/hystrix-application/default`

## 🔄 클라이언트 설정

### Spring Boot 모듈 (Resilience4j)

`bootstrap.yml` 또는 `application.yml`:

```yaml
spring:
  application:
    name: resilience4j-application
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
  config:
    import: "optional:configserver:http://localhost:8888"
```

### Spring Legacy 모듈 (Hystrix)

`bootstrap.properties`:

```properties
spring.application.name=hystrix-application
spring.cloud.config.uri=http://localhost:8888
spring.cloud.config.fail-fast=true
```

## 📊 설정 동적 변경

### 1. 설정 파일 수정

`config-server/src/main/resources/config/` 디렉토리의 설정 파일을 수정합니다.

### 2. 클라이언트 애플리케이션 리프레시

Spring Cloud Bus 또는 Actuator의 `/refresh` 엔드포인트 사용:

```bash
curl -X POST http://localhost:8080/actuator/refresh
```

## 🌐 Git Repository 사용 (선택사항)

로컬 파일 시스템 대신 Git repository를 사용하려면:

### 1. application.yml 수정

```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/your-username/config-repo
          default-label: main
          search-paths: circuit-breaker-configs
          clone-on-start: true
  profiles:
    active: git  # native에서 git으로 변경
```

### 2. Git Repository 구조

```
config-repo/
└── circuit-breaker-configs/
    ├── resilience4j-application.yml
    └── hystrix-application.properties
```

## 🔍 모니터링

Config Server는 Spring Boot Actuator를 통해 모니터링 가능:

```bash
# Health 체크
curl http://localhost:8888/actuator/health

# Env 확인
curl http://localhost:8888/actuator/env
```

## 💡 사용 시나리오

### 시나리오 1: Circuit Breaker 임계값 조정

**문제:** 실패 API가 너무 빨리 Circuit Open 상태가 됨

**해결:**
1. `resilience4j-application.yml`에서 `failingApi` 설정 수정:
   ```yaml
   failingApi:
     failure-rate-threshold: 60  # 30% → 60%로 증가
   ```
2. 클라이언트 애플리케이션 리프레시

### 시나리오 2: 타임아웃 시간 변경

**문제:** 느린 API의 타임아웃이 너무 짧음

**해결:**
1. `resilience4j-application.yml`에서 `slowApi` 설정 수정:
   ```yaml
   slowApi:
     slow-call-duration-threshold: 3s  # 1s → 3s로 증가
   ```
2. 클라이언트 애플리케이션 리프레시

## 🔐 보안 고려사항

프로덕션 환경에서는:
- Config Server에 인증 추가 (Spring Security)
- 민감한 정보는 암호화 (Spring Cloud Config Encryption)
- HTTPS 사용

```yaml
# 암호화 예시
spring:
  cloud:
    config:
      server:
        encrypt:
          enabled: true
encrypt:
  key: your-encryption-key
```

## 📚 참고 자료

- [Spring Cloud Config Documentation](https://spring.io/projects/spring-cloud-config)
- [Resilience4j Documentation](https://resilience4j.readme.io/)
- [Hystrix Documentation](https://github.com/Netflix/Hystrix/wiki)

## 🎯 다음 단계

1. **Spring Cloud Bus 통합**: 설정 변경 시 자동으로 모든 클라이언트에 알림
2. **암호화 적용**: 민감한 설정값 보호
3. **Git Repository 연동**: 버전 관리 및 이력 추적
4. **프로파일 관리**: dev, staging, production 환경별 설정 분리