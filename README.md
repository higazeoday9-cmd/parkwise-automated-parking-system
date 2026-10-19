# 🅿️ ParkWise — Automated Parking Lot & Conveyor Control System

An enterprise-grade, multi-facility automated parking management and conveyor logistics platform engineered in Java using the **Entity-Control-Boundary (ECB)** architectural pattern. The system orchestrates vehicle ingress, automated 3D spatial allocation, dynamic stepped billing, concurrent diagnostic state machines, and JasperReports analytical summaries.

---

## 🏗️ Architectural Overview (ECB Pattern & RBAC)

The application enforces a strict separation of concerns across three core architectural tiers, reinforced with **Role-Based Access Control (RBAC)**:

- **Boundary Layer (`boundary`):** Desktop presentation layer developed using Java Swing and asynchronous `SwingWorker` threads. To enforce security and business boundaries, the UI dynamically isolates operational tooling from public self-service:
  - **Administrator Portal:** System health monitoring, conveyor diagnostic consoles, IRS JSON tariff importing, facility capacity adjustments, and annual financial reporting.
  - **Subscriber Self-Service Portal:** Contactless customer terminal displaying live session tracking, assigned coordinate inspection, accumulated fee estimation, itemized receipt generation, and gate checkout.
- **Control Layer (`control`):** Pure business logic, decision algorithms, and state management. Manages the 3D nearest-neighbor spatial allocation engine, conveyor integrity check watchdog, dynamic tariff calculators, and external stub services (Payment Gateway, SMS notifications).
- **Entity Layer (`entity`):** Data access objects (DAOs) and domain models managing relational persistence in Microsoft Access via JDBC (`UCanAccess`), incorporating automated runtime schema migrations and transactional integrity.

---

## ⚡ Core Engineering Features

### 1. 3D Spatial Optimization Engine
- Implements a multi-constraint spatial heuristic algorithm to assign incoming vehicles to the optimal vacant spot.
- Validates candidate spaces by vehicle dimension classification (`small`, `medium`, `large`) and conveyor load thresholds ($\text{Vehicle Weight} \le \text{Conveyor Max Weight}$).
- Minimizes retrieval latency by heavily penalizing vertical floor traversal over horizontal Euclidean distance:
  $$\text{Score} = \vert{}\Delta\text{Floor}\vert{} \times 10^6 + \sqrt{\Delta x^2 + \Delta y^2}$$

### 2. Conveyor Finite State Machine & Fault Diagnostics
- Concurrent integrity diagnostics running parallel mechanical (belt tension, physical obstructions, roller resistance) and electronic (sensor data, controller link) integrity tests across an `ExecutorService` thread pool.
- Enforces a 10-minute timeout with up to 3 automated watchdog retries before safely transitioning the conveyor into `Paused` mode and dispatching administrative alerts.
- Guarantees non-preemptible routing executions (conveyors cannot be turned off mid-route).

### 3. Dynamic Tariff Ingestion & Billing Engine
- Ingests yearly tariff matrices published in JSON format by the Integrated Rate Service (IRS) and normalizes them into database rate tables.
- Computes stepped hourly and multi-day parking charges: $\lceil\text{minutes} / 60\rceil$ capped against full-day rates.
- Enforces Customer Club loyalty rules, preferred facility audit tracking, and automated promotional waivers (first session in registration month is free in preferred facilities).

### 4. Analytical BI & JasperReports Integration
- Compiles dynamic `.jrxml` templates to query and render production-ready PDF Annual Summary reports sorted by city descending and revenue ascending.
- Computes month-over-month Customer Club membership acquisition metrics and cumulative active rosters.

---

## 📁 Repository Structure

```text
├── src/
│   ├── Main.java                 # Application entry point
│   ├── boundary/                 # Swing presentation layer & views
│   ├── control/                  # Business logic, FSM, and algorithms
│   └── entity/                   # Relational domain models & DAOs
├── db/
│   └── ParkWise_Ex2.accdb        # Normalized Microsoft Access database
├── reports/
│   └── AnnualSummary.jrxml       # JasperReports template definition
├── lib/                          # UCanAccess & JasperReports JAR dependencies
├── output/                       # Pre-compiled sample PDF financial reports
├── json/                         # Sample IRS tariff JSON matrices
└── README.md                     # System documentation
