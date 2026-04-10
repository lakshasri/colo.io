# UE23CS352B — Object Oriented Analysis & Design

# Mini Project Report

## colo.io — Server & Rack Management System

**Submitted by:**

| NAME | SRN |
|------|-----|
|      |     |
|      |     |
|      |     |
|      |     |

**Semester — Section:**

**Faculty Name:**

January – May 2026

---

**DEPARTMENT OF COMPUTER SCIENCE AND ENGINEERING**
FACULTY OF ENGINEERING
**PES UNIVERSITY**
(Established under Karnataka Act No. 16 of 2013)
100ft Ring Road, Bengaluru – 560 085, Karnataka, India

---

## Problem Statement

Modern colocation data centers face significant challenges in managing server lifecycles, rack capacity, customer SLAs, and power budgets across multiple zones. Manual tracking leads to over-provisioning, missed maintenance windows, and SLA violations.

**colo.io** is a full-stack Colocation Server & Rack Management System (SRMS) that automates server lifecycle management, intelligent rack allocation, maintenance workflows, and real-time power monitoring for a multi-zone data center facility. The system provides a role-based web application with real-time WebSocket metrics, automated allocation strategies, and undo/redo-capable maintenance command execution.

### Key Features

- **Server Lifecycle Management** — Servers transition through states: `UNALLOCATED → OPERATIONAL → MAINTENANCE → FAULTY → DECOMMISSIONED`, enforced by status guards at the service layer.
- **Intelligent Rack Allocation** — Four pluggable strategies: FirstFit, BestFit, PowerOptimized, ZoneAware — selected at runtime via the Strategy Pattern.
- **Maintenance Ticket Workflow** — Full lifecycle (OPEN → PENDING → IN_PROGRESS → RESOLVED / CANCELLED) with undo/redo capability using the Command Pattern.
- **SLA Management** — Customer SLAs with ACTIVE / SUSPENDED / EXPIRED states, uptime tracking, and auto-expiry on end date.
- **Real-time Monitoring** — WebSocket (STOMP over SockJS) pushes live CPU, RAM, and disk metrics to role-specific dashboards.
- **Per-Zone Power Budget Enforcement** — Zone-level power cap checked before every allocation; rejected with `422` if exceeded.
- **Bulk Operations** — Batch status updates and batch decommission via dedicated bulk endpoint.
- **Role-Based Access Control** — Four roles: `DC_ADMIN`, `MANAGER`, `TECHNICIAN`, `CUSTOMER` — each with a tailored dashboard and JWT-secured endpoints.
- **Audit Trail & Reporting** — Every state change is logged; CSV export available for server and rack reports.

---

## Models

### Use Case Diagram

> \<insert image here — diagrams/use-case-diagram.drawio\>

The use case diagram captures the four actors (**DC_ADMIN**, **MANAGER**, **TECHNICIAN**, **CUSTOMER**) and their interactions with the system:

- **DC_ADMIN** — register servers, manage racks/zones, allocate servers, trigger bulk operations, manage users, view power budgets.
- **MANAGER** — approve maintenance tickets, view SLAs and reports, view analytics.
- **TECHNICIAN** — start/complete/cancel assigned maintenance tickets.
- **CUSTOMER** — view their allocated servers, monitor live metrics, view their SLAs.

---

### Class Diagram

> \<insert image here — diagrams/class-diagram.drawio\>

The class diagram shows the full domain model including:

- `AbstractUser` hierarchy (`DCAdmin`, `Manager`, `Technician`, `Customer`)
- `ServerComponent` / `ServerDecorator` hierarchy (Decorator Pattern)
- `Rack` as the Observable subject with `RackObserver` implementations
- `AllocationStrategy` interface and its four concrete implementations
- `ServerCommand` interface with `CommandInvoker` and all concrete commands
- JPA entities: `SlaEntity`, `MaintenanceTicket`, `ServerMetric`, `AlertEntity`, `Zone`

---

### State Diagrams

**State 1 — Server Status**

> \<insert image here — diagrams/state-server-status.drawio\>

A server begins `UNALLOCATED` after registration. Once allocated and provisioned it becomes `OPERATIONAL`. From there it can enter `MAINTENANCE` (scheduled window), `FAULTY` (hardware alert), or `DECOMMISSIONED` (end of life). A `FAULTY` server is routed through `MAINTENANCE` for repair before returning to `OPERATIONAL`. `DECOMMISSIONED` is a terminal state that triggers rack capacity and power reclamation.

