# colo.io — Server & Rack Management System
## Complete Project Plan

---

## 1. Executive Summary

**colo.io** is a full-stack colocation data center management platform that enables real-time monitoring, intelligent server allocation, and lifecycle-managed maintenance workflows across a multi-zone data center facility.

Built with **Spring Boot + PostgreSQL + React**, the system demonstrates enterprise-grade Object-Oriented design through six concrete design patterns: Factory, Strategy, Observer, Command, Decorator, and Singleton — each solving a real domain problem rather than being applied for its own sake.

| Property | Value |
|---|---|
| Domain | Infrastructure / Data Center Management |
| Backend | Java 21, Spring Boot 3.x, Spring Security, JPA/Hibernate |
| Frontend | React 18, Axios, Chart.js, Ant Design |
| Database | PostgreSQL 15 |
| Auth | JWT (Access + Refresh tokens) |
| Real-time | WebSocket (STOMP over SockJS) |
| Team | 2 members |

---

## 2. System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     React Frontend                          │
│   Login │ Dashboard │ Racks │ Servers │ Alerts │ Reports    │
└───────────────────────┬─────────────────────────────────────┘
                        │ REST + WebSocket
┌───────────────────────▼─────────────────────────────────────┐
│                  Spring Boot Backend                        │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │  Controller  │  │   Security   │  │    WebSocket     │  │
│  │    Layer     │  │  (JWT Filter)│  │    Handler       │  │
│  └──────┬───────┘  └──────────────┘  └──────────────────┘  │
│         │                                                   │
│  ┌──────▼───────────────────────────────────────────────┐  │
│  │                   Service Layer                       │  │
│  │  UserService │ RackService │ ServerService            │  │
│  │  MaintenanceService │ AlertService │ MonitoringService │  │
│  │  ZoneService │ AuditService │ ReportService           │  │
│  └──────┬───────────────────────────────────────────────┘  │
│         │                                                   │
│  ┌──────▼────────────────────────────────────────────────┐  │
│  │                  Domain / Model Layer                  │  │
│  │  AbstractUser hierarchy │ ServerComponent hierarchy    │  │
│  │  Rack (Observable) │ Zone │ Alert │ Maintenance        │  │
│  │  Design Pattern implementations                        │  │
│  └──────┬────────────────────────────────────────────────┘  │
│         │                                                   │
│  ┌──────▼───────────────────────────────────────────────┐  │
│  │                 Repository Layer (JPA)                 │  │
│  │  UserRepo │ RackRepo │ ServerRepo │ ZoneRepo           │  │
│  │  AlertRepo │ MaintenanceRepo │ AuditRepo              │  │
│  └──────┬───────────────────────────────────────────────┘  │
└─────────┼───────────────────────────────────────────────────┘
          │
┌─────────▼───────────────────┐
│     PostgreSQL Database     │
└─────────────────────────────┘
```

### Layered Architecture Principles
- **Controller Layer** — HTTP boundary only. No business logic. Maps DTOs ↔ domain objects.
- **Service Layer** — All business logic, pattern orchestration, transaction management.
- **Domain Layer** — Pure OOP model. No Spring dependencies. Fully unit-testable.
- **Repository Layer** — Data access only. Spring Data JPA interfaces.

---

## 3. OOP Design — Class Hierarchies

### 3.1 User Hierarchy (Abstract Class + Inheritance)

```
AbstractUser  (abstract class)
├── userId: Long
├── username: String
├── email: String
├── passwordHash: String
├── createdAt: LocalDateTime
├── isActive: boolean
│
├── + authenticate(rawPassword): boolean          // shared
├── + getRole(): UserRole                         // abstract
├── + getPermissions(): Set<Permission>           // abstract
└── + getDashboardSummary(): DashboardDTO         // abstract

        │
        ├─── DCAdmin
        │      └── + canManageRacks(), canManageUsers()
        │
        ├─── Technician
        │      ├── certifications: List<String>
        │      └── + canPerformMaintenance(), canUpdateServerStatus()
        │
        ├─── Customer
        │      ├── companyName: String
        │      ├── contactPhone: String
        │      └── + canViewAllocatedServers(), canMonitorResources()
        │
        └─── Manager
               └── + canViewAnalytics(), canApproveMaintenance()
