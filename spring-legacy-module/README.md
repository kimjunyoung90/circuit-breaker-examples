# Circuit Breaker 예제 - Spring Legacy Module

**Hystrix를 사용한 Spring 4.3 Circuit Breaker 구현 예제**

## 🎯 Circuit Breaker란?

Circuit Breaker는 외부 서비스 호출의 **실패를 감지**하고, **자동으로 차단**하여 시스템을 보호하는 패턴입니다.

**3가지 상태:**
- 🟢 **CLOSED**: 정상 상태, 모든 호출 허용
- 🔴 **OPEN**: 실패율 초과로 모든 호출 차단 → Fallback 실행
- 🟡 **HALF_OPEN**: 제한된 테스트 호출로 서비스 복구 확인

## 🚀 빠른 시작

### 1. 애플리케이션 실행
```bash
mvn spring-boot:run
# 또는 톰캣에 war 배포
# 포트: 8081
```

### 2. 기본 테스트 (포트: 8081)

#### ✅ 정상 API (항상 성공)
```bash
curl http://localhost:8081/api/test/normal/hello
# 응답: "Normal API Response: hello"
```

#### 🎲 랜덤 API (50% 확률로 실패)
```bash
curl http://localhost:8081/api/test/random
# 성공: "Random API Response: 1725533425123" 
# 실패: "Fallback: Random service temporarily unavailable"
```

#### ❌ 실패 API (항상 실패 → Circuit Open 테스트)
```bash
# 3번 연속 호출하여 Circuit Open 유발
for i in {1..5}; do
  curl http://localhost:8081/api/test/failing
  echo ""
  sleep 1
done
# 처음: 실패 → Fallback
# 3번 후: Circuit OPEN → 즉시 Fallback
```

#### ⏱️ 느린 API (타임아웃 테스트)
```bash
curl http://localhost:8081/api/test/slow
# 타임아웃 → Fallback: "Quick response instead of slow service"
```

## 📊 모니터링

### Circuit Breaker 상태 확인
```bash
curl http://localhost:8081/api/test/status
```

## ⚙️ 설정 (Hystrix)

### 어노테이션 기반 설정
```java
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
```

### API별 개별 설정
- **normalApi**: 기본 설정
- **randomApi**: 기본 설정
- **failingApi**: 실패율 30%로 빠르게 Open
- **slowApi**: 1초 타임아웃으로 설정

## 💡 초보자를 위한 학습 순서

### 1단계: 정상 동작 확인
```bash
curl http://localhost:8081/api/test/normal/test1
```
→ Circuit Breaker가 개입하지 않는 정상 케이스

### 2단계: Fallback 체험
```bash
curl http://localhost:8081/api/test/random
```
→ 50% 확률로 Fallback 응답 확인

### 3단계: Circuit Open 체험
```bash
# 실패 API를 여러 번 호출
for i in {1..5}; do curl http://localhost:8081/api/test/failing; echo ""; done
```
→ 처음엔 실제 API 호출 → 실패 누적 → Circuit OPEN → 즉시 Fallback

### 4단계: 상태 모니터링
```bash
curl http://localhost:8081/api/test/status
```
→ Circuit Breaker 상태 변화 관찰

## 🔧 구현 포인트

### Service Layer (Hystrix)
```java
@HystrixCommand(
    commandKey = "callNormalApi",
    fallbackMethod = "fallbackNormal"
)
public String callNormalApi(String data) {
    return "Normal API Response: " + data;
}

public String fallbackNormal(String data) {
    return "Fallback: Cached data for " + data;
}
```

### Controller Layer
```java
@Controller
@RequestMapping("/api/test")
public class CircuitBreakerTestController {
    @RequestMapping(value = "/normal/{data}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<String> callNormalApi(@PathVariable String data) {
        String result = externalApiService.callNormalApi(data);
        return ResponseEntity.ok(result);
    }
}
```

## 🆚 Spring Boot 모듈과의 차이점

| 구분 | Spring Legacy (Hystrix) | Spring Boot (Resilience4j) |
|------|------------------------|-----------------------------|
| 설정 방식 | 어노테이션 속성 | YAML 기반 |
| 의존성 | 무거움 (Netflix OSS) | 가벼움 |
| 상태 | 동일하지만 용어 차이 | CLOSED/OPEN/HALF_OPEN |
| 모니터링 | 커스텀 컨트롤러 | Spring Boot Actuator |
| 성능 | Thread Pool 격리 | 더 가볍고 빠름 |

## 📝 다음 단계

1. **springboot-module**과 API 비교 테스트
2. Hystrix Dashboard 연동
3. 실제 외부 API 연동 시뮬레이션
4. Thread Pool 격리 방식 학습