# Resilience4j를 이용한 서킷 브레이커 예제 (Spring Boot)

**Spring Boot 3.2와 Resilience4j를 사용하여 서킷 브레이커 패턴을 쉽게 이해하고 실습하는 프로젝트입니다.**

## 🎯 이 프로젝트의 목표
서킷 브레이커의 세 가지 상태(**CLOSED**, **OPEN**, **HALF_OPEN**)가 어떻게 동작하는지 직접 체험하고, 실제 프로젝트에 어떻게 적용할 수 있는지 감을 잡는 것을 목표로 합니다.

- 🟢 **CLOSED**: 정상 상태. 모든 요청을 정상적으로 처리합니다.
- 🔴 **OPEN**: 임계치 이상의 실패가 감지된 상태. 추가 요청을 즉시 차단하고 Fallback 응답을 반환하여 시스템을 보호합니다.
- 🟡 **HALF_OPEN**: OPEN 상태에서 일정 시간이 지난 후, 서비스가 복구되었는지 확인하기 위해 제한된 테스트 요청을 보내는 상태입니다.

---

## 🚀 빠른 시작

이 프로젝트는 `config-server`를 통해 설정을 동적으로 관리합니다. 따라서 `config-server`를 먼저 실행해야 합니다.

### 1단계: Config Server 실행
새 터미널을 열고 `config-server` 디렉토리로 이동하여 아래 명령어를 실행합니다.
```bash
# /config-server/
./gradlew bootRun
```
Config Server가 `http://localhost:8888`에서 실행됩니다.

### 2단계: 애플리케이션 실행
이제 `springboot-resillience4j` 애플리케이션을 실행합니다. (Java 17 이상 필요)

```bash
# /springboot-resillience4j/
./gradlew bootRun
```
애플리케이션이 `http://localhost:8080`에서 실행됩니다.

---

## 💡 서킷 브레이커 시나리오별 실습

프로젝트가 제공하는 3가지 API를 통해 서킷 브레이커의 동작을 단계별로 실습합니다.

### 1단계: 정상 호출 (✅ CLOSED 상태)
가장 기본적인 성공 사례입니다. 서킷 브레이커가 개입하지 않고 API가 정상적으로 응답합니다.

```bash
# 'normal' API 호출
curl http://localhost:8080/api/normal
```
**예상 결과:**
```
External API Response: Success
```

### 2단계: 실패율 증가로 서킷 열기 (🔴 OPEN 상태)
`failing` API는 항상 실패하도록 설정되어 있습니다. 반복적으로 호출하여 실패율 임계치를 넘기면 서킷이 OPEN 상태로 전환되는 것을 확인합니다.

```bash
# 'failing' API 호출
curl http://localhost:8080/api/failing
```

```bash
# 'failing' API를 5번 연속 호출
for i in {1..5}; do
  echo -n "호출 $i: "
  curl http://localhost:8080/api/failing
  echo ""
  sleep 1
done
```
**예상 결과:**
- **처음 1~2번 호출**: API가 실제로 호출되지만 실패하고, `Fallback: Failing service is unavailable` 메시지가 나타납니다.
- **3번째 호출부터**: 실패율이 설정된 임계치(30%)를 초과하여 서킷이 **OPEN** 상태로 변경됩니다. 이후의 호출은 API를 실제로 호출하지 않고 즉시 Fallback을 반환합니다.

### 3단계: 느린 응답으로 서킷 열기 (🔴 OPEN 상태)
`slow` API는 응답 시간이 길어 타임아웃을 유발합니다. Resilience4j는 단순히 실패하는 것뿐만 아니라, 응답이 느린 호출의 비율(`slowCallRateThreshold`)이 임계치를 넘어도 서킷을 OPEN 상태로 전환합니다.

```bash
# 'slow' API 호출
curl http://localhost:8080/api/slow
```

```bash
# 'slow' API를 여러 번 호출하여 느린 호출 비율을 높입니다.
for i in {1..5}; do
  echo -n "호출 $i: "
  curl http://localhost:8080/api/slow
  echo ""
done
```
**예상 결과:**
- 각 호출은 설정된 타임아웃(1초) 이후 Fallback 응답을 반환합니다.
- 느린 호출 비율이 임계치(30%)를 넘으면, 서킷이 OPEN 상태로 전환되어 이후의 호출은 기다리지 않고 즉시 Fallback을 반환합니다.

