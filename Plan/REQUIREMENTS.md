# colo.io — Requirements

All dependencies and tools needed to build and run the system.

---

## System Requirements

| Tool | Version | Install |
|---|---|---|
| Java (JDK) | 21+ | `sudo apt install openjdk-21-jdk` |
| Maven | 3.9+ | `sudo apt install maven` |
| Node.js | 18+ | `sudo apt install nodejs npm` |
| PostgreSQL | 15+ | `sudo apt install postgresql-15` |
| Git | any | `sudo apt install git` |

---

## Database Setup (one-time)

```bash
sudo -u postgres createdb srms_db
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

Config in `backend/src/main/resources/application.properties`:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/srms_db
spring.datasource.username=postgres
spring.datasource.password=postgres
```

---

## Backend Dependencies (`backend/pom.xml`)

| Dependency | Version | Purpose |
|---|---|---|
| spring-boot-starter-web | 3.x | REST API |
| spring-boot-starter-security | 3.x | JWT auth + role-based access |
| spring-boot-starter-data-jpa | 3.x | PostgreSQL ORM |
| spring-boot-starter-websocket | 3.x | STOMP WebSocket |
| spring-boot-starter-aop | 3.x | Audit trail aspect |
| springdoc-openapi-starter-webmvc-ui | 2.6.0 | Swagger UI at /swagger-ui.html |
| jjwt-api / jjwt-impl / jjwt-jackson | 0.12.x | JWT token signing/validation |
| postgresql driver | 42.x | JDBC driver |
| spring-boot-starter-test | 3.x | JUnit 5 + Mockito |
| lombok | 1.18.x | (optional) boilerplate reduction |

---

## Frontend Dependencies (`frontend/package.json`)

| Package | Version | Purpose |
|---|---|---|
| react | 18.x | UI framework |
| react-dom | 18.x | DOM rendering |
| react-router-dom | 6.x | Client-side routing |
| antd | 5.x | UI component library |
| @ant-design/icons | 5.x | Icon set |
| axios | 1.x | HTTP client |
| chart.js | 4.x | Metrics charts |
| react-chartjs-2 | 5.x | Chart.js React wrapper |
| @stomp/stompjs | 7.x | WebSocket STOMP client |
| sockjs-client | 1.x | SockJS fallback transport |
| dayjs | 1.x | Date handling (used by antd) |
| vite | 5.x | Dev server + build tool |
| @vitejs/plugin-react | 4.x | React fast refresh |

Install all frontend deps:
```bash
cd frontend && npm install
```

---

## Running the Project

```bash
# Terminal 1 — backend
cd backend
mvn spring-boot:run

# Terminal 2 — frontend
cd frontend
npm run dev
```

- Backend: http://localhost:8080
- Frontend: http://localhost:3000
- Swagger: http://localhost:8080/swagger-ui.html

---

## Demo Accounts (auto-seeded on first boot)

| Username | Password | Role |
|---|---|---|
| admin | admin123 | DC_ADMIN |
| manager | admin123 | MANAGER |
| tech | admin123 | TECHNICIAN |
| customer | admin123 | CUSTOMER |

---

## Running Tests

```bash
cd backend
mvn test
```

Test files:
- `UserFactoryTest` — Factory pattern, 5 tests
- `AllocationStrategyTest` — Strategy pattern, 7 tests
- `CommandInvokerTest` — Command pattern, 8 tests
- `DecommissionServerCommandTest` — Command, 5 tests
- `ReportServiceTest` — Service, 4 tests
- `SlaServiceTest` — Service, 7 tests
- `MaintenanceServiceTest` — Service, 7 tests