```

**Why:** Role-specific behavior is encapsulated in each subclass. Polymorphic dispatch replaces if-else role checks throughout the codebase. Spring Security RBAC maps directly to `UserRole` enum values.

---

### 3.2 ServerComponent Hierarchy (Decorator Pattern)

```
ServerComponent  (interface)
├── + getServerId(): Long
├── + getHostname(): String
├── + getStatus(): ServerStatus
├── + getResourceSummary(): ResourceSummary
└── + getDescription(): String

BaseServer  (implements ServerComponent)
├── serverId, rackId, customerId: Long
├── hostname, ipAddress: String
├── uSize, uPosition: int
├── cpuCores: int, ramGb: int, diskTb: double
├── status: ServerStatus
└── installedDate: LocalDate

ServerDecorator  (abstract, implements ServerComponent)
├── wrapped: ServerComponent
└── delegates all calls to wrapped (override to extend)

    ├─── MonitoredServer  extends ServerDecorator
    │      ├── cpuUsagePercent: double
    │      ├── ramUsagePercent: double
    │      ├── diskUsagePercent: double
    │      ├── lastPolledAt: LocalDateTime
    │      └── + getResourceSummary()  // returns live metrics
    │
    ├─── AlertableServer  extends ServerDecorator
    │      ├── thresholds: Map<MetricType, Double>
    │      ├── alertObservers: List<AlertObserver>
    │      └── + checkThresholds()     // fires alerts on breach
    │
    └─── ManagedServer  extends ServerDecorator
           ├── warrantyExpiry: LocalDate
           ├── maintenanceCount: int
           └── + isUnderWarranty(), + getMaintenanceHistory()
```

**Why:** Capabilities (monitoring, alerting, maintenance tracking) are added at runtime via wrapping, not via a combinatorial explosion of subclasses (`MonitoredAlertableManagedServer` etc.). A bare `BaseServer` can be progressively decorated as it gets assigned, provisioned, and monitored.

---

### 3.3 Rack as Observable Subject (Observer Pattern)

```
RackSubject  (interface)
├── + addObserver(RackObserver o)
├── + removeObserver(RackObserver o)
└── + notifyObservers(RackEvent event)

Rack  (implements RackSubject)
├── rackId: Long
├── name, location: String
├── zoneId: Long
├── totalUSpace, usedUSpace: int
├── maxPowerKw, currentPowerKw: double
├── status: RackStatus
├── observers: List<RackObserver>   // the subscriber list
│
├── + addServer(Server s)           // triggers notifyObservers
├── + removeServer(Server s)        // triggers notifyObservers
├── + updatePower(double kw)        // triggers notifyObservers
└── + getUtilizationPercent(): double

RackObserver  (interface)
├── + onPowerThresholdExceeded(Rack rack, double pct)
├── + onUSpaceNearCapacity(Rack rack, int remaining)
└── + onStatusChanged(Rack rack, RackStatus prev, RackStatus next)

    ├─── AlertObserver
    │      └── delegates to AlertManager.getInstance().raiseAlert(...)
    │
    ├─── NotificationObserver
    │      └── sends email/push to affected Customer users
    │
    └─── DashboardObserver
           └── pushes update over WebSocket to connected clients
```

**Why:** Rack state changes (power spike, space full, status change) need to propagate to multiple independent subsystems. Adding a new reaction (e.g., a PagerDuty integration) requires only a new `RackObserver` implementation — zero changes to `Rack`.

---

## 4. Design Patterns — Implementation Map

### Pattern 1: Factory — `UserFactory`

```java
public class UserFactory {
    public static AbstractUser create(CreateUserRequest request) {
        return switch (request.getRole()) {
            case DC_ADMIN    -> new DCAdmin(request);
            case TECHNICIAN  -> new Technician(request);
            case CUSTOMER    -> new Customer(request);
            case MANAGER     -> new Manager(request);
        };
    }
}
```

**Also:** `ServerDecoratorFactory` wraps a `BaseServer` with the appropriate decorator chain based on its allocation status:
- Newly installed → `BaseServer`
- Provisioned to customer → `MonitoredServer(AlertableServer(BaseServer))`
- Under warranty → `ManagedServer(MonitoredServer(AlertableServer(BaseServer)))`

---

### Pattern 2: Strategy — `AllocationStrategy`

```java
public interface AllocationStrategy {
    Optional<AllocationResult> findSlot(List<Rack> racks, Server server);
    String getStrategyName();
}

