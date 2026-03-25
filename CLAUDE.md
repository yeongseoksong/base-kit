# base-kit

## 프로젝트 개요

Spring Boot MSA 서비스들이 공통으로 사용하는 기반 라이브러리.
`core` 모듈을 GitHub Packages에 배포하고, 다른 레포의 서비스들이 의존성으로 받아 사용한다.

## 모듈 구조

```
base-kit/
├── core/                  라이브러리 (배포 대상)
├── demo/                  로컬 검증용 Spring Boot 앱
├── gradle/
│   └── libs.versions.toml 모든 의존성 버전 관리
└── .github/workflows/
    └── publish.yml        태그 푸시 시 GitHub Packages 배포
```

## core 패키지 구조

```
common/
├── cqrs/
│   ├── command/   Command<R>, CommandHandler<C,R>, CommandBus
│   ├── query/     Query<R>, QueryHandler<Q,R>, QueryBus
│   ├── bus/       SimpleCommandBus, SimpleQueryBus
│   └── CqrsAutoConfiguration
├── datasource/
│   ├── DataSourceType            (WRITE / READ enum)
│   ├── RoutingDataSource         (AbstractRoutingDataSource 구현)
│   ├── BasekitDatasourceProperties
│   └── RoutingDataSourceAutoConfiguration
├── exception/
│   ├── ErrorCode                 (interface — 서비스에서 enum으로 구현)
│   ├── CommonErrorCode           (공통 에러 코드)
│   ├── BusinessException
│   └── GlobalExceptionHandler
├── jpa/
│   ├── BaseEntity                (createdAt, updatedAt, createdBy, updatedBy)
│   ├── BaseAggregateRoot         (도메인 이벤트 발행)
│   └── JpaAuditingConfig
├── response/
│   ├── ApiResponse<T>            (성공 응답 래퍼)
│   └── ErrorResponse             (에러 응답 + FieldError)
├── util/
│   └── PageResponse<T>           (Page<T> → 페이지 응답)
└── web/
    ├── BasekitCorsProperties
    └── WebMvcConfig
```

## 아키텍처 원칙

### CQRS
- `CommandHandler` → `@Transactional` → WRITE DB
- `QueryHandler` → `@Transactional(readOnly = true)` → READ DB
- `RoutingDataSource`가 `TransactionSynchronizationManager.isCurrentTransactionReadOnly()`로 자동 라우팅

### Auto-configuration
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`에 등록된 클래스들이 자동 활성화된다.
소비 서비스는 `@ComponentScan` 수정 없이 모든 기능을 사용할 수 있다.

### ErrorCode 확장 패턴
`ErrorCode` 인터페이스를 서비스별 enum으로 구현한다.
```java
public enum OrderErrorCode implements ErrorCode { ... }
```

## 빌드 규칙

- 모든 의존성 버전은 `gradle/libs.versions.toml`에서 관리
- `core`는 `java-library` + `bootJar { enabled = false }` + `jar { enabled = true }`
- `demo`는 `bootJar { enabled = true }` + `jar { enabled = false }`
- `core`의 의존성은 `api`로 선언 (소비 서비스에 transitive 전달)

## 배포

- GitHub Actions: `v*` 태그 푸시 시 `:core:publish` 자동 실행
- 로컬 배포: `./gradlew :core:publish` (gpr.user, gpr.key 필요)
- artifactId: `base-kit-core`

## 주의사항

- `BasekitDatasourceProperties.enabled: false`가 기본값. 라우팅 사용 시 `true`로 변경
- `@CreatedBy` / `@LastModifiedBy` 사용 시 소비 서비스에서 `AuditorAware<String>` 빈 정의 필요
- `RoutingDataSource`는 auto-configuration 대상이 아님. 소비 서비스의 `DataSourceConfig`에서 직접 조립
