# PharmaMaster: Enterprise Pharmacy Management & ERP System

![Java](https://img.shields.io/badge/Java-17%2B-ed8b00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6db33f?style=for-the-badge&logo=spring&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ed?style=for-the-badge&logo=docker&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-Queue_&_Outbox-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![DigitalOcean](https://img.shields.io/badge/DigitalOcean-CI%2FCD-0080FF?style=for-the-badge&logo=digitalocean&logoColor=white)
![ELK Stack](https://img.shields.io/badge/ELK_Stack-Planned-005571?style=for-the-badge&logo=elasticsearch&logoColor=white)

## 📖 Executive Summary
**PharmaMaster** is a comprehensive Enterprise Resource Planning (ERP) system designed specifically for the pharmaceutical retail and wholesale operations. 

Built on a **Modular Monolith Architecture** with an **Event-Driven Architecture** integration, this project resolves the strict core challenges of pharmaceutical businesses: complex multiphase inventory operations, dynamic multi-unit conversions, strict expiry date (**FEFO**) tracking, and human resources flow. It embraces the same architectural principles (Redis Queue asynchronous events, CD automation, RBAC) utilized in scalable and fault-tolerant enterprise ecosystems.

## 🏗️ System Architecture
The system adopts a **Modular Monolith** structure to ensure high maintainability, tight transactional integrity, and a clear path toward microservices. Furthermore, it incorporates an **Event-Driven Architecture** via Redis Queue and the Outbox Pattern for high-throughput background processing, fully orchestrated via Docker containers.

### High-Level Architecture Diagram
![System Architecture Diagram](https://res.cloudinary.com/dfcb3zzw9/image/upload/v1772693463/cleanarchitecture.drawio_pa9fnk.png)

### Event-Driven Data Flow
To ensure strict system performance and decouple critical business logic, PharmaMaster implements **Redis Queue** coupled with the **Outbox Pattern** as its central message broker. 
For example, the Identity and HR domains communicate via Redis events (`user-password-email` queue) for asynchronous email notifications via SendGrid. This fully isolates the mail dispatcher worker (`MailWorker`), prevents blocking core API calls, and ensures reliable message delivery regardless of external third-party service latency.

### Observability & Centralized Logging (Upcoming Integration)
To maintain strict auditability and rapid troubleshooting across all modules, the system architecture plans to integrate the **ELK Stack (Elasticsearch, Logstash, Kibana)** coupled with **Filebeat**. Filebeat will tail distributed container logs and forward them to Elasticsearch (via Logstash). This will provide a centralized Kibana dashboard to securely monitor API latencies, track error rates, and trace asynchronous Redis events in real-time.

---

## 🧩 Modules Breakdown

| Module | Key Responsibilities |
| :--- | :--- |
| **`identity`** | Identity and Access Management (IAM), Security, Stateless JWT Authentication. |
| **`core`** | Global System properties and Branch configurations. |
| **`hr`** | Employee profiles, Payroll, Career Changes, and Leave requests approval flows. |
| **`catalog`** | Master data for Products, Categories, and complex Multi-unit conversion rates. |
| **`inventory`** | Procurement (Purchase Orders), warehouse management, Batch ID management, and Expiry tracking. |
| **`sales`** | Point of Sale (POS) logic, Invoicing, and strictly enforced FEFO inventory deduction. |
| **Workers** | Redis Queue Consumers for async processing (e.g., `modules/mail` for SendGrid Dispatcher). |

## 🛠️ Technology Stack & Engineering Decisions

* **Core Backend:** Java 17+, Spring Boot 3.x
* **Data Storage:** PostgreSQL 15, managed via Flyway for seamless DB versioning & migrations.
* **Message Broker:** Redis Queue & PostgreSQL Outbox Pattern (Event-driven asynchronous processing).
* **Inter-module Communication:** Java Method Invocations (Synchronous) and Redis Queues (Asynchronous).
* **DevOps & Infrastructure:** Docker & Docker Compose for local development & production orchestration.
* **CI/CD Pipeline:** Fully automated Continuous Deployment to **DigitalOcean** utilizing **GitHub Actions**.
* **Security & APIs:** Spring Security, JWT logic, MapStruct for DTOs, SpringDoc OpenAPI (Swagger).
* **Observability (Upcoming):** ELK Stack (Elasticsearch, Logstash, Kibana) + Filebeat for centralized logging and monitoring.

## 🚀 Key Features & Best Practices

1. **FEFO Inventory Strategy:** Automatic and strict inventory deduction following **First Expired, First Out** to prioritize clearing goods nearing their expiration, minimizing pharmaceutical waste.
2. **Multi-Unit Conversion System:** Seamlessly scale operations by selling products across multiple dynamic units (e.g., Box -> Blister -> Pill) with synchronized dynamic pricing and global conversion fractions.
3. **Enterprise RBAC:** Granular, robust Role-Based Access Control logic separating permissions for System Admins, Branch Managers, Pharmacists, and Warehouse Staff.
4. **Secure Onboarding:** Every employee creation automatically triggers an opaque random password generation and async email dispatching.
5. **Data Integrity & Traceability:** Hard deletion is heavily restricted. The application primarily utilizes **Soft Deletes**, state machine statuses (Pending, Approved, Rejected), and an Action Log to ensure thorough accounting audits.

## � CI/CD Pipeline Flow (GitHub Actions -> DigitalOcean)

The deployment and delivery process is fully automated, removing all manual provisioning overhead for the DigitalOcean VPS Droplet:

1. **Code Push:** The developer merges verified code into the `main` branch.
2. **Build & Push:** GitHub Actions steps in, packaging a Docker image and pushing it directly to Docker Hub securely. The image is strictly tagged with the specific Git Commit SHA (`github.sha`) for enterprise version tracking.
3. **SSH Execution:** The pipeline securely authenticates into the DigitalOcean VPS environment via SSH keys.
4. **Zero-Downtime Update:** Re-evaluates `.env` configurations, explicitly pulls the latest committed Docker image, and executes `docker compose up -d --remove-orphans` silently.
5. **Optimize Storage:** A post-deployment hook executes `docker image prune -f` to aggressively clean up dangling images, optimizing disk usage on the SSD server.
### CI/CD Flow Diagram
![CI/CD Flow Diagram](https://res.cloudinary.com/dfcb3zzw9/image/upload/v1772693327/DO-1_vpeonr.jpg)

## ⚙️ How to Run Locally

### Prerequisites
* Docker & Docker Compose (v2.0+)
* Java JDK 17+
* Maven 3.6+

### Step-by-Step
1. **Clone the repository:**
   ```bash
   git clone [YOUR_REPO_URL]
   cd pharmacy-erp-system/PharmacyManagement
   ```

2. **Start the Infrastructure (PostgreSQL, Redis, and App):**
   ```bash
   docker-compose up -d
   ```

3. **Verify Deployment:**
   * **Backend APIs (Swagger UI):** `http://localhost:8080/swagger-ui.html`
