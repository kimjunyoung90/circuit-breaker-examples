# Circuit Breaker 예제

이 모듈은 Spring 4.3 MVC와 Netflix Hystrix를 이용해 서블릿 기반 애플리케이션에서 회로 차단기를 구성하는 최소 예제를 제공합니다. 정적 속성 로드와 주기적 폴링을 모두 경험할 수 있도록 Archaius 기반 동적 설정까지 포함되어 있습니다.

## 요구 사항
- JDK 8
- Maven 3.8+
- 서블릿 컨테이너(Tomcat 8+)

## 빌드와 실행
1. 프로젝트 루트에서 `mvn clean package`를 실행하면 `target/spring-hystrix.war` 가 생성됩니다.
2. 생성된 WAR 파일을 원하는 서블릿 컨테이너에 배포하거나, 내장 서버가 필요하다면 로컬 Tomcat에 복사하십시오.
3. 애플리케이션 기본 포트는 `8080`이며 컨텍스트 루트는 `/`입니다. 배포 환경에 맞춰 `src/main/resources/application.properties` 를 수정할 수 있습니다.

## 주요 디렉터리
- `com.example.config` : Hystrix 설정(정적 설정, 동적 설정)과 Spring MVC 스캔 설정이 위치합니다.
- `com.example.controller` : 학습용 REST 엔드포인트(`/api/...`)와 설정 관리용 `/config` API를 제공합니다.
- `com.example.service` : 외부 시스템을 호출하는 `TestService` 와 외부 시스템 역할을 담당하는 `OuterService` 가 위치합니다.
- `src/main/resources/hystrix.properties` : 커맨드별 임계값과 타임아웃을 정의하는 프로퍼티 파일입니다.

## 학습용 엔드포인트
```bash
# 정상 흐름 (항상 성공)
curl http://localhost:8080/api/normal

# 지연 시나리오 (타임아웃 → 폴백)
curl http://localhost:8080/api/slow

# 항상 실패
curl http://localhost:8080/api/fail

# 의도적 실패 (Circuit Open 유도)
for i in {1..5}; 
do curl -s http://localhost:8080/api/failing; 
echo; 
done

# 현재 Circuit 상태 점검
curl http://localhost:8080/api/status | jq
```
각 엔드포인트의 Hystrix 설정과 응답 메시지는 `TestService` 와 `hystrix.properties` 에 정의되어 있습니다. 실패 시에는 `fallback*` 메서드에서 지정한 문구가 반환됩니다.

## 동적 설정 사용법

### hystrix.properties 폴링
- `HystrixConfig` 는 기본적으로 `startDynamicHystrixPolling()` 을 호출하여 1초 대기 후 5초 간격으로 `hystrix.properties` 를 재적용합니다. 파일 내용을 수정하면 다음 폴링 주기에 별도 배포 없이 반영됩니다.
- 정적 로드를 원한다면 `startDynamicHystrixPolling()` 호출을 주석 처리하고 `loadStaticHystrixConfiguration()` 만 유지하여 애플리케이션 기동 시 한 번만 속성을 읽도록 구성할 수 있습니다.

### 설정 변경 API
- `HystrixConfigController` 는 `/config/{commandKey}` 엔드포인트를 제공하여 런타임에 커맨드별 속성을 조회·수정할 수 있습니다.
  - `GET /config/{commandKey}` : 유효한 Hystrix 속성 값을 확인합니다.
  - `PUT /config/{commandKey}` : JSON 페이로드로 `circuitBreaker`, `execution`, `fallback`, `metrics` 섹션을 전달하면 해당 키의 설정을 변경합니다.

예시 요청:
```bash
curl -X PUT http://localhost:8080/config/callFailingApi \
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

## 참고 사항
- Hystrix 대시보드 스트림(`hystrix.stream`) 서블릿이 `web.xml` 에 등록되어 있으므로 필요 시 대시보드에서 메트릭을 시각화할 수 있습니다.
- Archaius 폴링 주기와 관련된 주석 설정은 `hystrix.properties` 하단에 정리되어 있습니다. 값의 실험이 끝나면 불필요한 설정은 주석 처리한 채 커밋하십시오.
