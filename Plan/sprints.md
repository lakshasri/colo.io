# colo.io — Sprint Document

---

## Sprint 1 — Foundation
**Goal:** Working auth + user management

| # | Task |
|---|---|
| 1.1 | Spring Boot project setup (PostgreSQL, JPA, Security, WebSocket starters) |
| 1.2 | Implement `AbstractUser` abstract class |
| 1.3 | Implement `DCAdmin`, `Technician`, `Customer`, `Manager` subclasses |
| 1.4 | Implement `UserFactory` |
| 1.5 | `UserEntity` + `UserRepository` (JPA) |
| 1.6 | `UserService` — CRUD with factory integration |
| 1.7 | JWT utility (generate, validate, parse) |
| 1.8 | Spring Security config + JWT filter chain |
| 1.9 | `AuthController` — login, refresh, logout endpoints |
| 1.10 | `UserController` — CRUD endpoints with role guards |
| 1.11 | `AuditLogEntity` + `AuditService` (AOP `@AfterReturning`) |
| 1.12 | React: Login page + JWT storage + Axios interceptor |
| 1.13 | React: Protected routes + `RoleGuard` component |
| 1.14 | React: Role-specific dashboard layout shell |

---

## Sprint 2 — Inventory Core
**Goal:** Full rack and server inventory with intelligent allocation

| # | Task |
|---|---|
| 2.1 | `Zone` domain class + `ZoneEntity` + `ZoneRepository` |
| 2.2 | `ZoneService` + `ZoneController` (CRUD) |
| 2.3 | `RackSubject` interface + `RackObserver` interface + `RackEvent` class |
| 2.4 | `Rack` domain class implementing `RackSubject` |
| 2.5 | `RackEntity` + `RackRepository` |
| 2.6 | `RackService` — CRUD + U-space tracking |
| 2.7 | `RackController` — CRUD + utilization endpoint |
| 2.8 | `ServerComponent` interface + `BaseServer` |
| 2.9 | `ServerDecorator` abstract class |
| 2.10 | `MonitoredServer`, `AlertableServer`, `ManagedServer` decorators |
| 2.11 | `ServerDecoratorFactory` — wraps `BaseServer` based on allocation state |
| 2.12 | `AllocationStrategy` interface + `AllocationContext` |
| 2.13 | `FirstFitAllocationStrategy` implementation |
| 2.14 | `BestFitAllocationStrategy` implementation |
| 2.15 | `PowerOptimizedAllocationStrategy` implementation |
| 2.16 | `ZoneAwareAllocationStrategy` implementation |
| 2.17 | `ServerEntity` + `ServerRepository` |
| 2.18 | `ServerService` — CRUD, allocate to rack, provision to customer |
| 2.19 | `ServerController` — CRUD + allocate + provision endpoints |
| 2.20 | React: Zone list + Rack list views |
| 2.21 | React: `RackVisualizer` — 42U grid showing server positions |
| 2.22 | React: Server list + server detail page |
| 2.23 | React: Server allocation form with strategy picker dropdown |

---

## Sprint 3 — Monitoring & Alerts
**Goal:** Real-time observability with WebSocket push

| # | Task |
|---|---|
| 3.1 | `Alert` domain class + `AlertType` / `AlertSeverity` enums |
| 3.2 | `AlertManager` Singleton (double-checked locking, thread-safe) |
| 3.3 | `AlertEntity` + `AlertRepository` |
| 3.4 | `AlertObserver` — `RackObserver` impl, delegates to `AlertManager` |
| 3.5 | `NotificationObserver` — `RackObserver` impl, sends in-app notifications |
| 3.6 | WebSocket config (STOMP over SockJS) + message broker setup |
| 3.7 | `DashboardObserver` — `RackObserver` impl, pushes to WebSocket topics |
| 3.8 | Register all observers on `Rack` at application startup |
| 3.9 | `ServerMetricEntity` + `ServerMetricRepository` (time-series) |
| 3.10 | `MonitoringService` Singleton — `ScheduledExecutorService` polls every 30s |
| 3.11 | `MonitoringService` — registers `MonitoredServer` wrappers, stores metrics |
| 3.12 | `AlertService` — acknowledge, list, filter alerts |
| 3.13 | `AlertController` — active alerts, acknowledge, stats endpoints |
| 3.14 | `ServerController` — add `/metrics` and `/metrics/history` endpoints |
| 3.15 | React: `AlertsPanel` — live feed via WebSocket (`/topic/alerts`) |
| 3.16 | React: `MetricsChart` — Chart.js CPU/RAM/Disk over time |
| 3.17 | React: Server detail page — live metrics + alert history |
| 3.18 | React: `useWebSocket` hook for STOMP subscription management |
| 3.19 | React: Rack detail — real-time power + U-space bars |

