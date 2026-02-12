# InsureCloud

Modern, cloud-native Insurance Policy Management Platform built with **Java 21**, **Spring Boot 3.4**, and **AWS** services (emulated via LocalStack).

## 🚀 Overview

InsureCloud is a microservices-based system designed to handle the full lifecycle of insurance policies. It demonstrates professional software engineering practices, including Event-Driven Architecture, Domain-Driven Design (DDD), and Infrastructure as Code (IaC).

## 🛠 Tech Stack

- **Languages:** Java 21
- **Framework:** Spring Boot 3.4
- **Persistence:** PostgreSQL, DynamoDB, Redis
- **Infrastructure:** Docker Compose, LocalStack (S3, SQS, SNS)
- **Search:** Elasticsearch
- **Testing:** JUnit 5, Mockito, Testcontainers
- **API:** OpenAPI (Swagger)

## 🏗 Architecture

The system consists of several autonomous microservices:
- **Policy Service:** Manages policy lifecycle and issuance.
- **Quote Engine:** Calculates premiums based on risk factors.
- **Notification Service:** Handles asynchronous communication with customers.
- **Document Service:** Generates and stores PDF policy documents.

### System Flow Diagram
```mermaid
graph TD
    Client[Client / Frontend] -->|1. Request Quote| API_Quote[Quote Service]
    API_Quote -->|2. Calc Premium + Cache| Redis[(Redis)]
    API_Quote -->|3. Return Quote| Client
    
    Client -->|4. Buy Policy| API_Policy[Policy Service]
    API_Policy -->|5. Save Policy| DB_Postgres[(PostgreSQL)]
    API_Policy -->|6. Publish 'PolicyIssued'| SNS[AWS SNS]
    
    SNS -->|7. Fan-out| SQS_Notif[SQS: notification-queue]
    SNS -->|7. Fan-out| SQS_Doc[SQS: document-queue]
    
    SQS_Notif -->|8. Consume| Service_Notif[Notification Service]
    Service_Notif -->|9. Send Email| Email[SMTP Mock]
    
    SQS_Doc -->|8. Consume| Service_Doc[Document Service]
    Service_Doc -->|9. Generate PDF| PDF[PDF Generator]
    Service_Doc -->|10. Upload PDF| S3[AWS S3]
```

## 🚦 Getting Started

### Prerequisites
- Docker & Docker Compose
- JDK 21
- Maven 3.9+

### Running Infrastructure
To start the local cloud environment (Postgres, LocalStack, etc.):
```bash
docker compose up -d
```

### Running Services
Navigate to a service directory and run:
```bash
mvn spring-boot:run
```

## 📈 API Documentation
Once the services are running, you can access the Swagger UI:
- Policy Service: `http://localhost:8081/swagger-ui.html`

## 📝 License
This project is licensed under the MIT License.