// Implementations:
FirstFitAllocationStrategy     // first rack with enough U-space + power headroom
BestFitAllocationStrategy      // minimizes wasted U-space after placement
PowerOptimizedAllocationStrategy  // distributes load across zones evenly
ZoneAwareAllocationStrategy    // respects zone cooling + power budget constraints

// Context:
public class AllocationContext {
    private AllocationStrategy strategy;
    public void setStrategy(AllocationStrategy strategy) { ... }
    public AllocationResult allocate(List<Rack> racks, Server server) { ... }
}
```

**Used in:** `RackService.allocateServer(serverId, strategyType)`. The admin selects the strategy from the UI; the context delegates. Changing allocation logic = swapping the strategy object, nothing else.

---

### Pattern 3: Observer — `RackSubject / RackObserver`

Described in Section 3.3.

**Event types modeled as enum `RackEventType`:**
- `POWER_THRESHOLD_EXCEEDED` (>85% of max_power_kw)
- `POWER_CRITICAL` (>95%)
- `USPACE_LOW` (<5U remaining)
- `USPACE_FULL`
- `STATUS_CHANGED`
- `SERVER_ADDED` / `SERVER_REMOVED`

---

### Pattern 4: Command — `MaintenanceCommand`

```java
public interface MaintenanceCommand {
    CommandResult execute();
    void undo();
    String getDescription();
    boolean canUndo();
}

// Concrete commands:
ScheduleMaintenanceCommand     // creates schedule, notifies customers
AssignTechnicianCommand        // assigns technician (stores prev for undo)
StartMaintenanceCommand        // transitions status PENDING → IN_PROGRESS
CompleteMaintenanceCommand     // finalizes + writes audit entry
CancelMaintenanceCommand       // cancels + notifies affected customers

// Invoker with history:
public class MaintenanceCommandInvoker {
    private final Deque<MaintenanceCommand> history = new ArrayDeque<>();
    private final Deque<MaintenanceCommand> redoStack = new ArrayDeque<>();

    public CommandResult execute(MaintenanceCommand cmd) { ... }
    public void undo() { ... }
    public void redo() { ... }
    public List<String> getHistory() { ... }
}
```

**Why undo matters:** An admin reassigning a technician or cancelling a schedule needs a reversible audit-friendly operation. The invoker's history also doubles as a lightweight audit log for the maintenance module.

---

### Pattern 5: Decorator — `ServerDecorator`

Described in Section 3.2.

**Key insight:** The REST API always returns a `ServerComponent`. Callers never need to know which decorators are present — they call `getResourceSummary()` and get either stub metrics (bare server) or live metrics (monitored server), transparently.

---

### Pattern 6: Singleton — `AlertManager` & `MonitoringService`

```java
public class AlertManager {
    private static volatile AlertManager instance;
    private final Map<Long, Alert> activeAlerts = new ConcurrentHashMap<>();
    private final List<AlertListener> listeners = new CopyOnWriteArrayList<>();

    private AlertManager() {}

    public static AlertManager getInstance() {
        if (instance == null) {
            synchronized (AlertManager.class) {
                if (instance == null) instance = new AlertManager();
            }
        }
        return instance;
    }

    public Alert raiseAlert(AlertType type, Severity severity,
                            String message, String sourceId, SourceType sourceType) { ... }
    public void acknowledge(Long alertId, AbstractUser by) { ... }
    public List<Alert> getActiveAlerts() { ... }
}
```

**Double-checked locking** ensures thread safety. `MonitoringService` follows the same pattern and manages a `ScheduledExecutorService` that polls decorated servers every 30 seconds for metrics.

---

## 5. Database Schema

```sql
-- Users (single table, role discriminates subclass)
CREATE TABLE users (
    user_id      BIGSERIAL PRIMARY KEY,
    username     VARCHAR(50)  UNIQUE NOT NULL,
    email        VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role         VARCHAR(20)  NOT NULL CHECK (role IN ('DC_ADMIN','TECHNICIAN','CUSTOMER','MANAGER')),
    company_name VARCHAR(100),          -- Customer only
    created_at   TIMESTAMP    DEFAULT NOW(),
    is_active    BOOLEAN      DEFAULT TRUE
);

