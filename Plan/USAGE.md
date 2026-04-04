# colo.io — Usage Guide

How to run, test, and use the system locally.

---

## Prerequisites

| Tool | Version |
|---|---|
| Java | 21+ |
| Maven | 3.9+ |
| Node.js | 18+ |
| PostgreSQL | 15+ |

---

## 1. Database Setup

```sql
-- Run once in psql
CREATE DATABASE srms_db;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE srms_db TO postgres;
```

The schema auto-creates on first boot (`ddl-auto=update`).

---

## 2. Running the Backend

```bash
cd backend
mvn spring-boot:run
```

Starts on **http://localhost:8080**

- Swagger UI: http://localhost:8080/swagger-ui.html
- API docs: http://localhost:8080/api-docs
- Health check: http://localhost:8080/api/health

### Running tests

```bash
cd backend
mvn test
```

---

## 3. Running the Frontend

```bash
cd frontend
npm install       # first time only
npm run dev
```

Opens on **http://localhost:3000**

The dev server proxies `/api` and `/ws` to the backend automatically.

---

## 4. Default Roles & Login

Create users via `POST /api/auth/register` or seed directly. The system supports four roles:

| Role | Access |
|---|---|
| `DC_ADMIN` | Full system access — users, servers, racks, zones, SLA, bulk ops |
| `MANAGER` | Reports, maintenance approval, SLA management, capacity view |
| `TECHNICIAN` | Maintenance tasks, server status updates, alerts |
| `CUSTOMER` | Own servers, alerts, maintenance visibility, SLA view |

### Auth flow

```
POST /api/auth/login
Body: { "username": "...", "password": "..." }
Response: { "accessToken": "...", "refreshToken": "..." }
```

Include the access token as `Authorization: Bearer <token>` on all subsequent requests.

---

## 5. Key Workflows

### Register and allocate a server
1. `POST /api/servers` — register server (status = UNALLOCATED)
2. `POST /api/servers/{id}/allocate?strategy=BEST_FIT` — auto-assign to a rack
3. `POST /api/servers/{id}/provision?customerId=...` — assign to a customer

**Allocation strategies:** `FIRST_FIT`, `BEST_FIT`, `POWER_OPTIMIZED`, `ZONE_AWARE`

### Create a maintenance ticket
1. `POST /api/maintenance` — schedule ticket
2. `PUT /api/maintenance/{id}/assign?technicianId=...` — assign technician
3. `PUT /api/maintenance/{id}/start` — begin work
4. `PUT /api/maintenance/{id}/complete` — mark done

Undo/redo any step:
```
POST /api/maintenance/commands/undo
POST /api/maintenance/commands/redo
```

### Bulk server operations (DC_ADMIN / MANAGER)
```
PATCH /api/bulk/servers/status
Body: { "serverIds": [1, 2, 3], "status": "MAINTENANCE" }

POST /api/bulk/servers/decommission
Body: { "serverIds": [1, 2] }
```

### Zone power budget
```
GET /api/power/zones                        — all zones summary
GET /api/power/zones/{zoneId}               — single zone
GET /api/power/zones/{zoneId}/can-allocate?kw=5.0
```

### SLA management
```
GET    /api/sla                    — list all SLAs
POST   /api/sla                    — create SLA (DC_ADMIN / MANAGER)
PATCH  /api/sla/{id}/status        — update status (ACTIVE / SUSPENDED / EXPIRED)
DELETE /api/sla/{id}               — delete SLA
```

---

## 6. Real-time (WebSocket)

Connect via STOMP over SockJS at `/ws`. Topics:

| Topic | Payload |
|---|---|
| `/topic/alerts` | New alert fired |
| `/topic/maintenance` | Ticket state change |
| `/topic/rack/{rackId}` | Rack U-space / power update |
| `/topic/metrics/{serverId}` | Live CPU / RAM / disk metrics |

---

## 7. Reports & Exports

| Endpoint | Description |
|---|---|
| `GET /api/reports/capacity` | Zone capacity summary |
| `GET /api/reports/utilization` | Per-server avg CPU/RAM/Disk |
| `GET /api/reports/maintenance-history` | Completion rate, avg duration |
| `GET /api/reports/utilization/export` | Download utilization CSV |
| `GET /api/reports/capacity/export` | Download capacity CSV |

---

## 8. Design Patterns Reference

| Pattern | Where |
|---|---|
| Factory | `UserFactory` (creates role-typed users), `ServerDecoratorFactory` |
| Strategy | `AllocationStrategy` — FirstFit, BestFit, PowerOptimized, ZoneAware |
| Observer | `Rack` subject → `AlertObserver`, `NotificationObserver`, `DashboardObserver` |
| Command | `CommandInvoker` with undo/redo — maintenance lifecycle + decommission |
| Decorator | `ServerComponent` chain — `MonitoredServer`, `AlertableServer`, `ManagedServer` |
| Singleton | `AlertManager`, `MonitoringService` (double-checked locking) |