---

**State 2 — Maintenance Ticket**

> \<insert image here — diagrams/state-maintenance-ticket.drawio\>

A ticket starts `OPEN` upon creation via `POST /api/maintenance` (executed by `ScheduleMaintenanceCommand`). A manager approves it, moving it to `PENDING`. A technician starts work (`StartMaintenanceCommand`) → `IN_PROGRESS`. From there it either reaches `RESOLVED` (`CompleteMaintenanceCommand`) or `CANCELLED` (`CancelMaintenanceCommand`). Cancellation is also allowed from `OPEN` and `PENDING`. All transitions are reversible via `POST /commands/undo`.

---

**State 3 — SLA Lifecycle**

> \<insert image here — diagrams/state-sla-lifecycle.drawio\>

An SLA is created `ACTIVE`. A DC_ADMIN or MANAGER can `SUSPEND` it (pausing the SLA clock) and later resume it back to `ACTIVE`. It becomes `EXPIRED` either manually via `PATCH /sla/{id}/status` or automatically when `endDate` is passed (detected by `updateStatus`). Once expired or deleted, the lifecycle ends.

---

**State 4 — User Session (JWT)**

> \<insert image here — diagrams/state-user-session.drawio\>

A user begins `UNAUTHENTICATED`. On `POST /api/auth/login`, the system enters `AUTHENTICATING` — verifying credentials via `UserDetailsServiceImpl` and BCrypt. On success, a JWT access token + refresh token are issued and the session becomes `AUTHENTICATED`, routing to the role-specific dashboard. If the access token expires, the session enters `TOKEN_EXPIRED`; a valid refresh token restores `AUTHENTICATED`. Logout transitions to the terminal state.

---

### Activity Diagrams

**Activity 1 — Authentication Flow (JWT Login)**

> \<insert image here — diagrams/activity-authentication.drawio\>

The authentication flow begins with the user submitting credentials via `POST /api/auth/login`. `UserDetailsServiceImpl.loadUserByUsername()` fetches the user from the database. If not found, `401 Unauthorized` is returned immediately. If found, BCrypt verifies the password hash. On match, `JwtUtil` generates an access token (role-embedded) and a refresh token; the response returns both. The frontend stores the access token in `localStorage` and navigates to the role dashboard. All subsequent requests are intercepted by `JwtAuthFilter`, which validates the Bearer token and populates the `SecurityContext`.

---

**Activity 2 — Server Registration & Allocation (Strategy Pattern)**

> \<insert image here — diagrams/activity-server-allocation.drawio\>

A DC_ADMIN registers a server via `POST /api/servers`. The system checks hostname uniqueness in the database; duplicates return `409 Conflict`. On success, `ServerDecoratorFactory.wrap(server, BARE)` creates the initial `BareMetalServer` decorator with status `UNALLOCATED`.

Allocation is triggered via `POST /api/servers/{id}/allocate?strategy=BEST_FIT`. `AllocationContext` selects the requested strategy (`FirstFit`, `BestFit`, `PowerOptimized`, or `ZoneAware`) and calls `strategy.allocate(server, availableRacks)`. If no suitable rack is found, `422 Unprocessable Entity` is returned. On success, rack `usedUSpace` and `currentPowerKw` are updated, the server is wrapped with `MonitoredServer`, status → `OPERATIONAL`, and Rack observers are notified.

---

**Activity 3 — Maintenance Ticket Lifecycle (Command Pattern)**

> \<insert image here — diagrams/activity-maintenance-lifecycle.drawio\>

The diagram is structured as a swimlane across three actors: **DC_ADMIN/MANAGER**, **TECHNICIAN**, and **System (CommandInvoker)**.

A DC_ADMIN posts `POST /api/maintenance`. The `CommandInvoker` executes `ScheduleMaintenanceCommand`, creating the ticket (status `OPEN`) and pushing it onto the history stack. `NotificationService` pushes a WebSocket notification to `/topic/maintenance`.

The manager approves the ticket (`PUT /{id}/approve` → status `PENDING`). DC_ADMIN assigns a technician (`PUT /{id}/assign`), executing `AssignTechnicianCommand`. The technician starts work (`PUT /{id}/start` → `StartMaintenanceCommand` → status `IN_PROGRESS`; server status → `MAINTENANCE`).