-- Zones
CREATE TABLE zones (
    zone_id          BIGSERIAL PRIMARY KEY,
    name             VARCHAR(50) NOT NULL,
    floor            INT,
    power_budget_kw  DECIMAL(8,2),
    cooling_capacity DECIMAL(8,2)
);

-- Racks
CREATE TABLE racks (
    rack_id          BIGSERIAL PRIMARY KEY,
    name             VARCHAR(20) NOT NULL,
    zone_id          BIGINT REFERENCES zones(zone_id),
    location         VARCHAR(100),
    total_u_space    INT DEFAULT 42,
    used_u_space     INT DEFAULT 0,
    max_power_kw     DECIMAL(8,2),
    current_power_kw DECIMAL(8,2) DEFAULT 0,
    status           VARCHAR(20) DEFAULT 'ACTIVE'
);

-- Servers
CREATE TABLE servers (
    server_id      BIGSERIAL PRIMARY KEY,
    rack_id        BIGINT REFERENCES racks(rack_id),
    customer_id    BIGINT REFERENCES users(user_id),
    hostname       VARCHAR(100) UNIQUE NOT NULL,
    ip_address     VARCHAR(45),
    u_size         INT NOT NULL,
    u_position     INT,
    cpu_cores      INT,
    ram_gb         INT,
    disk_tb        DECIMAL(8,2),
    status         VARCHAR(20) DEFAULT 'OPERATIONAL',
    installed_date DATE
);

-- Server Metrics (time-series snapshot)
CREATE TABLE server_metrics (
    metric_id        BIGSERIAL PRIMARY KEY,
    server_id        BIGINT REFERENCES servers(server_id),
    cpu_usage_pct    DECIMAL(5,2),
    ram_usage_pct    DECIMAL(5,2),
    disk_usage_pct   DECIMAL(5,2),
    recorded_at      TIMESTAMP DEFAULT NOW()
);

-- Alerts
CREATE TABLE alerts (
    alert_id         BIGSERIAL PRIMARY KEY,
    type             VARCHAR(20) NOT NULL,
    severity         VARCHAR(10) NOT NULL,
    message          TEXT NOT NULL,
    source_id        BIGINT,
    source_type      VARCHAR(20),
    created_at       TIMESTAMP DEFAULT NOW(),
    acknowledged     BOOLEAN DEFAULT FALSE,
    acknowledged_by  BIGINT REFERENCES users(user_id),
    acknowledged_at  TIMESTAMP
);

-- Maintenance Schedules
CREATE TABLE maintenance_schedules (
    schedule_id     BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    type            VARCHAR(30) NOT NULL,
    scheduled_date  TIMESTAMP NOT NULL,
    end_date        TIMESTAMP,
    assigned_to     BIGINT REFERENCES users(user_id),
    created_by      BIGINT REFERENCES users(user_id),
    status          VARCHAR(20) DEFAULT 'PENDING',
    approved_by     BIGINT REFERENCES users(user_id)
);

-- Maintenance ↔ Server (many-to-many)
CREATE TABLE maintenance_servers (
    schedule_id BIGINT REFERENCES maintenance_schedules(schedule_id),
    server_id   BIGINT REFERENCES servers(server_id),
    PRIMARY KEY (schedule_id, server_id)
);

-- Maintenance Checklist Items
CREATE TABLE checklist_items (
    item_id      BIGSERIAL PRIMARY KEY,
    schedule_id  BIGINT REFERENCES maintenance_schedules(schedule_id),
    description  TEXT NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE,
    completed_by BIGINT REFERENCES users(user_id),
    completed_at TIMESTAMP
);

