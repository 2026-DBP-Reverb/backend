# Database Programming - Backend

## 실행 방법

프로젝트 루트에서 실행합니다.

```bash
./gradlew bootRun
```

서버 상태 확인:

```bash
curl http://localhost:8080/api/heartbeat
```

DB 연결 확인:

```bash
curl http://localhost:8080/api/heartbeat/db
```

## 빌드 방법

실행 가능한 jar를 생성합니다.

```bash
./gradlew clean bootJar
```

생성 파일:

```text
build/libs/Backend-0.0.1-SNAPSHOT.jar
```

실행:

```bash
java -jar build/libs/Backend-0.0.1-SNAPSHOT.jar
```

## DB 환경변수

환경변수는 `.env.example`을 참고합니다.
프로젝트 루트에 `.env` 파일을 만들면 서버 실행 시 자동으로 읽습니다.

```text
DB_DRIVER=oracle.jdbc.driver.OracleDriver
DB_URL=jdbc:oracle:thin:@...
DB_USERNAME=...
DB_PASSWORD=...
```

## React 연동

개발 중 React는 `http://localhost:3000`에서 실행한다고 가정합니다.

백엔드는 CORS에서 `http://localhost:3000`을 허용합니다.

세션 쿠키를 사용할 경우 React 요청에는 `credentials: "include"`를 포함합니다.

```javascript
fetch("http://localhost:8080/api/heartbeat", {
  method: "GET",
  credentials: "include"
});
```

## 표준 JSON 응답

성공 응답:

```json
{
  "success": true,
  "data": {}
}
```

실패 응답:

```json
{
  "success": false,
  "error": {
    "code": "ERROR_CODE",
    "detail": "에러 설명"
  }
}
```

서버 실행 후 [Swagger UI](http://localhost:8080/swagger-ui/index.html)에서 API 명세와 요청/응답 예시를 확인하고 호출할 수 있습니다.

## 개발 규칙

개발 규칙은 [개발규칙.md](./개발규칙.md)를 확인합니다.
