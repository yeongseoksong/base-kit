# base-kit

> Spring Boot MSA 서비스들이 공통으로 사용하는 기반 라이브러리.
> DDD 4-레이어 아키텍처와 CQRS 패턴을 기반으로 설계되었습니다.

---

## 목차

1. [모듈 구조](#1-모듈-구조)
2. [의존성 추가](#2-의존성-추가)
3. [제공 기능](#3-제공-기능)
   - 3.1 [CQRS](#31-cqrs)
   - 3.2 [예외 처리](#32-예외-처리)
   - 3.3 [응답 형식](#33-응답-형식)
   - 3.4 [JPA 기반 엔티티](#34-jpa-기반-엔티티)
   - 3.5 [Read/Write DB 라우팅](#35-readwrite-db-라우팅)
   - 3.6 [CORS 설정](#36-cors-설정)
   - 3.7 [공통 Value Object](#37-공통-value-object)
4. [설정 레퍼런스](#4-설정-레퍼런스)
5. [배포](#5-배포)
6. [요구사항](#6-요구사항)

---

## 1. 모듈 구조

```
base-kit/
├── platform/        공통 라이브러리 — GitHub Packages 배포 대상
├── lib-entity/      공통 Value Object — GitHub Packages 배포 대상
└── demo/            로컬 검증용 Spring Boot 앱
```

| 아티팩트 | artifactId | 설명 |
|---|---|---|
| platform | `base-kit-platform` | CQRS, 예외처리, 응답 래퍼, JPA, 라우팅, CORS |
| lib-entity | `base-kit-lib-entity` | `Money`, `Name` 등 공통 Value Object |

> `platform`은 `lib-entity`를 `api`로 포함하므로, 일반적으로 `platform`만 의존성으로 추가하면 됩니다.

---

## 2. 의존성 추가

### 저장소 설정

GitHub Packages는 공개 저장소여도 읽기 인증이 필요합니다.

```groovy
repositories {
    mavenCentral()
    maven {
        url = uri('https://maven.pkg.github.com/yeongseoksong/base-kit')
        credentials {
            username = project.findProperty('gpr.user')?.toString() ?: System.getenv('GITHUB_ACTOR')
            password = project.findProperty('gpr.key')?.toString() ?: System.getenv('GITHUB_TOKEN')
        }
    }
}
```

**로컬 개발 환경** — `~/.gradle/gradle.properties` 또는 `~/.zshrc` 중 선택:

```properties
# ~/.gradle/gradle.properties
gpr.user=yeongseoksong
gpr.key=<GitHub Personal Access Token>  # read:packages 권한 필요
```

```bash
# ~/.zshrc
export GITHUB_ACTOR=yeongseoksong
export GITHUB_TOKEN=<GitHub Personal Access Token>
```

### 의존성 선언

```groovy
dependencies {
    // platform 하나로 lib-entity 포함
    implementation 'io.github.yeongseoksong:base-kit-platform:1.0.0'

    // Value Object만 별도로 필요한 경우
    implementation 'io.github.yeongseoksong:base-kit-lib-entity:1.0.0'
}
```

---

## 3. 제공 기능

### 3.1 CQRS

`CommandBus` / `QueryBus`를 통해 Command와 Query를 분리합니다.
Auto-configuration으로 자동 등록되며 `@ComponentScan` 수정이 불필요합니다.

```java
// Command 정의
public record CreateOrderCommand(String productId, int quantity) implements Command<Long> {}

// CommandHandler — WRITE DB 트랜잭션
@Component
@Transactional
public class CreateOrderCommandHandler implements CommandHandler<CreateOrderCommand, Long> {
    @Override
    public Long handle(CreateOrderCommand command) {
        return savedOrder.getId();
    }
}

// Query 정의
public record OrderQuery(Long orderId) implements Query<OrderResponse> {}

// QueryHandler — READ DB 자동 라우팅
@Component
@Transactional(readOnly = true)
public class OrderQueryHandler implements QueryHandler<OrderQuery, OrderResponse> {
    @Override
    public OrderResponse handle(OrderQuery query) {
        return orderRepository.findById(query.orderId());
    }
}

// Controller
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final CommandBus commandBus;
    private final QueryBus queryBus;

    @PostMapping("/orders")
    public ApiResponse<Long> create(@RequestBody CreateOrderRequest request) {
        return ApiResponse.success(commandBus.dispatch(new CreateOrderCommand(request.productId(), request.quantity())));
    }

    @GetMapping("/orders/{id}")
    public ApiResponse<OrderResponse> get(@PathVariable Long id) {
        return ApiResponse.success(queryBus.dispatch(new OrderQuery(id)));
    }
}
```

---

### 3.2 예외 처리

`ErrorCode` 인터페이스를 서비스별 `enum`으로 구현합니다.
`GlobalExceptionHandler`가 `BusinessException`을 자동으로 `ErrorResponse`로 변환합니다.

```java
// 서비스별 ErrorCode 정의
@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND(404, "O001", "주문을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(400, "O002", "재고가 부족합니다.");

    private final int status;
    private final String code;
    private final String message;
}

// 사용
throw new BusinessException(OrderErrorCode.ORDER_NOT_FOUND);
```

**에러 응답 형식:**
```json
{
  "code": "O001",
  "message": "주문을 찾을 수 없습니다.",
  "errors": []
}
```

---

### 3.3 응답 형식

모든 API 응답은 `ApiResponse<T>`로 래핑합니다.

```java
// 단건 응답
return ApiResponse.success(orderResponse);

// 페이지 응답
Page<Order> page = orderRepository.findAll(pageable);
return ApiResponse.success(PageResponse.of(page.map(OrderResponse::from)));
```

**성공 응답 형식:**
```json
{
  "data": { ... }
}
```

**페이지 응답 형식:**
```json
{
  "data": {
    "content": [...],
    "page": 0,
    "size": 20,
    "totalElements": 100,
    "totalPages": 5,
    "last": false
  }
}
```

---

### 3.4 JPA 기반 엔티티

#### BaseEntity

생성/수정 시각과 생성/수정자를 자동 관리합니다.

```java
@Entity
public class Order extends BaseEntity {
    @Id @GeneratedValue
    private Long id;
    // createdAt, updatedAt, createdBy, updatedBy 자동 관리
}
```

> `createdBy` / `updatedBy` 사용 시 소비 서비스에서 `AuditorAware<String>` 빈 정의가 필요합니다.

#### BaseAggregateRoot

도메인 이벤트 발행이 필요한 Aggregate Root에 사용합니다.

```java
@Entity
public class Order extends BaseAggregateRoot<Order> {
    public void complete() {
        // 비즈니스 로직
        registerEvent(new OrderCompletedEvent(this.id));
    }
}
```

---

### 3.5 Read/Write DB 라우팅

`@Transactional(readOnly = true)` 여부에 따라 `RoutingDataSource`가 자동으로 READ/WRITE DB를 선택합니다.

`base-kit.datasource.enabled: false`가 기본값입니다. 활성화하려면 아래 설정을 추가하세요.

```yaml
base-kit:
  datasource:
    enabled: true
    write:
      url: jdbc:mysql://primary:3306/db
      username: root
      password: secret
      driver-class-name: com.mysql.cj.jdbc.Driver
      pool:
        maximum-pool-size: 10
        minimum-idle: 5
        connection-timeout: 3000
    read:
      url: jdbc:mysql://replica:3306/db
      username: root
      password: secret
      driver-class-name: com.mysql.cj.jdbc.Driver
      pool:
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 3000
```

> `enabled: false`이면 기존 `spring.datasource` 설정을 그대로 사용합니다.

---

### 3.6 CORS 설정

```yaml
base-kit:
  cors:
    mapping: /api/**
    allowed-origins:
      - https://my-service.com
    allowed-credentials: true
```

---

### 3.7 공통 Value Object

`lib-entity` 모듈이 제공하는 JPA `@Embeddable` Value Object입니다.

#### Money

```java
@Embedded
private Money price = Money.ZERO;

// 사용
Money total = price.add(Money.of(1000));
boolean canAfford = balance.isGreaterThanOrEqual(total);
```

#### Name

```java
@Embedded
private Name name;

// 사용 — null, blank, 50자 초과 시 IllegalArgumentException
Name username = Name.of("홍길동");
```

---

## 4. 설정 레퍼런스

| 키 | 기본값 | 설명 |
|---|---|---|
| `base-kit.datasource.enabled` | `false` | Read/Write 라우팅 활성화 여부 |
| `base-kit.cors.mapping` | — | CORS 적용 경로 패턴 |
| `base-kit.cors.allowed-origins` | — | 허용 Origin 목록 |
| `base-kit.cors.allowed-credentials` | `false` | 인증 정보 포함 허용 여부 |

---

## 5. 배포

GitHub Actions가 `v*` 태그 푸시 시 자동으로 GitHub Packages에 배포합니다.

```bash
# 태그 푸시 → 자동 배포
git tag v1.0.1
git push origin v1.0.1

# 수동 배포 (로컬)
./gradlew :platform:publish :lib-entity:publish
```

버전은 git 태그에서 자동 결정됩니다.
- 태그가 있는 커밋: `1.0.1`
- 태그가 없는 커밋: `{commitHash}-SNAPSHOT`

---

## 6. 요구사항

- Java 21
- Spring Boot 3.x