-- Audit Trail
CREATE TABLE audit_log (
    log_id       BIGSERIAL PRIMARY KEY,
    user_id      BIGINT REFERENCES users(user_id),
    action       VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(30) NOT NULL,
    entity_id    BIGINT,
    details      TEXT,
    ip_address   VARCHAR(45),
    timestamp    TIMESTAMP DEFAULT NOW()
);
```

---

## 6. REST API Design

### Auth
| Method | Endpoint | Role | Description |
|---|---|---|---|
| POST | `/api/auth/login` | Public | Returns JWT access + refresh tokens |
| POST | `/api/auth/refresh` | Public | Refresh access token |
| POST | `/api/auth/logout` | Any | Invalidate tokens |

### Users
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/users` | DC_ADMIN | List all users |
| POST | `/api/users` | DC_ADMIN | Create user (via UserFactory) |
| GET | `/api/users/{id}` | DC_ADMIN | Get user detail |
| PUT | `/api/users/{id}` | DC_ADMIN | Update user |
| DELETE | `/api/users/{id}` | DC_ADMIN | Deactivate user |
| GET | `/api/users/me` | Any | Own profile |

### Zones
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/zones` | DC_ADMIN, MANAGER | List zones |
| POST | `/api/zones` | DC_ADMIN | Create zone |
| GET | `/api/zones/{id}` | DC_ADMIN, MANAGER | Zone detail + power summary |
| PUT | `/api/zones/{id}` | DC_ADMIN | Update zone |

### Racks
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/racks` | DC_ADMIN, TECHNICIAN, MANAGER | List all racks |
| POST | `/api/racks` | DC_ADMIN | Create rack |
| GET | `/api/racks/{id}` | DC_ADMIN, TECHNICIAN | Rack detail + utilization |
| PUT | `/api/racks/{id}` | DC_ADMIN | Update rack |
| DELETE | `/api/racks/{id}` | DC_ADMIN | Decommission rack |
| GET | `/api/racks/{id}/servers` | DC_ADMIN, TECHNICIAN | Servers in rack (visual layout) |
| GET | `/api/racks/{id}/utilization` | DC_ADMIN, MANAGER | U-space + power breakdown |

### Servers
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/servers` | DC_ADMIN, TECHNICIAN | List all servers |
| POST | `/api/servers` | DC_ADMIN | Register new server |
| GET | `/api/servers/{id}` | DC_ADMIN, TECHNICIAN, CUSTOMER | Server detail |
| PUT | `/api/servers/{id}` | DC_ADMIN | Update server |
| POST | `/api/servers/{id}/allocate` | DC_ADMIN | Allocate to rack (pass strategy in body) |
| POST | `/api/servers/{id}/provision` | DC_ADMIN | Assign to customer |
| PUT | `/api/servers/{id}/status` | TECHNICIAN | Update operational status |
| GET | `/api/servers/{id}/metrics` | DC_ADMIN, CUSTOMER | Latest resource metrics |
| GET | `/api/servers/{id}/metrics/history` | DC_ADMIN, MANAGER | Historical metrics (paginated) |
| GET | `/api/customers/me/servers` | CUSTOMER | Own allocated servers |

### Alerts
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/alerts` | DC_ADMIN, TECHNICIAN, MANAGER | Active alerts (filterable) |
| GET | `/api/alerts/{id}` | DC_ADMIN, TECHNICIAN | Alert detail |
| PUT | `/api/alerts/{id}/acknowledge` | DC_ADMIN, TECHNICIAN | Acknowledge alert |
| GET | `/api/alerts/stats` | MANAGER | Alert counts by severity/type |

