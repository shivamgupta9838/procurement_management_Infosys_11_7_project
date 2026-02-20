# Smart Procurement & Vendor Management System

A comprehensive Enterprise Resource Planning (ERP) module designed to streamline procurement processes, manage vendor relationships, and generate insightful performance reports. Built with Java 21 and Spring Boot.

## 🚀 Key Features

### 🔐 Authentication & Security
- **JWT-Based Auth**: Secure stateless authentication using JSON Web Tokens.
- **User Management**: Registration and login functionality with encrypted passwords.

### 🏢 Vendor Management
- **Vendor Profiles**: Manage details like contact info, address, and status.
- **Performance Tracking**: Track and calculate vendor ratings based on feedback.
- **Status Workflow**: Manage vendor lifecycle (PENDING_APPROVAL, ACTIVE, INACTIVE).

### 🛒 Procurement Workflow
- **Requisitions**: Create and manage purchase requests.
- **Purchase Orders (PO)**: Generate and track formal orders sent to vendors.
- **Approvals**: Integrated approval system for procurement documents.

### 📊 Advanced Reporting
- **Performance Reports**: Generate detailed vendor performance reports.
- **Multi-Format Export**: Support for high-quality PDF and Excel exports using JasperReports.
- **Live Data API**: Flexible endpoints supporting both GET and POST requests.

---

## 🛠️ Technology Stack

- **Lanuage**: Java 21
- **Framework**: Spring Boot 3.2.5
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA / Hibernate
- **Reporting**: JasperReports 7.0
- **Documentation**: Swagger / OpenAPI 3.0
- **Security**: Spring Security & JWT

---

## ⚙️ Setup & Installation

### Prerequisites
- JDK 21
- Maven 3.8+
- MySQL Server

### 1. Database Setup
Create a database named `procurement_db`:
```sql
CREATE DATABASE procurement_db;
```

Update `src/main/resources/application.properties` with your MySQL credentials:
```properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 2. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```
The server will start on `http://localhost:8082`.

---

## 📖 API Documentation

Access the interactive Swagger UI to explore and test all available endpoints:
👉 [http://localhost:8082/swagger-ui.html](http://localhost:8082/swagger-ui.html)

### Core Endpoints
- **Auth**: `/auth/login`, `/auth/register`
- **Vendors**: `/vendors/**`
- **Procurement**: `/procurement/requisition/**`, `/procurement/purchase-order/**`
- **Reporting**: `/reports/vendor` (Supports `format=pdf` or `format=excel`)

---

## 📝 License
This project is part of the Infosys Virtual Internship program.
