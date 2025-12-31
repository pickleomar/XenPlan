# XenPlan – Enterprise Event Management Platform

![Project Status](https://img.shields.io/badge/Status-Active_Development-brightgreen)
![Java Version](https://img.shields.io/badge/Java-21_LTS-orange)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-green)
![Vaadin](https://img.shields.io/badge/Vaadin-24.4-blue)
![Database](https://img.shields.io/badge/PostgreSQL-16-336791)

## Abstract

**XenPlan** is a comprehensive event management and reservation system developed as a core component of a doctoral research project. It explores advanced object-oriented design patterns, secure enterprise architecture, and the integration of server-side Java UI frameworks (Vaadin Flow) with modern Spring Boot ecosystems.

The platform facilitates a complete lifecycle for event organization, enabling seamless interaction between three distinct actors: **Organizers** (event creation & management), **Clients** (discovery & booking), and **Administrators** (platform governance).

---

## Key Features

### For Public & Clients
* **Event Discovery:** Browse events with advanced filtering by category (Concert, Theatre, Sport, etc.), date, and location.
* **Responsive UI:** Modern, glassmorphism-inspired interface optimized for desktop and mobile.
* **Reservation System:** Real-time seat availability checks and secure booking workflow.
* **User Dashboard:** Manage profile details and view booking history.

### For Organizers
* **Event Lifecycle Management:** Create, edit, publish, cancel, and delete events.
* **Validation Rules:** Robust business logic prevents conflicts (e.g., end dates before start dates, negative capacity).
* **Operational Dashboard:** Real-time overview of created events and their statuses.

### For Administrators
* **Platform Oversight:** Global view of all platform activities.
* **User Management:** Role-based access control (RBAC) enforcement.

---

## Technology Stack

### Backend & Core
* **Language:** Java 21 (LTS)
* **Framework:** Spring Boot 3.2.5
* **Data Access:** Spring Data JPA / Hibernate
* **Security:** Spring Security (RBAC, BCrypt hashing)
* **Validation:** Bean Validation (Hibernate Validator)

### Frontend (UI/UX)
* **Framework:** Vaadin Flow 24.4.8 (Server-side Java UI)
* **Styling:** Lumo Theme, Custom CSS (Glassmorphism), Vaadin Icons
* **Components:** Responsive Grid, Form Layouts, DateTime Pickers

### Data & Infrastructure
* **Database:** PostgreSQL 16 (Production/Dev), H2 (Testing)
* **Migration:** Liquibase (Schema version control)
* **Containerization:** Docker & Docker Compose
* **Configuration:** `spring-dotenv` for secure environment variable management

---

## Getting Started

Follow these instructions to set up the project locally for development and testing.

### Prerequisites
* **Java 21 JDK** installed.
* **Docker Desktop** (or Docker Engine + Compose) installed.
* **Git** installed.

### 1. Clone the Repository
```bash
git clone [https://github.com/your-username/XenPlan.git](https://github.com/your-username/XenPlan.git)
cd XenPlan

```

### 2. Configure Environment Variables

The project uses a `.env` file to manage secrets securely.

1. Copy the example configuration:
```bash
cp .env.example .env

```


2. (Optional) Edit `.env` if you need to change ports or credentials. The defaults work out-of-the-box with Docker.

### 3. Start Database Infrastructure

Use Docker Compose to spin up PostgreSQL and pgAdmin (Database GUI).

```bash
docker-compose up -d

```

* **PostgreSQL** will run on port `5432`.
* **pgAdmin** (optional) will run on `http://localhost:5050`.

### 4. Run the Application

You can run the application using the Maven wrapper included in the project.

**On Linux/Mac:**

```bash
./mvnw clean spring-boot:run

```

**On Windows:**

```cmd
mvnw.cmd clean spring-boot:run

```

Once started, the application will be accessible at: **`http://localhost:8080`**

---

## Default Credentials

The system initializes with a default administrator account:

* **Username (Email):** `admin@xenplan.com`
* **Password:** `admin` (or check `DataInitializer.java`)

*Note: New users can register via the "Sign Up" page.*

---

## Project Architecture

```plaintext
src/main/java/com/xenplan/app
├── config/          # Spring & Vaadin configurations (Security, OpenAPI)
├── domain/          # Domain layer (Entities, Enums, Exceptions)
├── repository/      # Data access layer (JPA Repositories)
├── service/         # Business logic layer
├── security/        # Authentication & Authorization logic
└── ui/              # Presentation layer (Vaadin Views & Components)
    ├── component/   # Reusable UI widgets (Cards, Dialogs)
    ├── layout/      # Main application layouts (NavBar)
    └── view/        # Role-specific views (Admin, Client, Organizer)

```

---

## Testing

To execute the unit and integration test suite:

```bash
./mvnw test

```

---

## License

This project is proprietary research software. Unauthorized copying of this file, via any medium, is strictly prohibited.

```
