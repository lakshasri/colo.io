# colo.io — Server & Rack Management System

A full-stack data center infrastructure management platform built for colocation facilities. Manages server inventory, rack allocation, real-time environmental monitoring, and maintenance scheduling across multi-zone data centers.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.x, Spring Security |
| ORM | JPA / Hibernate |
| Database | PostgreSQL 15 |
| Auth | JWT (Access + Refresh tokens) |
| Real-time | WebSocket (STOMP over SockJS) |
| Frontend | React 18, Chart.js, Ant Design |

---

## Core Features

- **Rack & Server Inventory** — Track servers across zones and racks with U-space and power utilization
- **Intelligent Allocation** — Pluggable allocation strategies (First-Fit, Best-Fit, Power-Optimized, Zone-Aware)
- **Real-time Monitoring** — Live CPU, RAM, and disk metrics with WebSocket-pushed dashboard updates
- **Alert System** — Threshold-based alerts for power overload and environmental anomalies
- **Maintenance Workflows** — Scheduled maintenance with technician assignment, checklists, and manager approval
- **Role-based Access** — DC Admin, Technician, Customer, and Manager roles with scoped permissions
- **Audit Trail** — Full action log across all entities

---

## OOP Design Patterns

| Pattern | Applied To |
|---|---|
| Factory | `UserFactory` — creates role-correct user instances; `ServerDecoratorFactory` — wraps servers with capabilities |
| Strategy | `AllocationStrategy` — swappable rack slot selection algorithms |
| Observer | `Rack` as subject — propagates state changes to alert, notification, and dashboard observers |
| Command | `MaintenanceCommand` — reversible maintenance operations with undo/redo stack |
| Decorator | `ServerComponent` — layers monitoring, alerting, and lifecycle tracking onto servers at runtime |
| Singleton | `AlertManager`, `MonitoringService` — system-wide, thread-safe service instances |

---

## User Roles

| Role | Responsibilities |
|---|---|
| DC Admin | Manage racks, servers, zones, users; configure alerts |
| Technician | Perform maintenance, update server status, complete checklists |
| Customer | View allocated servers, monitor resource usage, request support |
| Manager | View analytics, approve maintenance, review compliance reports |

---

## Project Structure

```
colo.io/
├── backend/
│   └── src/main/java/com/coloio/srms/
│       ├── domain/        # Core domain classes (User, Rack, Server, Alert)
│       ├── pattern/       # GoF patterns (factory, strategy, command, decorator)
│       ├── entity/        # JPA entities
│       ├── repository/    # Spring Data repositories
│       ├── service/       # Business logic
│       ├── controller/    # REST API
│       ├── config/        # Security, WebSocket, JWT, AOP
│       └── audit/         # AOP audit aspect
└── frontend/
    └── src/
        ├── components/    # AppLayout, RackVisualizer, AlertsPanel, MetricsChart
        ├── context/       # AuthContext
        ├── hooks/         # useWebSocket
        ├── pages/         # Dashboard, Racks, Servers, Maintenance, Reports
        └── services/      # Axios API client
```

---

## Setup

### Prerequisites
- Java 21, Maven 3.9+
- PostgreSQL 15 (create a `srms` database)
- Node.js 20+

### Backend
```bash
cd backend
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Edit DB credentials
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

API docs available at `http://localhost:8080/swagger-ui.html` after starting the backend.
