# ChatService Backend

Spring Boot 기반의 실시간 채팅 백엔드입니다. 일반 로그인과 소셜 로그인, 친구 관리, 공개/비공개 그룹 채팅방, 1:1 DM, 이미지 전송, 참여자 및 운영자 관리를 제공합니다.

프론트엔드 저장소: [D7S5/chatService-FE](https://github.com/D7S5/chatService-FE)

## 주요 기능

- 이메일/비밀번호 회원가입 및 로그인
- Google, Kakao, Naver OAuth 2.0 로그인
- JWT access token과 HttpOnly refresh token 쿠키 기반 인증
- Redis에 저장되는 1회용 토큰을 이용한 WebSocket 연결 인증
- STOMP/SockJS 기반 그룹 채팅 및 1:1 DM
- Kafka와 Outbox 패턴을 이용한 메시지 발행, 저장, 브로드캐스트
- 친구 요청, 수락, 거절, 삭제 및 친구 상태 실시간 알림
- 그룹 채팅방 생성, 초대 코드, 참여/퇴장, 강퇴/차단, 관리자 위임
- Redis TTL 기반 접속 상태, heartbeat, 채팅방 세션 관리
- 채팅/프로필 이미지 업로드와 파일 검증
- 사용자별 채팅 전송 빈도 제한

## 기술 스택

| 영역 | 기술 |
| --- | --- |
| 애플리케이션 | Java 17, Spring Boot 3.5.7, Gradle |
| API/실시간 통신 | Spring MVC, WebSocket, STOMP, SockJS |
| 인증/인가 | Spring Security, OAuth 2.0 Client, JWT, BCrypt |
| 데이터베이스 | MySQL, Spring Data JPA, Hibernate |
| 메시징 | Apache Kafka, Spring Kafka, Transactional Outbox |
| 캐시/상태 | Redis, Spring Data Redis |
| 테스트 | JUnit 5, Spring Boot Test |
| 운영 | Docker Compose, Jenkins, Spring Boot Actuator |


그룹 메시지는 `group-message-topic`, DM은 `dm-messages` Kafka 토픽을 사용합니다. Outbox processor가 미발행 메시지를 Kafka로 전달하고, 저장 consumer와 WebSocket broadcast consumer가 각각 영속화와 실시간 전송을 담당합니다.


### 요구 사항

- JDK 17
- Docker 및 Docker Compose
- MySQL 8.x

> Docker Compose에는 ZooKeeper, Kafka, Kafdrop, Redis가 포함되어 있습니다. MySQL은 별도로 실행하고 데이터베이스를 생성해야 합니다.

### 1. 저장소 실행 준비

```bash
git clone <repository-url>
cd chatService
chmod +x gradlew
```

### 2. 인프라 실행

```bash
docker compose up -d
```

| 서비스 | 주소/포트 |
| --- | --- |
| Kafka | `localhost:9092` |
| Redis | `localhost:6379` |
| Kafdrop | http://localhost:9000 |
| ZooKeeper | `localhost:2181` |

MySQL에 로컬용 데이터베이스를 생성합니다.

```sql
CREATE DATABASE chatService_demo
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

### 3. 애플리케이션 설정

가장 빠른 로컬 실행 방법은 `demo` 프로필을 사용하는 것입니다. 현재 `application-demo.yml`에는 아래 기본값이 들어 있습니다.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/chatService_demo?serverTimezone=Asia/Seoul&characterEncoding=UTF-8
    username: root
    password: 1234
```


### 4. 서버 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=demo'
```

## 인증 흐름

1. `POST /api/auth/login`으로 로그인합니다.
2. 응답 body의 access token은 REST 요청의 `Authorization: Bearer <token>` 헤더에 사용합니다.
3. refresh token은 `refreshToken`이라는 HttpOnly 쿠키로 발급됩니다.
4. access token 재발급은 `POST /api/auth/refresh`, 로그아웃은 `POST /api/auth/logout`을 사용합니다.
5. WebSocket 연결 전 `POST /api/ws/token`을 호출해 120초 동안 유효한 1회용 토큰을 받습니다.
6. SockJS/STOMP 연결 주소에 `ws-token` query parameter를 전달합니다.

```text
http://localhost:9090/ws?ws-token=<one-time-token>
```

쿠키를 사용하는 프론트엔드는 REST 요청 시 credentials 옵션을 활성화해야 합니다. 현재 CORS 허용 origin은 `http://localhost:3000`입니다.

## 주요 REST API

아래 표는 컨트롤러 기준의 주요 endpoint 요약입니다. 인증이 필요한 요청에는 access token을 전달하세요.

### 인증 및 사용자

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/auth/register` | 회원가입 |
| POST | `/api/auth/login` | 로그인 |
| POST | `/api/auth/refresh` | access token 재발급 |
| POST | `/api/auth/logout` | 로그아웃 |
| GET | `/api/me` | 현재 사용자 조회 |
| POST | `/api/user/set-nickname` | 닉네임 설정 |
| POST | `/api/user/oauth/nickname` | OAuth 사용자 닉네임 설정 |
| POST | `/api/ws/token` | WebSocket 1회용 토큰 발급 |

소셜 로그인 시작 주소는 `/oauth2/authorization/google`, `/oauth2/authorization/kakao`, `/oauth2/authorization/naver`입니다.

### 채팅방 및 참여자

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/rooms/create` | 채팅방 생성 |
| GET | `/api/rooms` | 전체 채팅방 조회 |
| GET | `/api/rooms/with-count` | 현재 인원을 포함한 채팅방 조회 |
| GET | `/api/rooms/{roomId}` | 채팅방 상세 조회 |
| GET | `/api/rooms/{roomId}/messages?limit=50` | 그룹 메시지 이력 조회 |
| POST | `/api/rooms/{roomId}/participants` | 채팅방 참여 |
| DELETE | `/api/rooms/{roomId}/participants` | 채팅방 퇴장 |
| GET | `/api/rooms/{roomId}/participants` | 참여자 목록 조회 |
| GET | `/api/rooms/{roomId}/count` | 현재 참여자 수 조회 |
| POST | `/api/rooms/join-by-invite` | 초대 코드로 참여 |
| POST | `/api/rooms/{roomId}/invite/reissue` | 초대 코드 재발급 |
| POST | `/api/rooms/{roomId}/kick` | 참여자 강퇴 |
| POST | `/api/rooms/{roomId}/ban` | 참여자 차단 |

### DM, 친구, 이미지

| Method | Endpoint | 설명 |
| --- | --- | --- |
| POST | `/api/dm/start` | DM 방 생성 또는 조회 |
| GET | `/api/dm/messages/{roomId}` | DM 메시지 이력 조회 |
| GET | `/api/dm/list/{userId}` | 사용자의 DM 방과 읽지 않은 수 조회 |
| PUT | `/api/dm/messages/{roomId}/read?userId=...` | DM 읽음 처리 |
| POST | `/api/user/friends/request` | 친구 요청 |
| GET | `/api/user/friends/received/{userId}` | 받은 친구 요청 조회 |
| POST | `/api/user/friends/accept/{id}` | 친구 요청 수락 |
| POST | `/api/user/friends/reject/{id}` | 친구 요청 거절 |
| GET | `/api/user/friends/list/{userId}` | 친구 목록 조회 |
| DELETE | `/api/user/friends/{friendUserId}` | 친구 삭제 |
| POST | `/api/chat/images` | 채팅 이미지 업로드 (`multipart/form-data`, `image`) |

이미지는 최대 10 MB이며 기본적으로 `uploads` 디렉터리에 저장되고 `/uploads/**` 경로로 제공됩니다.

### 발행 destination

| Destination | 설명 |
| --- | --- |
| `/app/chat.send` | 그룹 메시지 전송 |
| `/app/dm.send` | DM 전송 |
| `/app/user.enter` | 온라인 상태 등록 |
| `/app/user.heartbeat` | 온라인/채팅방 세션 TTL 갱신 |
| `/app/user.leave` | 오프라인 상태 전환 |
| `/app/room.enter` | 현재 접속한 채팅방 등록 |
| `/app/rooms/{roomId}/admin` | 관리자 권한 토글 |

### 구독 destination

| Destination | 설명 |
| --- | --- |
| `/topic/chat/{roomId}` | 그룹 메시지 |
| `/user/queue/dm` | 개인 DM |
| `/topic/online-users` | 온라인 사용자 목록 |
| `/topic/friends/{userId}` | 친구 관련 이벤트 |
| `/topic/room-users/{roomId}` | 채팅방 사용자 변경 |
| `/topic/rooms/{roomId}/count` | 참여 인원 변경 |
| `/topic/rooms/{roomId}/participants` | 참여자/관리자 변경 이벤트 |
| `/topic/room/{roomId}/owner` | 방장 변경 이벤트 |
| `/user/queue/room-force-exit` | 강퇴/차단 알림 |
| `/user/queue/rate-limit` | 메시지 전송 제한 알림 |

## 프로젝트 구조

```text
src/main/java/com/example/chatService
├── config/       # Security, WebSocket, Redis, Kafka 설정
├── controller/   # REST 및 STOMP endpoint
├── component/    # interceptor, event handler, rate limiter
├── dto/          # API 및 Kafka 메시지 DTO
├── entity/       # JPA entity와 Outbox entity
├── repository/   # Spring Data JPA repository
├── service/      # 채팅방, 참여자, 친구, DM, 이미지 도메인 로직
├── kafka/        # producer, consumer, Outbox processor
├── redis/        # 온라인 상태, 세션, WebSocket 토큰
├── security/     # JWT 인증과 로그인 처리
├── oauth/        # OAuth 2.0 사용자 매핑과 handler
└── listener/     # 도메인 이벤트의 WebSocket 전파
```

## CI/CD

`Jenkinsfile`은 다음 순서로 배포합니다.

1. 소스 checkout
2. `./gradlew clean bootJar -x test`로 `app.jar` 생성
3. 원격 서버로 JAR 업로드
4. Jenkins credentials로 `.env` 생성
5. `prod` 프로필로 애플리케이션 재시작 및 로그 확인
