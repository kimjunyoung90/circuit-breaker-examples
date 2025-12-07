# Hystrix를 이용한 서킷 브레이커 예제 (Spring MVC)

**Spring 4.3 MVC와 Netflix Hystrix를 사용하여 레거시 서블릿 환경에서 서킷 브레이커 패턴을 실습하는 프로젝트입니다.**

## 🎯 이 프로젝트의 목표
Hystrix 서킷 브레이커의 세 가지 상태(**CLOSED**, **OPEN**, **HALF_OPEN**)가 어떻게 동작하는지 직접 체험하고, 동적 설정 변경, 대시보드 연동 등 Hystrix의 주요 기능을 학습하는 것을 목표로 합니다.

- 🟢 **CLOSED**: 정상 상태. 모든 요청을 정상적으로 처리합니다.
- 🔴 **OPEN**: 임계치 이상의 실패가 감지된 상태. 추가 요청을 즉시 차단하고 Fallback 응답을 반환하여 시스템을 보호합니다.
- 🟡 **HALF_OPEN**: OPEN 상태에서 일정 시간이 지난 후, 서비스가 복구되었는지 확인하기 위해 제한된 테스트 요청을 보내는 상태입니다.

---

## 🚀 빠른 시작

이 프로젝트는 Spring Boot가 아닌 전통적인 Spring MVC 프로젝트이므로, 서블릿 컨테이너(예: Tomcat)에 배포하여 실행해야 합니다.

### 1단계: 프로젝트 빌드
Maven을 사용하여 프로젝트를 빌드하고 `.war` 파일을 생성합니다.
```bash
mvn clean package
```
빌드가 성공하면 `target/spring-hystrix.war` 파일이 생성됩니다.

### 2단계: Tomcat 서버에 배포 및 실행
사용하는 IDE(IntelliJ, Eclipse 등)에 내장된 Tomcat 서버를 사용하거나, 별도로 설치된 Tomcat의 `webapps` 디렉토리에 위에서 생성된 `.war` 파일을 배포합니다.

> **IntelliJ 실행 Tip:**
> 1. `Run/Debug Configurations`에서 `Tomcat Server > Local`을 추가합니다.
> 2. `Deployment` 탭에서 `spring-hystrix.war` 아티팩트를 추가합니다.
> 3. `Application context`를 `/spring-hystrix`로 설정하고 서버를 실행합니다.

서버가 정상적으로 실행되면 `http://localhost:8080/spring-hystrix/` 경로로 애플리케이션에 접근할 수 있습니다. (포트는 8080로 설정되어 있습니다.)

---

## 💡 서킷 브레이커 시나리오별 실습

프로젝트가 제공하는 3가지 API를 통해 서킷 브레이커의 동작을 단계별로 실습합니다.

### 1단계: 정상 호출 (✅ CLOSED 상태)
가장 기본적인 성공 사례입니다. 서킷 브레이커가 개입하지 않고 API가 정상적으로 응답합니다.

```bash
# 'normal' API 호출
curl http://localhost:8080/spring-hystrix/api/normal
```
**예상 결과:**
```
Normal API Response
```

### 2단계: 실패율 증가로 서킷 열기 (🔴 OPEN 상태)
`failing` API는 항상 실패합니다. 반복적으로 호출하여 실패율 임계치를 넘기면 서킷이 OPEN 상태로 전환되는 것을 확인합니다.

```bash
# 'falling' API 단건 호출
curl http://localhost:8080/spring-hystrix/api/failing
```

```bash
# 'failing' API를 100번 연속 호출
for i in {1..100}; do
  echo -n "호출 $i: "
  curl http://localhost:8080/spring-hystrix/api/failing
  echo ""
  sleep 1
done
```
**예상 결과:**
- **처음 1~4번 호출**: API가 실제로 호출되지만 실패하고, `Fallback: Failing service is unavailable` 메시지가 나타납니다.
- **5번째 호출부터**: 실패율이 설정된 임계치(`requestVolumeThreshold=5`)를 초과하여 서킷이 **OPEN** 상태로 변경됩니다. 이후의 호출은 API를 실제로 호출하지 않고 즉시 Fallback을 반환합니다.

### 3단계: 느린 응답으로 서킷 열기 (🔴 OPEN 상태)
`slow` API는 응답 시간이 길어 타임아웃을 유발합니다. Hystrix는 설정된 타임아웃(`timeoutInMilliseconds`)을 초과하면 요청을 실패로 간주하고 Fallback을 실행합니다.

```bash
# 'slow' API 호출
curl http://localhost:8080/spring-hystrix/api/slow
```
```bash
# 'slow' API를 100번 연속 호출
for i in {1..100}; do
  echo -n "호출 $i: "
  curl http://localhost:8080/spring-hystrix/api/slow
  echo ""
  sleep 1
done
```
**예상 결과:**
- 설정된 타임아웃(2초) 이후 `Fallback: Slow service is unavailable` 응답을 반환합니다.
- 타임아웃도 실패로 간주되므로, 반복 호출 시 서킷이 OPEN 될 수 있습니다.

