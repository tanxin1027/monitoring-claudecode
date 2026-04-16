# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

### Backend (Java 11, Maven)

```bash
# Build all modules
mvn clean package -DskipTests

# Build specific module
cd cloud-server && mvn clean package -DskipTests
cd agent && mvn clean package -DskipTests

# Run cloud-server locally
cd cloud-server && mvn spring-boot:run

# Run agent locally
cd agent && mvn spring-boot:run
```

### Frontend (Node.js, Vite + Vue 3)

```bash
cd frontend

# Install dependencies
npm install

# Dev server (proxies to localhost:8080)
npm run dev

# Production build
npm run build
```

## Architecture Overview

This is a hospital server monitoring platform with a dual-component architecture:

```
┌──────────────────────┐         HTTPS         ┌──────────────────────┐
│   Cloud Server       │ ◄───────────────────► │      Agent           │
│   (port 8080)        │   Agent pushes data   │   (inner network)    │
│   - Admin API        │                       │   - System collector │
│   - Dashboard API    │                       │   - MySQL collector  │
│   - Data collector   │                       │   - Tomcat collector │
│   - MySQL/Redis      │                       │                      │
└──────────────────────┘                       └──────────────────────┘
```

### Module Structure

- **cloud-server/**: Spring Boot backend (port 8080)
  - `controller/`: REST API endpoints (`/api/auth`, `/api/monitor`, `/api/collector`, etc.)
  - `service/`: Business logic with MyBatis Plus
  - `mapper/`: Data access layer + XML mappers
  - `entity/`: JPA entities (ServerInfo, HospitalInfo, etc.)
  - `dto/`: Request/response objects
  - `config/`: Security, JWT, MyBatis configuration

- **agent/**: Inner-network data collector
  - `collector/`: System (OSHI), MySQL, Tomcat metrics
  - `reporter/`: HTTP data pusher to cloud-server
  - `config/`: Cloud server connection config

- **frontend/**: Vue 3 + Element Plus admin UI
  - `views/`: Dashboard, server/MySQL/Tomcat monitoring, alarm management
  - `store/`: Pinia state management
  - Proxies `/api` to `localhost:8080`

- **database/**: MySQL schema with monthly partitioned tables

### Key Design Patterns

1. **Token-based Agent Authentication**: Agents authenticate via `X-Agent-Token` header, validated against `agent_token` table

2. **Monthly Data Partitioning**: Metric tables are named `server_metric_YYYYMM`, `mysql_metric_YYYYMM`, `tomcat_metric_YYYYMM` - create new tables monthly

3. **Hospital Data Isolation**: All data queries are scoped by `hospital_id` for multi-tenant access control

4. **Scheduled Tasks**: Both cloud-server and agent use `@EnableScheduling` for periodic jobs (data collection, cleanup)

### API Endpoints

| Module | Path | Auth |
|--------|------|------|
| Auth | `POST /api/auth/login` | None |
| Hospital | `GET/POST/PUT/DELETE /api/hospital/*` | JWT |
| Server Monitor | `GET /api/monitor/server/*` | JWT |
| Collector | `POST /api/collector/server/metric` | Agent Token |
| Dashboard | `GET /api/dashboard/*` | JWT |
| Agent Token | `GET/POST /api/agent/token/*` | JWT |

## Database

Requires MySQL 8.0+ and Redis. Initialize with:
```bash
mysql -u root -p < database/schema.sql
```

The schema creates tables for users, roles, hospitals, servers, MySQL/Tomcat instances, metrics, alarms, and agent tokens.
