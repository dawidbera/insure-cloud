# InsureCloud

Modern, cloud-native Insurance Policy Management Platform built with **Java 21**, **Spring Boot 3.4**, and **AWS** services (emulated via LocalStack).

## 🚀 Overview

InsureCloud is a microservices-based system designed to handle the full lifecycle of insurance policies. It demonstrates professional software engineering practices, including Event-Driven Architecture, Domain-Driven Design (DDD), and Infrastructure as Code (IaC).

## 🛠 Tech Stack

- **Languages:** Java 21 (with **Virtual Threads** enabled)
- **Framework:** Spring Boot 3.4
- **API Gateway:** Spring Cloud Gateway
- **Discovery:** Netflix Eureka (Secured with Basic Auth)
- **Security:** Keycloak (OAuth2 / OpenID Connect)
- **Resilience:** Resilience4j (Circuit Breaker, Fallback)
- **Validation:** Jakarta Validation
- **Persistence:** PostgreSQL, DynamoDB (Audit Log), Redis
- **Infrastructure:** Docker Compose, LocalStack (S3, SQS, SNS, DynamoDB)
- **Search:** Elasticsearch
- **Observability:** Prometheus, Grafana, Micrometer Tracing (Zipkin)
- **Testing:** JUnit 5, Mockito, Testcontainers, WireMock
- **API:** OpenAPI (Swagger)

## 🏗 Architecture & Request Flow

The system utilizes a centralized **API Gateway**, **Service Discovery**, and an **Identity Provider (Keycloak)** to ensure secure, resilient, and manageable communication. A **Zero-Trust** security model is enforced, where every service validates JWT tokens and checks RBAC roles using the shared `common-security` library.

### Microservices Architecture & Request Flow Diagram
```mermaid
graph TB
    Client[Client / Frontend]
    
    subgraph "Identity & Access"
        Keycloak[Keycloak: 8088]
        CS[common-security: lib]
    end

    subgraph "Control Plane"
        Eureka[Discovery Service: Eureka]
    end

    subgraph "Observability"
        Prometheus[Prometheus: 9090]
        Grafana[Grafana: 3000]
        Zipkin[Zipkin: 9411]
    end

    subgraph "Service Layer (Synchronous & Resilient)"
        direction TB
        PS[Policy Service: 8081]
        QS[Quote Service: 8082]
        SS[Search Service: 8085]
        DS[Document Service: 8084]
        NS[Notification Service: 8083]
        
        subgraph "Internal Logic Patterns"
            CB{Circuit Breaker}
            Fallback[Fallback Handler]
            QS_STRAT[Strategy Pattern: Car/Home/Life]
        end
    end

    %% Auth & Routing
    Client -->|1. Authenticate| Keycloak
    Client -->|2. Request + JWT| GW[API Gateway: 8080]
    
    GW <-->|Fetch Routes| Eureka
    GW -.->|Validate JWT| Keycloak
    GW -->|Route| PS & QS & SS & DS & NS

    %% Security & Validation
    PS & QS & SS & DS & NS -.->|Validate JWT via| CS
    CS -.->|Map Roles| Keycloak

    %% Inter-service Flow
    PS -- "QuoteClient" --> CB
    CB -->|Allowed| QS
    CB -.->|Open/Fallback| Fallback
    Fallback -.-> PS
    
    QS -.->|Premium Strategy| QS_STRAT

    %% Persistence
    QS -->|Cache| Redis[(Redis)]
    PS -->|PostgreSQL| DB_PG[(Transactional Outbox)]
    PS -->|Audit Log| DB_DYNAMO[(DynamoDB)]
    
    subgraph "Event-Driven Layer (Asynchronous)"
        direction LR
        Outbox[Outbox Processor]
        SNS[AWS SNS: Topic]
        SQS[SQS Queues]
    end

    DB_PG -.->|Polling| Outbox
    Outbox -->|Publish| SNS
    SNS -->|Fan-out| SQS
    
    SQS -.->|Consume| NS & DS & SS

    subgraph "LocalStack (AWS Emulation)"
        S3[S3: policy-documents]
        SES[SES: Notifications]
    end

    NS -->|Send| SES
    DS -->|Upload PDF| S3
    SS -->|Index| ES[(Elasticsearch)]

    subgraph "API Documentation"
        Swagger[Swagger UI Aggregator]
        GW -->|Expose: 8080/swagger-ui.html| Swagger
        Swagger -.->|Aggregate OpenAPI| PS & QS & SS & DS & NS
    end

    %% Observability
    PS & QS & SS & DS & NS & GW -.->|Metrics| Prometheus
    PS & QS & SS & DS & NS & GW -.->|Traces| Zipkin
    Prometheus -.-> Grafana
```