### 4단계: 서킷 상태 모니터링하기
이 프로젝트는 서킷의 상태를 확인할 수 있는 2가지 방법을 제공합니다.

**1. Hystrix 스트림으로 실시간 데이터 확인**
```bash
# Hystrix가 제공하는 실시간 메트릭 스트림을 직접 확인
curl http://localhost:8080/spring-hystrix/hystrix.stream
```
이 스트림은 Hystrix Dashboard에 연결하여 시각적으로 모니터링하는 데 사용됩니다. (아래 `Hystrix Dashboard 연동` 참고)

---

## ⚙️ 주요 설정 살펴보기 (`hystrix.properties`)
Hystrix의 주요 동작은 `src/main/resources/hystrix.properties` 파일에서 설정합니다.

```properties
# 기본 설정 (모든 Command에 적용)
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=2000

# failingApi Command 개별 설정
hystrix.command.callFailingApi.circuitBreaker.requestVolumeThreshold=5
hystrix.command.callFailingApi.circuitBreaker.errorThresholdPercentage=50
hystrix.command.callFailingApi.circuitBreaker.sleepWindowInMilliseconds=10000

# slowApi Command 개별 설정
hystrix.command.callSlowApi.execution.isolation.thread.timeoutInMilliseconds=2000
```
- `execution.isolation.thread.timeoutInMilliseconds`: 타임아웃 시간 (ms)
- `circuitBreaker.requestVolumeThreshold`: 서킷 상태를 계산하기 위한 최소 요청 횟수
- `circuitBreaker.errorThresholdPercentage`: 서킷을 OPEN할 실패율 임계치 (%)
- `circuitBreaker.sleepWindowInMilliseconds`: OPEN 상태 유지 시간 (ms)

---

## ✨ Hystrix만의 특별한 기능들

### Hystrix Dashboard 연동
이 프로젝트는 Hystrix의 상태를 실시간으로 시각화하여 보여주는 **Hystrix Dashboard**와 연동할 수 있습니다.

1.  별도의 Hystrix Dashboard 애플리케이션을 실행합니다. (예: `hystrix-dashboard` 프로젝트에 대시보드를 추가하여 사용 가능)
2.  대시보드 UI에서 이 프로젝트의 메트릭 스트림 주소인 `http://localhost:8080/spring-hystrix/hystrix.stream` 을 입력합니다.
3.  'Monitor Stream' 버튼을 클릭하면 실시간으로 서킷의 상태, 요청량, 실패율 등을 그래프로 확인할 수 있습니다.

### 동적 설정 변경
`hystrix.properties` 파일을 직접 수정하거나, API를 통해 런타임에 Hystrix 설정을 동적으로 변경할 수 있습니다.

**1. `hystrix.properties` 파일 폴링**
- 프로젝트는 5초마다 `hystrix.properties` 파일의 변경 사항을 감지하여 자동으로 설정에 반영합니다. (Archaius 라이브러리 사용)
- HystrixConfig에 `startDynamicHystrixPolling`이 설정을 동적으로 로드하는 기능을 활성화 하는 설정입니다. 해당 설정이 적용되지 않으면 기본값인 1분마다 설정을 동적으로 로드합니다.

**2. API를 통한 설정 변경**
- 아래와 같이 `PUT` 요청을 보내면 `callFailingApi` 커맨드의 타임아웃과 실패율 임계치를 동적으로 변경할 수 있습니다.
```bash
curl -X PUT http://localhost:8080/spring-hystrix/api/config/callFailingApi \
  -H 'Content-Type: application/json' \
  -d '{
        "circuitBreaker": {
          "errorThresholdPercentage": 25
        },
        "execution": {
          "timeoutInMilliseconds": 3000
        }
      }'
```

---

## 🔧 핵심 구현 코드 (`MyService.java`)
서비스 로직에 `@HystrixCommand` 어노테이션을 추가하여 서킷 브레이커를 적용합니다.
설정값들은 `hystrix.properties`에 입력된 설정을 따릅니다.

```java
@Service
public class MyService {

    // 'callFailingApi'라는 Command Key를 가진 서킷 브레이커를 적용합니다.
    // 실패 시 'fallbackFailing' 메소드가 대신 실행됩니다.
    @HystrixCommand(commandKey = "callFailingApi", fallbackMethod = "fallbackFailing")
    public String callFailingApi() {
        // 이 메소드는 항상 예외를 발생시킵니다.
        throw new RuntimeException("Always failing");
    }

    // Fallback 메소드
    public String fallbackFailing(Throwable t) {
        return "Fallback: Failing service is unavailable. Reason: " + t.getMessage();
    }
}
```

---

## 📝 다음 단계
- `hystrix.properties` 파일의 임계치 값을 직접 수정하며 서킷 브레이커의 동작이 어떻게 변하는지 테스트해보세요.
- API를 통해 동적으로 설정을 변경하고, Hystrix Dashboard에서 변화가 즉시 반영되는지 확인해보세요.