### Maintenance
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/maintenance` | All | List schedules (role-filtered) |
| POST | `/api/maintenance` | DC_ADMIN | Schedule (executes ScheduleMaintenanceCommand) |
| GET | `/api/maintenance/{id}` | All | Schedule detail |
| PUT | `/api/maintenance/{id}/assign` | DC_ADMIN | Assign technician (Command) |
| PUT | `/api/maintenance/{id}/start` | TECHNICIAN | Start maintenance (Command) |
| PUT | `/api/maintenance/{id}/complete` | TECHNICIAN | Complete with report (Command) |
| PUT | `/api/maintenance/{id}/approve` | MANAGER | Approve schedule |
| DELETE | `/api/maintenance/{id}` | DC_ADMIN | Cancel (Command + undo-able) |
| PUT | `/api/maintenance/{id}/undo` | DC_ADMIN | Undo last command on schedule |
| PUT | `/api/maintenance/{id}/checklist/{itemId}` | TECHNICIAN | Complete checklist item |

### Reports & Dashboard
| Method | Endpoint | Role | Description |
|---|---|---|---|
| GET | `/api/dashboard` | Any | Role-specific KPI summary |
| GET | `/api/reports/capacity` | MANAGER, DC_ADMIN | Zone/rack capacity report |
| GET | `/api/reports/utilization` | MANAGER, DC_ADMIN | Server utilization summary |
| GET | `/api/reports/maintenance` | MANAGER | Maintenance history report |
| GET | `/api/audit-log` | DC_ADMIN | Full audit trail (paginated) |

### WebSocket (Real-time)
| Topic | Description |
|---|---|
| `/topic/alerts` | New alerts pushed to all connected admins |
| `/topic/rack/{rackId}` | Real-time U-space + power updates for a specific rack |
| `/topic/server/{serverId}/metrics` | Live metric stream for monitored servers |

---

## 7. SOLID Principles Checklist

| Principle | How It's Applied |
|---|---|
| **S** Single Responsibility | Each service class owns exactly one domain concern. `AlertService` only handles alerts; `AllocationContext` only handles rack slot selection. |
| **O** Open/Closed | `AllocationStrategy` and `RackObserver` are open to extension (new implementations) without modifying existing code. |
| **L** Liskov Substitution | Any `ServerComponent` subclass can substitute for the interface. Any `AbstractUser` subclass works wherever a user is expected. |
| **I** Interface Segregation | `RackObserver`, `MaintenanceCommand`, `AllocationStrategy` are narrow, focused interfaces. No class is forced to implement methods it doesn't use. |
| **D** Dependency Inversion | Services depend on interfaces (`AllocationStrategy`, `MaintenanceCommand`) not concrete implementations. Spring DI injects the right concrete class. |

---

## 8. Project Structure

```
colo.io/
├── backend/
│   └── src/main/java/com/coloio/srms/
│       ├── config/
│       │   ├── SecurityConfig.java         # Spring Security + JWT filter chain
│       │   ├── WebSocketConfig.java        # STOMP WebSocket config
│       │   └── JwtConfig.java
│       │
│       ├── domain/                         # Pure OOP model — no Spring
│       │   ├── user/
│       │   │   ├── AbstractUser.java
│       │   │   ├── DCAdmin.java
│       │   │   ├── Technician.java
│       │   │   ├── Customer.java
│       │   │   └── Manager.java
│       │   ├── server/
│       │   │   ├── ServerComponent.java    # interface
│       │   │   ├── BaseServer.java
│       │   │   ├── ServerDecorator.java    # abstract
│       │   │   ├── MonitoredServer.java
│       │   │   ├── AlertableServer.java
│       │   │   └── ManagedServer.java
│       │   ├── rack/
│       │   │   ├── RackSubject.java        # interface
│       │   │   ├── RackObserver.java       # interface
│       │   │   ├── RackEvent.java
│       │   │   └── Rack.java
│       │   ├── alert/
│       │   │   ├── Alert.java
│       │   │   ├── AlertManager.java       # Singleton
│       │   │   └── AlertObserver.java
│       │   └── enums/
│       │       ├── UserRole.java
│       │       ├── ServerStatus.java
│       │       ├── RackStatus.java
│       │       ├── AlertType.java
│       │       ├── AlertSeverity.java
│       │       └── MaintenanceType.java
│       │
│       ├── pattern/
│       │   ├── factory/
│       │   │   ├── UserFactory.java
│       │   │   └── ServerDecoratorFactory.java
│       │   ├── strategy/
│       │   │   ├── AllocationStrategy.java         # interface
│       │   │   ├── AllocationContext.java
│       │   │   ├── FirstFitAllocationStrategy.java
│       │   │   ├── BestFitAllocationStrategy.java
│       │   │   ├── PowerOptimizedAllocationStrategy.java
│       │   │   └── ZoneAwareAllocationStrategy.java
│       │   └── command/
│       │       ├── MaintenanceCommand.java          # interface
│       │       ├── MaintenanceCommandInvoker.java
│       │       ├── ScheduleMaintenanceCommand.java
│       │       ├── AssignTechnicianCommand.java
│       │       ├── StartMaintenanceCommand.java
│       │       ├── CompleteMaintenanceCommand.java
│       │       └── CancelMaintenanceCommand.java
│       │
│       ├── entity/                         # JPA entities (thin wrappers)
│       │   ├── UserEntity.java
│       │   ├── RackEntity.java
│       │   ├── ServerEntity.java
│       │   ├── ZoneEntity.java
│       │   ├── AlertEntity.java
│       │   ├── MaintenanceScheduleEntity.java
│       │   ├── ServerMetricEntity.java
│       │   └── AuditLogEntity.java
│       │
│       ├── repository/
│       │   ├── UserRepository.java
│       │   ├── RackRepository.java
│       │   ├── ServerRepository.java
│       │   ├── ZoneRepository.java
│       │   ├── AlertRepository.java
│       │   ├── MaintenanceRepository.java
│       │   ├── ServerMetricRepository.java
│       │   └── AuditLogRepository.java
│       │
│       ├── service/
│       │   ├── UserService.java
│       │   ├── RackService.java
│       │   ├── ServerService.java
│       │   ├── ZoneService.java
│       │   ├── AlertService.java
│       │   ├── MaintenanceService.java
│       │   ├── MonitoringService.java      # Singleton
│       │   ├── AuditService.java
│       │   ├── ReportService.java
│       │   └── NotificationService.java
│       │
│       ├── controller/
│       │   ├── AuthController.java
│       │   ├── UserController.java
│       │   ├── RackController.java
│       │   ├── ServerController.java
│       │   ├── ZoneController.java
│       │   ├── AlertController.java
│       │   ├── MaintenanceController.java
│       │   ├── DashboardController.java
│       │   └── ReportController.java
│       │
│       └── dto/
│           ├── request/
│           └── response/
│
└── frontend/
    └── src/
        ├── pages/
        │   ├── Login.jsx
        │   ├── Dashboard.jsx              # role-specific KPIs
        │   ├── racks/
        │   │   ├── RackList.jsx
        │   │   ├── RackDetail.jsx         # visual U-space layout
        │   │   └── RackForm.jsx
        │   ├── servers/
        │   │   ├── ServerList.jsx
        │   │   ├── ServerDetail.jsx       # metrics charts
        │   │   └── ServerAllocate.jsx     # strategy picker
        │   ├── maintenance/
        │   │   ├── MaintenanceCalendar.jsx
        │   │   ├── MaintenanceDetail.jsx
        │   │   └── ChecklistView.jsx
        │   ├── alerts/
        │   │   └── AlertsPanel.jsx        # live WebSocket feed
        │   └── reports/
        │       └── CapacityReport.jsx
        ├── components/
        │   ├── RackVisualizer.jsx         # 42U rack grid
        │   ├── MetricsChart.jsx           # Chart.js wrapper
        │   ├── AlertBadge.jsx
        │   └── RoleGuard.jsx             # route protection
        └── hooks/
            ├── useWebSocket.js
            └── useAuth.js
