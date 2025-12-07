# Circuit Breaker 예제 - Spring MVC + Hystrix

Spring 4.3 MVC와 Netflix Hystrix를 이용하여 **레거시 서블릿 환경에서 Circuit Breaker를 체험**할 수 있는 예제입니다. Archaius를 활용한 동적 설정, Hystrix Dashboard 연동, 다양한 실패 시나리오를 모두 한 자리에서 실습할 수 있습니다.

## 요구 사항
- JDK 8
- Maven 3.8+
- 서블릿 컨테이너(Tomcat 8+) 또는 IDE 내장 Tomcat

## Hystrix 한눈에 보기
- 🟢 **CLOSED**: 정상 상태, 모든 호출 허용
- 🔴 **OPEN**: 실패율이 임계치를 넘으면 모든 호출 차단 → 즉시 Fallback 실행
- 🟡 **HALF_OPEN**: 제한된 수의 호출만 통과시켜 복구 여부 확인
- Hystrix는 타임아웃/실패율/슬로우콜 비율을 모니터링하여 자동으로 상태를 전환하며, 각 상태는 `/hystrix.stream` 스트림과 `/api/status` 응답에서 확인할 수 있습니다.

## 실행 안내 (IntelliJ 예시)
1. Run/Debug Config에서 Tomcat 서버를 추가하고, `spring-hystrix` WAR 아티팩트를 배포합니다.
2. Application context를 `/spring-hystrix` (또는 원하는 값)으로 설정하고 8080 포트를 사용합니다.
3. 서버를 실행한 뒤 `http://localhost:8080/spring-hystrix/api/normal` 등 엔드포인트가 응답하는지 확인합니다.

## 엔드포인트 빠르게 살펴보기
```bash
# 정상 흐름 (항상 성공)
curl http://localhost:8080/spring-hystrix/api/normal

# 지연 시나리오 (타임아웃 → 폴백)
curl http://localhost:8080/spring-hystrix/api/slow

# 항상 실패
a=`date +%s`; curl "http://localhost:8080/spring-hystrix/api/failing?ts=$a"

# Circuit 상태 확인 (단순 JSON)
curl http://localhost:8080/spring-hystrix/api/status | jq
```
각 엔드포인트의 Hystrix 설정과 응답 메시지는 `MyService` 와 `hystrix.properties` 에 정의되어 있으며, 실패 시 `fallback*` 메서드의 문구가 반환됩니다.

## 실습 시나리오
1. **애플리케이션 실행**: IDE에서 Tomcat을 `/spring-hystrix` 컨텍스트로 기동하거나, WAR를 로컬 톰캣에 배포합니다.
2. **정상 호출 확인**: `curl http://localhost:8080/spring-hystrix/api/normal` 을 여러 번 호출하여 성공 응답과 폴백 문구를 비교합니다.
3. **실패 시나리오 재현**: `for i in {1..5}; do curl -s http://localhost:8080/spring-hystrix/api/failing; echo; done` 으로 연속 실패를 유도해 Fallback 응답을 확인합니다.
4. **느린 호출과 타임아웃**: `curl http://localhost:8080/spring-hystrix/api/slow` 로 긴 지연을 유발하고, Hystrix가 타임아웃 후 폴백을 반환하는지 살펴봅니다.
5. **서킷 상태 점검**: `/api/status` 엔드포인트를 호출하여 각 커맨드의 `isOpen`, `allowRequest` 값을 확인합니다.
6. **스트림 및 대시보드 관찰**: 별도 Hystrix Dashboard에서 `http://localhost:8080/spring-hystrix/hystrix.stream` 을 모니터링하거나, `curl http://localhost:8080/spring-hystrix/hystrix.stream` 으로 SSE 이벤트를 직접 확인합니다.

## Hystrix Dashboard 연동
- 애플리케이션은 `/hystrix.stream` 에서 Server-Sent Events 형식의 메트릭을 노출합니다. 브라우저에서는 빈 화면처럼 보이나, SSE 클라이언트나 대시보드에 연결하면 실시간 메트릭이 표시됩니다.
- 별도의 Hystrix Dashboard 애플리케이션(Spring Boot 기준 `spring-cloud-starter-netflix-hystrix-dashboard`)을 실행하고, UI에서 `http://localhost:8080/spring-hystrix/hystrix.stream` 을 입력하면 이 모듈의 메트릭을 모니터링할 수 있습니다.
- 여러 인스턴스를 동시에 보고 싶다면 Turbine 서버를 띄우고 각 인스턴스의 `/hystrix.stream` 을 등록하면 됩니다.

## 동적 설정 사용법

### hystrix.properties 폴링
- `HystrixConfig` 는 기본적으로 `startDynamicHystrixPolling()` 을 호출하여 1초 대기 후 5초 간격으로 `hystrix.properties` 를 재적용합니다. 파일 내용을 수정하면 다음 폴링 주기에 별도 배포 없이 반영됩니다.
- 정적 로드를 원한다면 `startDynamicHystrixPolling()` 호출을 주석 처리하고 `loadStaticHystrixConfiguration()` 만 유지하여 애플리케이션 기동 시 한 번만 속성을 읽도록 구성할 수 있습니다.

### 설정 변경 API
- `HystrixConfigController` 는 `/config/{commandKey}` 엔드포인트를 제공하여 런타임에 커맨드별 속성을 조회·수정할 수 있습니다.
  - `GET /config/{commandKey}` : 현재 적용 중인 Hystrix 속성 값을 확인합니다.
  - `PUT /config/{commandKey}` : JSON 페이로드로 `circuitBreaker`, `execution`, `fallback`, `metrics` 섹션을 전달하면 해당 키의 설정을 변경합니다.

예시 요청:
```bash
curl -X PUT http://localhost:8080/spring-hystrix/config/callFailingApi \
  -H 'Content-Type: application/json' \
  -d '{
        "circuitBreaker": {
          "requestVolumeThreshold": 5,
          "errorThresholdPercentage": 25
        },
        "execution": {
          "timeoutInMilliseconds": 2500
        }
      }'
```
새 설정은 이후 생성되는 Hystrix 명령 인스턴스부터 적용되며, 변경 내역은 응답 `updatedProperties` 필드에서 확인할 수 있습니다.

## 주요 디렉터리
- `com.example.config` : `HystrixConfig`, `WebConfig` 등 Hystrix/Archaius 및 Spring MVC 설정이 위치합니다.
- `com.example.controller` : `/api/*` 학습용 시나리오를 제공하는 `MyServiceTestController` 와 `/config/*` 동적 설정 API를 제공하는 `HystrixConfigController` 가 있습니다.
- `com.example.service` : Hystrix 커맨드를 보유한 `MyService` 와 외부 API를 시뮬레이션하는 `ExternalService` 로 구성됩니다.
- `src/main/resources/hystrix.properties` : 커맨드별 임계값, 타임아웃, 서킷 설정 등의 기본 프로퍼티를 정의합니다.

## 참고 사항
- Hystrix 대시보드 스트림(`hystrix.stream`) 서블릿이 `web.xml` 에 등록되어 있으며, 톰캣 포트/컨텍스트에 따라 `http://localhost:8080/hystrix.stream` 또는 `http://localhost:8080/spring-hystrix/hystrix.stream` 으로 접근합니다.
- 포트가 이미 사용 중이라면 톰캣이 기동되지 않을 수 있으니(`java.net.BindException`), 다른 프로세스를 종료하거나 포트를 변경하세요.
- Archaius 폴링 주기와 관련된 실험이 끝나면 `hystrix.properties` 내 불필요한 설정은 주석 처리한 상태로 유지하는 것을 권장합니다.