On completion, `CompleteMaintenanceCommand` sets status `RESOLVED`; on cancellation, `CancelMaintenanceCommand` sets status `CANCELLED`. At any point, `POST /commands/undo` reverses the last command, and `POST /commands/redo` re-executes the undone command.

---

**Activity 4 — SLA Management Workflow**

> \<insert image here — diagrams/activity-sla-management.drawio\>

A DC_ADMIN or MANAGER creates an SLA via `POST /api/sla` with customer ID, name, uptime %, response time, resolution hours, and date range. The system verifies the customer exists (404 if not). On success, a `SlaEntity` is saved with status `ACTIVE` and `201 Created` is returned.

The system continuously monitors uptime and response times while the SLA is active. A manager can suspend it (`PATCH /sla/{id}/status {status: SUSPENDED}`), force-expire it (`PATCH ... {status: EXPIRED}`), or let it auto-expire when `endDate` passes. A suspended SLA can be resumed back to `ACTIVE`. Once expired (by any route), the record can be deleted via `DELETE /api/sla/{id}`.

---

## Design Principles and Design Patterns

### MVC Architecture: **Yes**

The application follows a strict three-layer MVC / layered architecture:

| Layer | Technology | Responsibility |
|-------|-----------|----------------|
| **View (Frontend)** | React 18, Axios, Chart.js | UI rendering, user interaction, WebSocket subscriptions |
| **Controller** | Spring MVC `@RestController` | HTTP boundary only — parses requests, delegates to service, returns ResponseEntity |
| **Model / Service** | Spring `@Service`, JPA Entities | All business logic, pattern orchestration, transaction management |
| **Repository** | Spring Data JPA `@Repository` | Data access only — no business logic |

Controllers contain zero business logic. All decisions (allocation, status transitions, command execution) live exclusively in the Service layer. Domain entities are pure Java POJOs with no Spring dependencies, making them fully unit-testable in isolation.

---

### Design Principles

**1. Single Responsibility Principle (SRP)**

Each service class owns exactly one domain concern: `ServerService` manages server lifecycle, `SlaService` manages SLA state transitions, `PowerBudgetService` enforces zone power caps, `MaintenanceService` orchestrates ticket workflow, and `MonitoringService` handles metric polling. No service bleeds into another's domain. Similarly, `JwtUtil` is responsible only for token generation and validation — it does not touch user storage or session management.

**2. Open/Closed Principle (OCP)**

The allocation subsystem is open for extension and closed for modification. Adding a new allocation strategy (e.g., `CostOptimizedStrategy`) requires only: (1) implementing `AllocationStrategy`, (2) registering it in `AllocationContext`. The existing `AllocationContext`, `ServerService`, and controller code require zero changes. This is directly demonstrated by the four existing strategies (`FirstFitStrategy`, `BestFitStrategy`, `PowerOptimizedStrategy`, `ZoneAwareStrategy`) coexisting without any conditional branching in the context class.

**3. Liskov Substitution Principle (LSP)**

`MonitoredServer` and `BareMetalServer` both extend `ServerDecorator` which implements `ServerComponent`. Any code that accepts a `ServerComponent` (e.g., `AllocationStrategy.allocate()`, `ServerService.getResourceSummary()`) works identically whether passed a `BaseServer`, `BareMetalServer`, or `MonitoredServer`. No subtype breaks the contract of the parent interface.

**4. Interface Segregation Principle (ISP)**

Interfaces are kept narrow and role-specific. `AllocationStrategy` exposes only `allocate(server, racks)`. `RackObserver` exposes only `onRackUpdated(rack)`. `ServerCommand` exposes only `execute()` and `undo()`. No implementing class is forced to stub out methods it doesn't need.

**5. Dependency Inversion Principle (DIP)**

Controllers depend on service interfaces, not concrete classes. `AllocationContext` depends on `AllocationStrategy` (interface), not any concrete strategy. `Rack` depends on `RackObserver` (interface), not `AlertObserver` or `DashboardObserver` directly. Spring's IoC container injects concrete implementations at runtime, keeping the high-level modules fully decoupled from low-level details.

---

### Design Patterns

**1. Strategy Pattern** — *Allocation*

`AllocationStrategy` is the strategy interface. `AllocationContext` holds a reference to the selected strategy and calls `strategy.allocate(server, availableRacks)` without knowing the concrete algorithm.