## 🚦 Getting Started

### Prerequisites
- Docker & Docker Compose
- JDK 21
- Maven 3.9+

### Building the Project
Build all microservices at once from the root directory:
```bash
mvn clean package -DskipTests
```

### Running the Entire System
You can launch the full environment (Infrastructure + Microservices) with a single command:
```bash
docker compose up -d --build
```

### Verification & Testing

#### 1. Obtain a Security Token
Since the API is secured with Keycloak, you need a JWT token to call most endpoints. Use the provided helper script:

- **For Insurance Agent:** `./get-token.sh` (or `./get-token.sh AGENT`)
- **For Admin:** `./get-token.sh ADMIN`
- **For Customer:** `./get-token.sh CUSTOMER`

The script will output a `Bearer <token>` string.

#### 2. Test the Flow via API Gateway (Port 8080)
You can use the **Swagger UI** (`http://localhost:8080/swagger-ui.html`) or **curl**.

1. **Calculate a Quote:**
```bash
curl -X POST http://localhost:8080/api/quotes \
  -H "Content-Type: application/json" \
  -d '{"productCode": "CAR_INSURANCE", "customerAge": 25, "assetValue": 50000}'
```

2. **Issue a Policy:**
```bash
curl -X POST http://localhost:8080/api/policies \
  -H "Content-Type: application/json" \
  -d '{"policyNumber": "POL-123", "customerId": "CUST-001", "premiumAmount": 500.00, "startDate": "2026-02-16", "endDate": "2027-02-16"}'
```

3. **Check Search Index:**
```bash
curl http://localhost:8080/api/search/by-number?policyNumber=POL-123
```

### 📈 API Documentation & Monitoring
Once the services are running, you can access the tools:

#### API Documentation
- **Centralized API Docs:** `http://localhost:8080/swagger-ui.html`

#### Monitoring & Tracing
- **Prometheus (Metrics):** `http://localhost:9090`
- **Grafana (Dashboards):** `http://localhost:3000` (admin/admin)
- **Zipkin (Distributed Tracing):** `http://localhost:9411`

### 🧪 Automated Testing (E2E & Integration)
The project emphasizes reliability through comprehensive automated testing using **Testcontainers** and **WireMock**.

- **Business Flow E2E Test:** `BusinessProcessFlowIntegrationTest.java` (in `policy-service`) covers the entire process:
    1. Mocking the Quote Engine response.
    2. Creating a policy via REST API.
    3. Verifying the **Transactional Outbox** pattern.
    4. Confirming asynchronous event processing and **Audit Log** creation in DynamoDB.
- **Infrastructure Verification:** `DocumentListenerIntegrationTest.java` (in `document-service`) verifies:
    1. Consumption of SQS events.
    2. PDF generation.
    3. Successful upload to **AWS S3**.

To run all integration tests:
```bash
mvn test -Pintegration-tests
```


Individual service documentation (if needed):
- Policy Service: `http://localhost:8081/swagger-ui.html`
- Quote Service: `http://localhost:8082/swagger-ui.html`
- Notification Service: `http://localhost:8083/swagger-ui.html`
- Document Service: `http://localhost:8084/swagger-ui.html`
- Search Service: `http://localhost:8085/swagger-ui.html`

## 📝 License
This project is licensed under the MIT License.
