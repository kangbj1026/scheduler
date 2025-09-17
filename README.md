## 1. 프로젝트 설정

### 1.1 Gradle 설정
- build.gradle 필요한 dependencies 추가
    - spring-boot-starter-web
    - spring-boot-starter-data-jpa
    - spring-boot-starter-quartz
    - mysql-connector-j

### 1.2 application.yml 설정
- MySQL 데이터베이스 연결
- JPA DDL 자동 생성(update)
- Hibernate SQL 출력 설정
- Quartz 스케줄러 기본 설정

## 2. Quartz 스케줄러 설정

Spring Boot 3.x YAML Quartz 세부 설정(threadCount, instanceName 등)이 자동 매핑
따라서 Java Config 직접 정의

### SchedulerConfig.java

- 위치: `src/main/java/com/example/config/SchedulerConfig.java`
- 역할: Quartz 스케줄러 초기화, 스레드 풀, 메모리/DB 저장 방식 설정

## 3. Spring Boot Application

### SchedulerApplication.java
- 위치: `src/main/java/com/example/SchedulerApplication.java`
- 역할:
    1. Spring Boot 서버 진입점
    2. ApplicationContext 생성
    3. Quartz Config 빈 자동 로딩

## 4. Security & CORS 설정

### SecurityConfig.java

- Security 설정 파일(`SecurityConfig.java`)에서 반드시 `CorsConfigurationSource`를 정의하고
  `HttpSecurity`에 연결해야 브라우저의 preflight(OPTIONS) 요청이 Security 필터에 걸리지 않습니다.

#### SecurityConfig 요약
- CSRF 비활성화: `csrf(CsrfConfigurer::disable)`
- CORS 연결: `cors(cors -> cors.configurationSource(corsConfigurationSource()))`
- 모든 요청 허용(개발용): `authorizeHttpRequests(auth -> auth.anyRequest().permitAll())`

#### CORS 설정 예
- 허용 Origin: `http://localhost:3000`
- 허용 Methods: `GET, POST, PUT, DELETE, OPTIONS`
- 허용 Headers: `*`
- Allow Credentials: `true`

## 5. SchedulerJob 엔티티 및 Repository

### BaseEntity.java
- 위치: `src/main/java/com/example/common/entity/BaseEntity.java`
- 역할: 모든 엔티티 공통 필드(createdAt, updatedAt) 자동 관리
- @MappedSuperclass + AuditingEntityListener 사용
- @PrePersist / @PreUpdate DB에 생성/수정 시간 자동 반영

### SchedulerJob.java
- 위치: `src/main/java/com/example/entity/SchedulerJob.java`
- BaseEntity 상속: createdAt, updatedAt 자동 관리
- 필드:
  - id
  - jobName
  - jobGroup
  - description
  - cronExpression
  - status (RUNNING, PAUSED 등)

### SchedulerJobRepository.java
- 위치: `src/main/java/com/example/repository/SchedulerJobRepository.java`
- 역할: DB CRUD 수행
- JpaRepository 상속으로 기본 CRUD 지원
- 추가로 `findByJobNameAndJobGroup` 메소드로 Job 조회 가능

## 6. DynamicJob

### DynamicJob.java
- 위치: `src/main/java/com/example/scheduler/DynamicJob.java`
- Quartz Job 실제 실행
- execute()에서 로그 기록

## 7. SchedulerService

- 위치: `src/main/java/com/example/service/SchedulerService.java`
- 기능:
  1. createJob: 스케줄러 등록 및 DB 저장, 로그 기록
  2. getAllJobs: 모든 스케줄러 조회, 로그 기록
  3. runJobNow: 수동 실행, 상태 체크 후 로그 기록
  4. deleteJob: 삭제, DB와 Quartz 동기화, 로그 기록
  5. pauseJob: 중단, 상태 변경 후 로그 기록
  6. resumeJob: 재개, 상태 변경 후 로그 기록

## 8. SchedulerController

- 위치: `src/main/java/com/example/controller/SchedulerController.java`
- 기능:
  1. 스케줄러 등록: POST `/api/schedulers`
  2. 스케줄러 조회: GET `/api/schedulers`
  3. 수동 실행: POST `/api/schedulers/run?jobName=xxx&jobGroup=xxx`
  4. 삭제: DELETE `/api/schedulers?jobName=xxx&jobGroup=xxx`
  5. 중단: POST `/api/schedulers/pause?jobName=xxx&jobGroup=xxx`
  6. 재개: POST `/api/schedulers/resume?jobName=xxx&jobGroup=xxx`

## 9. React 프론트

1. 프로젝트 위치: `scheduler/frontend`
2. 설치:
```bash
 cd scheduler/frontend
 npx create-react-app .
 npm install axios react-router-dom 
 
 frontend/
  ├─ src/
  │  ├─ api/
  │  │  └─ schedulerApi.js         ← 백엔드 API 호출
  │  ├─ components/
  │  │  └─ SchedulerForm.jsx       ← 스케줄러 등록/수정 Form
  │  ├─ pages/
  │  │  └─ SchedulerPage.jsx       ← 스케줄러 관리 화면
  │  ├─ App.js                      ← 라우팅 및 메인 화면
  │  └─ index.js                    ← React 엔트리포인트

npm start
```

## 10. CORS 설정

- 위치: src/main/java/com/example/config/CorsConfig.java
- 목적: SecurityConfig CORS 설정이 적용되지 않을 때, 전역적으로 CORS 정책 적용
- 설정 요약:
  1. React 프론트(http://localhost:3000)에서 API 호출 허용
  2. 모든 엔드포인트(/**)에 대해 적용
  3. 모든 HTTP 메서드(GET, POST, PUT, DELETE 등) 허용
  4. 모든 헤더 허용
  5. 쿠키/인증정보 포함 요청 허용
- 설명:
  - SecurityConfig .cors()만 설정하면 일부 요청에서 CORS 동작하지 않을 수 있음
  - 별도의 WebMvcConfigurer 사용하여 전역 CORS 정책 적용하면 React 개발 환경에서 발생하는 CORS 오류 방지 가능
  - 브라우저에서 발생하는 오류 예: Access to XMLHttpRequest at 'http://localhost:8081/api/schedulers' from origin 'http://localhost:3000' has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present on the requested resource.
  - 이유: 서버가 해당 출처(http://localhost:3000)에서 오는 요청에 대해 Access-Control-Allow-Origin 헤더를 포함하지 않아 브라우저가 요청을 차단함
  - 해결: 별도의 CorsConfig 통해 모든 엔드포인트에 대해 허용 origin, 메서드, 헤더, 인증 정보(credentials)를 명시적으로 설정

## 11. Scheduler 초기화

- 위치: `src/main/java/com/example/config/SchedulerInitializer.java`
- 설명:
  - 서버 시작 시 DB에 저장된 Quartz Job 조회
  - 존재하지 않는 Job Scheduler 등록
  - 상태가 PAUSED 즉시 일시정지, RUNNING 실행 대기
  - 서버 재시작 시 DB와 Quartz Scheduler 상태 동기화