```

---

## 9. Sprint Plan

### Sprint 1 — Foundation (Week 1-2)
**Goal:** Working auth + user management

- [ ] Spring Boot project setup (PostgreSQL, JPA, Security, WebSocket starters)
- [ ] Implement `AbstractUser` hierarchy + `UserFactory`
- [ ] JWT filter + Spring Security config (role-based route protection)
- [ ] `UserEntity`, `UserRepository`, `UserService`, `UserController`
- [ ] `AuthController` — login, refresh, logout
- [ ] `AuditService` — intercepts service calls via AOP `@AfterReturning`
- [ ] React: Login page, protected routes, role-based layout shell
- **Deliverable:** Login → role-specific dashboard skeleton

### Sprint 2 — Inventory Core (Week 3-4)
**Goal:** Full rack and server inventory management

- [ ] `Zone`, `Rack` domain classes + JPA entities
- [ ] `RackSubject` / `RackObserver` interfaces + `Rack` as observable
- [ ] `BaseServer`, `ServerDecorator`, `ServerDecoratorFactory`
- [ ] `AllocationStrategy` implementations (FirstFit, BestFit, PowerOptimized)
- [ ] `AllocationContext` + `RackService.allocateServer()`
- [ ] Full CRUD: Zone, Rack, Server controllers + services
- [ ] React: Rack list, `RackVisualizer` (42U grid), server list, allocation form with strategy picker
- **Deliverable:** Admin can create zones, racks, servers, and allocate servers using selectable strategies

### Sprint 3 — Monitoring & Alerts (Week 5-6)
**Goal:** Real-time observability

- [ ] `AlertManager` Singleton + `AlertEntity` + `AlertRepository`
- [ ] `AlertObserver` and `DashboardObserver` implementations
- [ ] Register observers on `Rack` at startup
- [ ] `MonitoringService` Singleton — `ScheduledExecutorService` polls `MonitoredServer` wrappers every 30s
- [ ] `ServerMetricEntity` + time-series storage
- [ ] WebSocket config + `DashboardObserver` pushes to `/topic/alerts` and `/topic/rack/{id}`
- [ ] `AlertController`, `AlertService`
- [ ] React: `AlertsPanel` (WebSocket live feed), `MetricsChart` (Chart.js), server detail with live metrics
- **Deliverable:** Real-time alert feed and live server metric graphs

### Sprint 4 — Maintenance & Reports (Week 7-8)
**Goal:** Full maintenance workflow + Manager reports

- [ ] `MaintenanceCommand` interface + all 5 concrete commands
- [ ] `MaintenanceCommandInvoker` with undo/redo stack
- [ ] `MaintenanceService` orchestrates invoker
- [ ] `ManagedServer` decorator + `MaintenanceScheduleEntity` + checklist items
- [ ] `NotificationObserver` — email/in-app on schedule create/cancel
- [ ] Manager approval workflow endpoint
- [ ] `ReportService` — capacity, utilization, maintenance history
- [ ] React: Maintenance calendar, checklist view, reports page with charts
- **Deliverable:** End-to-end maintenance lifecycle with undo, notifications, and analytics

---

## 10. Feature Summary Table

| Feature | Pattern Used | Sprint |
|---|---|---|
| Role-based user creation | Factory | 1 |
| JWT auth + RBAC | — | 1 |
| Audit trail (AOP) | — | 1 |
| Rack + server inventory | — | 2 |
| Intelligent server allocation | Strategy | 2 |
| Server capability decoration | Decorator | 2 |
| Zone + power budget management | — | 2 |
| Real-time alert propagation | Observer | 3 |
| Live metric monitoring | Singleton (MonitoringService) | 3 |
| Centralized alert management | Singleton (AlertManager) | 3 |
| WebSocket dashboard updates | Observer → WebSocket | 3 |
| Reversible maintenance commands | Command | 4 |
| Maintenance approval workflow | — | 4 |
| Checklist management | — | 4 |
| Capacity + utilization reports | — | 4 |

---

## 11. Resume Talking Points

- **"Designed and implemented a layered Spring Boot system with six GoF design patterns applied to real domain problems"** — not toy examples, each pattern solves a concrete problem in the data center domain.
- **"Applied the Observer pattern to decouple rack state changes from alert generation, notification, and dashboard updates — adding new reactions requires zero changes to existing code."**
- **"Used the Strategy pattern to make server allocation algorithms pluggable at runtime — the admin selects First-Fit, Best-Fit, or Power-Optimized allocation from the UI with no backend code changes."**
- **"Implemented the Command pattern with a full undo/redo stack for maintenance operations, enabling reversible administrative actions with a built-in audit trail."**
- **"Used the Decorator pattern to progressively layer monitoring, alerting, and lifecycle tracking onto server instances at runtime, replacing a combinatorial subclass hierarchy."**
- **"Integrated WebSocket (STOMP) for real-time alert streaming and live metric updates — no polling, event-driven push from the backend Observer directly to the React frontend."**
- **"PostgreSQL schema with proper normalization, foreign key constraints, and a time-series metrics table for historical server utilization trends."**

---

*Last updated: April 2026*