---

## Sprint 4 — Maintenance & Reports
**Goal:** Full maintenance lifecycle + manager analytics

| # | Task |
|---|---|
| 4.1 | `MaintenanceCommand` interface + `CommandResult` class |
| 4.2 | `ScheduleMaintenanceCommand` — creates schedule, notifies customers |
| 4.3 | `AssignTechnicianCommand` — assigns technician (stores previous for undo) |
| 4.4 | `StartMaintenanceCommand` — transitions status `PENDING → IN_PROGRESS` |
| 4.5 | `CompleteMaintenanceCommand` — finalizes + writes audit entry |
| 4.6 | `CancelMaintenanceCommand` — cancels + notifies affected customers |
| 4.7 | `MaintenanceCommandInvoker` — execute, undo, redo with `Deque` history |
| 4.8 | `MaintenanceScheduleEntity` + `ChecklistItemEntity` + repositories |
| 4.9 | `MaintenanceService` — orchestrates invoker, checklist management |
| 4.10 | `MaintenanceController` — schedule, assign, start, complete, cancel, undo endpoints |
| 4.11 | Manager approval endpoint (`PUT /api/maintenance/{id}/approve`) |
| 4.12 | `NotificationService` — in-app notification on schedule create/cancel/complete |
| 4.13 | `ReportService` — capacity report (zone/rack utilization breakdown) |
| 4.14 | `ReportService` — server utilization summary (avg CPU/RAM/Disk per customer) |
| 4.15 | `ReportService` — maintenance history report (completion rate, avg duration) |
| 4.16 | `ReportController` + `DashboardController` (role-specific KPI response) |
| 4.17 | `AuditController` — paginated audit trail endpoint (DC Admin only) |
| 4.18 | React: Maintenance calendar view |
| 4.19 | React: Maintenance detail + checklist tick-off UI |
| 4.20 | React: Reports page — capacity charts, utilization table |
| 4.21 | React: Role-specific dashboard KPI cards (finalize all 4 roles) |
| 4.22 | End-to-end testing + bug fixes |

---

---

## Sprint 5 — Polish & Production Readiness
**Goal:** User management, search/filter, validation, Swagger docs, export, and UX polish

| # | Task |
|---|---|
| 5.1 | `UserController` — list, get by ID, update role, deactivate endpoints |
| 5.2 | React: User management page (DC Admin only) |
| 5.3 | Bean Validation (`@Valid`) on all controller request bodies |
| 5.4 | `GlobalExceptionHandler` — validation error responses with field-level messages |
| 5.5 | Pagination support for `/api/servers` and `/api/racks` |
| 5.6 | Server search endpoint — filter by status, hostname, zone |
| 5.7 | Rack search endpoint — filter by zone, status, power threshold |
| 5.8 | `HealthController` — system health endpoint (DB ping, server count, alert count) |
| 5.9 | Swagger/OpenAPI 3 setup with springdoc-openapi |
| 5.10 | `@Operation` and `@Tag` annotations on all controllers |
| 5.11 | CSV export for server utilization report (`/api/reports/utilization/export`) |
| 5.12 | CSV export for capacity report (`/api/reports/capacity/export`) |
| 5.13 | React: Search and filter bar on Server list page |
| 5.14 | React: Search and filter bar on Rack list page |
| 5.15 | React: User management table with role-change modal |
| 5.16 | React: Notification bell in header using WebSocket `/topic/maintenance` |
| 5.17 | React: Maintenance calendar link in sidebar for all relevant roles |
| 5.18 | React: Dark/light theme toggle in AppLayout header |
| 5.19 | `README.md` — setup, architecture overview, design patterns used |
| 5.20 | Final bug fixes, security hardening, and code cleanup |

---

## Task Count Summary

| Sprint | Tasks | Focus |
|---|---|---|
| Sprint 1 | 14 | Auth, user hierarchy, factory, JWT |
| Sprint 2 | 23 | Zones, racks, servers, strategy, decorator |
| Sprint 3 | 19 | Observer, alerts, monitoring, WebSocket |
| Sprint 4 | 22 | Command, maintenance, reports, dashboard |
| Sprint 5 | 20 | Users, search, validation, Swagger, export, UX |
| **Total** | **98** | |
