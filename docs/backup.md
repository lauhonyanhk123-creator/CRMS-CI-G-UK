# CRMS CI G UK Configuration Reference

Complete reference for all configuration options.

## Environment Variables

### Database Configuration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `POSTGRES_PASSWORD` | Yes | - | PostgreSQL password |
| `SPRING_DATASOURCE_URL` | Yes | jdbc:postgresql://postgres:5432/crms | JDBC connection URL |
| `SPRING_DATASOURCE_USERNAME` | Yes | crms | Database username |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | No | validate | Hibernate schema mode (validate/create/update) |

### MinIO Configuration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `MINIO_ROOT_USER` | Yes | - | MinIO root access key |
| `MINIO_ROOT_PASSWORD` | Yes | - | MinIO root secret key |
| `MINIO_ENDPOINT` | No | http://minio:9000 | MinIO server endpoint |
| `MINIO_BUCKET` | No | crms | Default bucket name |

### Security Configuration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `APP_SECRET_KEY` | Yes | - | Application encryption key (min 32 chars) |
| `JWT_SECRET` | Yes | - | JWT signing key (min 64 hex chars) |
| `JWT_EXPIRATION` | No | 900 | JWT expiration in seconds |
| `SESSION_TIMEOUT` | No | 3600 | Session timeout in seconds |

### HMRC Integration

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `HMRC_API_MODE` | No | sandbox | API mode (sandbox/production) |
| `HMRC_CLIENT_ID` | No | - | HMRC OAuth client ID |
| `HMRC_CLIENT_SECRET` | No | - | HMRC OAuth client secret |

### Application Settings

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `SPRING_PROFILES_ACTIVE` | No | prod | Spring profile (dev/test/prod) |
| `TZ` | No | Europe/London | Timezone |
| `JAVA_OPTS` | No | -Xms512m -Xmx2g | JVM options |
| `SERVER_PORT` | No | 8080 | Backend server port |

## Application Properties

### Backend (application.yml)

```yaml
server:
  port: ${SERVER_PORT:8080}
  shutdown: graceful
  tomcat:
    max-threads: 200
    connection-timeout: 30000

spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: ${SPRING_JPA_HIBERNATE_DDL_AUTO:validate}
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: false
        jdbc:
          time_zone: Europe/London

  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: when_authorized

minio:
  endpoint: ${MINIO_ENDPOINT:http://minio:9000}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket: ${MINIO_BUCKET:crms}

security:
  jwt:
    secret: ${JWT_SECRET}
    expiration: ${JWT_EXPIRATION:900}
```

## Feature Flags

Enable/disable specific features via environment variables:

```env
FEATURE_CIS_ENABLED=true
FEATURE_CSCS_ENABLED=true
FEATURE_ADOPTIONS_ENABLED=true
FEATURE_PWA_OFFLINE_ENABLED=true
FEATURE_REPORTING_ENABLED=true
```

## Logging Configuration

```env
LOG_LEVEL=INFO
LOG_FILE=/data/logs/application.log
LOG_PATTERN=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
```

## File Storage

```env
STORAGE_PATH=/data
MAX_UPLOAD_SIZE=104857600  # 100MB
ALLOWED_FILE_TYPES=pdf,jpg,jpeg,png,doc,docx,xls,xlsx,dwg,dxf
```

## Email Configuration (Optional)

```env
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=noreply@crms-ci-g-uk.co.uk
SMTP_PASSWORD=password
SMTP_FROM=CRMS <noreply@crms-ci-g-uk.co.uk>
```

## Integration APIs

### Companies House
```env
COMPANIES_HOUSE_API_KEY=your-api-key
```

### Open Banking
```env
OPEN_BANKING_BASE_URL=https://openbanking.api.uk
OPEN_BANKING_CLIENT_ID=your-client-id
```

## Rate Limiting

```env
RATE_LIMIT_ENABLED=true
RATE_LIMIT_REQUESTS_PER_MINUTE=100
RATE_LIMIT_BURST=20
```

## Session Management

```env
SESSION_COOKIE_NAME=CRMS_SESSION
SESSION_COOKIE_SECURE=true
SESSION_COOKIE_HTTP_ONLY=true
SESSION_COOKIE_SAMESITE=strict
```
