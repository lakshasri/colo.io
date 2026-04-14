# colo.io — Quickstart

How to start the project from a cold machine in under 5 minutes.

---

## 0. Prerequisites (one-time)

Install these once. Versions matter — older versions will not compile.

| Tool | Version | Install (Ubuntu/Debian) |
|---|---|---|
| Java JDK | 21+ | `sudo apt install openjdk-21-jdk` |
| Node.js | 18+ | `sudo apt install nodejs npm` |
| PostgreSQL | 15+ | `sudo apt install postgresql-15` |

Verify:
```bash
java -version     # openjdk 21.x
node -v           # v18.x or v20.x
psql --version    # psql 15.x
```

---

## 1. Database (one-time)

Start Postgres and create the database + user the app expects:

```bash
sudo systemctl start postgresql
sudo -u postgres createdb srms_db
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'postgres';"
```

That's it. The app auto-creates all tables and seeds demo users on first boot.

If you ever want to reset everything:
```bash
sudo -u postgres dropdb srms_db && sudo -u postgres createdb srms_db
```

---

## 2. Start the backend

Open **Terminal 1**:

```bash
cd backend
./mvnw spring-boot:run
```

First run downloads Maven + dependencies (~2 min). Subsequent starts take ~10 sec.

**Ready when you see:** `Started SrmsApplication in X seconds`

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API docs (JSON): http://localhost:8080/api-docs

Leave this terminal running.

---

## 3. Start the frontend

Open **Terminal 2**:

```bash
cd frontend
npm install       # first time only, ~1 min
npm run dev
```

**Ready when you see:** `VITE ready in Xms` and `Local: http://localhost:3000/`

Open http://localhost:3000 in your browser.

The dev server auto-proxies `/api` and `/ws` requests to the backend on port 8080, so you don't need to configure anything.

---

## 4. Log in

Four demo accounts are auto-seeded on first backend boot. All use password `admin123`:

| Username | Password | Role | Access |
|---|---|---|---|
| `admin` | `admin123` | DC_ADMIN | Full system — users, racks, servers, zones, SLA, bulk ops |
| `manager` | `admin123` | MANAGER | Reports, SLA, maintenance approvals, capacity |
| `tech` | `admin123` | TECHNICIAN | Maintenance tasks, server status, alerts |
| `customer` | `admin123` | CUSTOMER | Own servers, alerts, SLA view |

Each role redirects to its own dashboard after login.

---

## 5. Run the tests (optional)

```bash
cd backend
./mvnw test
```

8 test suites, ~40 test methods — Factory, Strategy, Command, Service layers.

---

## Common issues

**Port 3000 already in use**
→ Vite will auto-pick 3001. Or kill the existing process: `kill $(lsof -i :3000 -t)`

**Port 8080 already in use**
→ Kill it: `kill $(lsof -i :8080 -t)` — or change `server.port` in `backend/src/main/resources/application.properties`

**Blank white page on frontend**
→ Hard-refresh the browser (Ctrl+Shift+R). Usually a cached stale bundle.

**"Invalid credentials" on login**
→ Backend is probably not running or still booting. Check Terminal 1. Verify with:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

**Postgres connection refused**
→ `sudo systemctl start postgresql`

**Frontend can't reach backend**
→ Confirm backend is up (`curl http://localhost:8080/api-docs`). The frontend proxies through Vite, so both servers must run.

---

## Project layout

```
colo.io/
├── backend/              Spring Boot 3 + Java 21
│   ├── src/main/java     Source
│   ├── src/test/java     JUnit tests
│   └── pom.xml
├── frontend/             React 18 + Vite + Ant Design
│   ├── src/              Source
│   ├── package.json
│   └── vite.config.js
└── Plan/                 Architecture + sprint docs
    ├── plan.md
    ├── sprints.md
    ├── REQUIREMENTS.md
    └── USAGE.md          API reference
```

---

## Stopping everything

In each terminal, press `Ctrl+C`. Or kill by port:

```bash
kill $(lsof -i :8080 -t) $(lsof -i :3000 -t) 2>/dev/null
```

Postgres can stay running — the app doesn't hold connections when stopped.