| Concrete Strategy | Algorithm |
|---|---|
| `FirstFitStrategy` | Returns the first rack with sufficient U-space and power headroom |
| `BestFitStrategy` | Returns the rack that minimises remaining U-space after allocation |
| `PowerOptimizedStrategy` | Selects the rack in the zone with the lowest current power draw |
| `ZoneAwareStrategy` | Filters racks to the preferred zone, then applies best-fit |

The strategy is selected at runtime from the query parameter `?strategy=BEST_FIT` passed to `POST /api/servers/{id}/allocate`. Adding a new strategy requires no changes to any existing class.

---

**2. Command Pattern** — *Maintenance Workflow with Undo/Redo*

`ServerCommand` interface defines `execute()` and `undo()`. `CommandInvoker` maintains two stacks — `history` and `redoStack` — and exposes `executeCommand()`, `undo()`, `redo()`.

| Command | `execute()` | `undo()` |
|---|---|---|
| `ScheduleMaintenanceCommand` | Creates ticket, status = `OPEN` | Deletes the ticket |
| `AssignTechnicianCommand` | Sets technician, status = `PENDING` | Clears technician, reverts status |
| `StartMaintenanceCommand` | status = `IN_PROGRESS`, sets `startedAt` | Reverts to `PENDING`, clears `startedAt` |
| `CompleteMaintenanceCommand` | status = `RESOLVED`, sets `resolvedAt` | Reverts to `IN_PROGRESS`, clears `resolvedAt` |
| `CancelMaintenanceCommand` | status = `CANCELLED` | Reverts to previous status |
| `DecommissionServerCommand` | Server status = `DECOMMISSIONED`, reclaims rack capacity | Restores server status and rack capacity |

Endpoints: `POST /commands/undo` and `POST /commands/redo`.

---

**3. Observer Pattern** — *Rack State Notifications*

`Rack` implements the subject role, maintaining a list of `RackObserver` instances. When rack state changes (server allocated, server decommissioned, threshold crossed), `Rack.notifyObservers(event)` is called.

| Observer | Behaviour |
|---|---|
| `AlertObserver` | Creates an `AlertEntity` in the database if power or U-space threshold is exceeded |
| `DashboardObserver` | Pushes a WebSocket message to `/topic/rack-updates` for live dashboard refresh |

This decouples the `Rack` domain object from the notification infrastructure — `Rack` has no knowledge of alerting or WebSocket concerns.

---

**4. Decorator Pattern** — *Server Capability Extension*

`ServerComponent` is the component interface. `ServerDecorator` is the abstract decorator. Concrete decorators add capabilities without modifying `BaseServer`.

| Decorator | Adds |
|---|---|
| `BareMetalServer` | Physical hardware metadata (vendor, model, serial number) |
| `MonitoredServer` | Live metrics fields (CPU %, RAM %, disk %) and `lastPolledAt`; `MonitoringService` polls it |

`ServerDecoratorFactory.wrap(server, type)` creates the appropriate decorator chain. A server starts as `BareMetalServer` (`UNALLOCATED`) and is upgraded to `MonitoredServer` upon allocation (`OPERATIONAL`).

---

**5. Factory Pattern** — *Decorator Construction*

`ServerDecoratorFactory` centralises all decorator construction logic. Callers (`ServerService`) request a wrapped server by type (`BARE`, `MONITORED`) without knowing which decorator class is instantiated. This ensures consistent initialisation and future extensibility (e.g., adding a `HighAvailabilityServer` decorator requires changing only the factory).

---

**6. Singleton Pattern** — *CommandInvoker*

`CommandInvoker` is a Spring `@Component` (singleton-scoped by default). Only one `CommandInvoker` instance exists in the application context, ensuring the command history stacks (`history`, `redoStack`) are shared across all requests. This guarantees that an undo operation from any API call reverses the globally last executed command.

---

## GitHub Link to the Codebase

> https://github.com/

*(Repository should be public before submission)*

---

## Screenshots

### UI

> \<insert screenshot — Login page\>

> \<insert screenshot — Admin Dashboard (server list, real-time metrics)\>

> \<insert screenshot — Rack Detail (zone power budget widget)\>

> \<insert screenshot — Maintenance Ticket List\>

> \<insert screenshot — SLA Management page\>

> \<insert screenshot — Bulk Operations (ServerList row selection)\>

---

## Individual Contributions of Team Members

| Name | Module worked on |
|------|-----------------|
|      |                 |
|      |                 |
|      |                 |
|      |                 |