### 4단계: 서킷 상태 모니터링하기
Actuator 엔드포인트를 통해 현재 모든 서킷 브레이커의 상태와 통계를 실시간으로 확인할 수 있습니다.

```bash
# 모든 서킷 브레이커의 현재 상태 확인
curl http://localhost:8080/actuator/circuitbreakers

# 상세 메트릭 확인
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```
**확인 포인트:**
- `failingApi`와 `slowApi`의 상태가 `CLOSED`에서 `OPEN`으로 바뀌는지 관찰해 보세요.
- `failureRate`, `slowCallRate` 등의 지표가 어떻게 변하는지 확인해 보세요.

---

## ⚙️ 주요 설정 살펴보기 (Config Server)
이 프로젝트는 **Spring Cloud Config Server**를 통해 외부에서 설정을 받아오도록 구성되어 있습니다.
따라서 설정을 변경하며 테스트하고 싶다면 **`config-server` 쪽의 `resilience4j-application.yml` 파일을 수정**해야합니다.

아래는 `config-server`에 설정된 주요 내용입니다.

### 기본 설정 (`default`)
모든 서킷 브레이커에 공통으로 적용되는 기본값입니다.
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        failure-rate-threshold: 50        # 서킷을 OPEN할 실패율 임계치 (50%)
        slow-call-rate-threshold: 50      # 서킷을 OPEN할 느린 호출 비율 임계치 (50%)
        slow-call-duration-threshold: 1000ms # 1초 이상 걸리면 '느린 호출'로 간주
        wait-duration-in-open-state: 10s  # OPEN 상태 유지 시간 (10초)
        sliding-window-type: COUNT_BASED  # 카운트 기반으로 최근 호출 기록
        sliding-window-size: 5            # 실패율을 계산할 최근 호출 횟수 (5번)
        minimum-number-of-calls: 3        # 서킷 상태를 계산하기 위한 최소 호출 횟수 (3번)
        permitted-number-of-calls-in-half-open-state: 2 # HALF_OPEN 상태에서 허용할 테스트 호출 횟수
```

### API별 개별 설정 (`instances`)
특정 API에만 다른 설정을 적용하고 싶을 때 사용합니다.
```yaml
resilience4j:
  circuitbreaker:
    instances:
      failingApi:
        base-config: default
        failure-rate-threshold: 30  # 실패율 30%만 넘어도 서킷 OPEN (더 민감하게)
      slowApi:
        base-config: default
        slow-call-rate-threshold: 30 # 느린 호출 비율 30%만 넘어도 서킷 OPEN
```

---

## 🔧 핵심 구현 코드 (`MyService.java`)
서비스 로직에 `@CircuitBreaker` 어노테이션 하나만 추가하면 간단하게 서킷 브레이커를 적용할 수 있습니다.

```java
@Service
public class MyService {

    // 'failingApi'라는 이름의 서킷 브레이커를 적용합니다.
    // 실패 시 'fallbackFailing' 메소드가 대신 실행됩니다.
    @CircuitBreaker(name = "failingApi", fallbackMethod = "fallbackFailing")
    public String callFailingApi() {
        // 이 메소드는 항상 예외를 발생시킵니다.
        throw new RuntimeException("Always failing");
    }

    // Fallback 메소드: 원본 메소드와 동일한 파라미터를 가지며, 마지막에 예외(Throwable)를 받을 수 있습니다.
    public String fallbackFailing(Throwable t) {
        return "Fallback: Failing service is unavailable";
    }
}
```
---

## 📝 다음 단계
- `config-server`의 `resilience4j-application.yml` 파일에서 임계치(`threshold`) 값을 직접 수정하며 서킷 브레이커의 동작이 어떻게 변하는지 테스트해보세요.
- 실제 외부 API를 호출하는 코드를 추가하여 실제와 유사한 환경을 시뮬레이션해보세요.
- `spring-hystrix` 프로젝트의 예제와 비교하며 Resilience4j와 Hystrix의 차이점을 학습해보세요.
