# Circuit Breaker 패턴 학습 예제

**초보자를 위한 Circuit Breaker 패턴 이해와 실습 프로젝트**

## 🎯 학습 목표

Circuit Breaker 패턴을 **이해하고 체험**할 수 있는 간단하고 일관된 예제를 제공합니다.

### Circuit Breaker란?
외부 서비스 호출의 **실패를 감지**하고, **자동으로 차단**하여 시스템을 보호하는 디자인 패턴

**3가지 상태:**
- 🟢 **CLOSED**: 정상 상태, 모든 호출 허용
- 🔴 **OPEN**: 실패율 초과로 모든 호출 차단 → Fallback 실행  
- 🟡 **HALF_OPEN**: 제한된 테스트 호출로 서비스 복구 확인

## 📁 프로젝트 구조

```
circuit-breaker-examples/
├── springboot-module/          # Spring Boot 3.2 + Resilience4j
└── spring-legacy-module/       # Spring 4.3 + Hystrix
```

## 🚀 빠른 테스트

### 공통 API 엔드포인트 (동일한 동작)

| 엔드포인트 | 설명 | 예상 동작 |
|-----------|------|----------|
| `GET /api/test/normal/{data}` | 정상 API | 항상 성공 |
| `GET /api/test/random` | 랜덤 API | 50% 확률로 실패 |
| `GET /api/test/failing` | 실패 API | 항상 실패 → Circuit Open |
| `GET /api/test/slow` | 느린 API | 타임아웃 → Fallback |

### 테스트 시나리오

#### 1️⃣ 기본 테스트
```bash
# Spring Boot Module (포트: 8080)
curl http://localhost:8080/api/test/normal/hello

# Spring Legacy Module (포트: 8081) 
curl http://localhost:8081/api/test/normal/hello
```

#### 2️⃣ Circuit Breaker 체험
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

#### 3️⃣ 상태 모니터링
```bash
# Spring Boot: Actuator
curl http://localhost:8080/actuator/circuitbreakers

# Spring Legacy: 상태 확인
curl http://localhost:8081/api/test/circuit-status
```

## 📊 두 모듈 비교

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

## 🎓 초보자 학습 순서

### 1단계: 개념 이해
- README를 통해 Circuit Breaker 패턴 이해
- 3가지 상태 (CLOSED/OPEN/HALF_OPEN) 숙지

### 2단계: 기본 동작 확인
```bash
# 정상 동작 확인 (Circuit Breaker 개입 없음)
curl http://localhost:8080/api/test/normal/test1
```

### 3단계: Fallback 체험
```bash
# 50% 확률로 Fallback 응답 체험
curl http://localhost:8080/api/test/random
```

### 4단계: Circuit Open 체험  
```bash
# 연속 실패로 Circuit Open 유발
for i in {1..5}; do curl http://localhost:8080/api/test/failing; echo ""; done
```

### 5단계: 모니터링 활용
```bash
# 상태 변화 관찰
curl http://localhost:8080/actuator/circuitbreakers
```

### 6단계: 두 모듈 비교
- 같은 API로 두 모듈 모두 테스트
- 설정 방식과 모니터링 차이점 비교

## 🔧 실행 방법

### Spring Boot Module
```bash
cd springboot-module
./gradlew bootRun
# 포트: 8080
```

### Spring Legacy Module  
```bash
cd spring-legacy-module
mvn spring-boot:run
# 또는 톰캣에 배포
# 포트: 8081
```

## 💡 핵심 학습 포인트

### Circuit Breaker 동작 이해
1. **정상 상태**: 모든 호출이 실제 서비스로 전달
2. **실패 누적**: 설정된 실패율 도달하면 Circuit OPEN
3. **Fallback 실행**: Circuit OPEN 시 즉시 Fallback 응답
4. **자동 복구**: 일정 시간 후 HALF_OPEN으로 복구 시도

### 실무 적용 시나리오
- **외부 API 호출**: 결제, 인증, 데이터 조회 서비스
- **데이터베이스 연결**: Connection Pool 고갈 방지
- **마이크로서비스**: 서비스 간 통신 안정성 확보

## 📝 다음 단계

1. **설정값 조정**: 실패율, 대기시간, 윈도우 크기 변경
2. **실제 API 연동**: HTTP Client를 이용한 외부 API 호출  
3. **모니터링 도구**: Prometheus + Grafana 연동
4. **부하 테스트**: JMeter로 대량 요청 테스트

---

**🎉 Circuit Breaker 패턴을 통해 더 안정적인 시스템을 구축해보세요!**