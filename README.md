# XenPlan

**Event Reservation Management System**  
Built with Spring Boot 3.x, Vaadin 24, Java 17, and H2 Database.

## Overview
XenPlan allows administrators, organizers, and clients to manage events and reservations through a web interface. Features include:

- User registration, authentication, and role-based access (ADMIN, ORGANIZER, CLIENT)  
- Event creation, editing, and listing  
- Reservation booking with unique 8-character codes  
- Reservation rules enforcement (capacity limits, cancellation deadlines)  
- Responsive Vaadin-based UI  

## Technologies
- **Backend:** Java 17, Spring Boot 3.x, Spring Data JPA, Spring Security  
- **Frontend:** Vaadin 24  
- **Database:** H2 (in-memory)  
- **Build & Dependency Management:** Maven  

## Getting Started

### Prerequisites
- Java 17  
- Maven 3.x  

### Running the Application
```bash
mvn clean install
mvn spring-boot:run
