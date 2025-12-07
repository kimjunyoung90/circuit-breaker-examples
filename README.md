# Circuit Breaker 패턴 학습 예제

**초보자를 위한 Circuit Breaker 패턴 이해와 실습 프로젝트**

## 목표

Circuit Breaker 패턴을 **이해하고 체험**할 수 있는 간단하고 일관된 예제를 제공합니다.

### Circuit Breaker란?
외부 서비스 호출의 **실패를 감지**하고, **자동으로 차단**하여 시스템을 보호하는 디자인 패턴

**3가지 상태:**
- 🟢 **CLOSED**: 정상 상태, 모든 호출 허용
- 🔴 **OPEN**: 실패율 초과로 모든 호출 차단 → Fallback 실행  
- 🟡 **HALF_OPEN**: 제한된 테스트 호출로 서비스 복구 확인

## 프로젝트 구조

```
circuit-breaker-examples/
├── config-server/                # Spring Cloud Config Server
├── springboot-resillience4j/   # Spring Boot 3.2 + Resilience4j
└── spring-hystrix/             # Spring 4.3 + Hystrix
```

## 모듈별 상세 설명

각 모듈의 자세한 실행 방법과 설명은 아래 README 링크를 참고하세요.

- **`config-server`**: [README.md](./config-server/README.md)
- **`springboot-resillience4j`**: [README.md](./springboot-resillience4j/README.md)
- **`spring-hystrix`**: [README.md](./spring-hystrix/README.md)

## 빠른 테스트

### 공통 API 엔드포인트 (동일한 동작)

| 엔드포인트 | 설명 | 예상 동작 |
|-----------|------|----------|
| `GET /api/test/normal` | 정상 API | 항상 성공 |
| `GET /api/test/failing` | 실패 API | 항상 실패 → Circuit Open |
| `GET /api/test/slow` | 느린 API | 타임아웃 → Fallback |

### 테스트 시나리오

#### 기본 테스트
```bash
# Spring Boot Module (포트: 8080)
curl http://localhost:8080/api/test/normal

# Spring Legacy Module (포트: 8081) 
curl http://localhost:8081/spring-hystrix/api/test/normal
```

#### Circuit Breaker 체험
```bash
# 실패 API를 여러 번 호출하여 Circuit Open 유발
for i in {1..5}; do
  curl http://localhost:8080/api/test/failing
  echo " - Call $i"
  sleep 1
done
```

**관찰 포인트:**
- 처음 3번: 실제 API 호출 → 실패 → Fallback  
- 4번째부터: Circuit OPEN → 즉시 Fallback (실제 API 호출 안 함)

#### 상태 모니터링
```bash
# Spring Boot: Actuator
curl http://localhost:8080/actuator/circuitbreakers

# Spring Legacy: 상태 확인
curl http://localhost:8081/api/test/circuit-status
```

## 두 모듈 비교

### Spring Boot Module (Modern)
- **기술**: Spring Boot 3.2 + Resilience4j
- **포트**: 8080
- **설정**: YAML 기반 (`application.yml`)
- **모니터링**: Spring Boot Actuator
- **장점**: 가볍고 설정 간단

### Spring Legacy Module (Traditional)  
- **기술**: Spring 4.3 + Hystrix
- **포트**: 8081
- **설정**: 어노테이션 기반 (`@HystrixProperty`)
- **모니터링**: 커스텀 컨트롤러
- **장점**: 세밀한 제어 가능