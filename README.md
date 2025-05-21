## Full Project Description

The Electronic Store is a full-stack e-commerce web application that allows users to browse and purchase electronic products. The system is designed to simulate the functionality of a real-world electronics store, with features for product browsing, shopping cart management, order processing, and role-based access to administrative functionalities.

The application supports three user roles:
- **Customer** – Can register, log in, view products, add them to the cart, and place orders.
- **Worker** – Can access and manage orders (e.g., update their status as processed, shipped, etc.).
- **Admin** – Has full access to manage categories, products, and orders through an admin panel.

Key features include:
- A structured product catalog with categories, prices, discounts, and stock levels.
- A shopping cart that dynamically updates total prices and validates product availability.
- Order placement with automatic order creation and stock management.
- Administrative functionalities allowing product creation, update, and deletion.
- Secure login and registration with role-based access using JWT tokens or session management (depending on implementation).
- REST API for decoupled frontend-backend communication.
- A responsive frontend built with React, supporting real-time interaction with the backend.

The system is divided into two separate modules:
- **Backend** (Java Spring Boot): Handles business logic, database interactions, and API endpoints.
- **Frontend** (React + Redux): Provides a dynamic and user-friendly interface for both customers and admin users.

This project is suitable as a foundation for a production-level e-commerce platform and demonstrates the integration of modern web technologies in a modular, scalable architecture.

Architecture schema:
![image](https://github.com/user-attachments/assets/48ab2a4e-13e7-4455-a6e9-98775885f411)

## 🚀 How to Run the Project

This project consists of two main parts:
- **Backend**: Spring Boot application using Gradle and PostgreSQL
- **Frontend**: React application using Vite

---

### 🖥️ Backend (Spring Boot + Gradle)

1. **Install dependencies**  
   Ensure you have the following installed:
   - Java 17 or newer
   - Gradle (or use the included Gradle wrapper: `./gradlew`)
   - PostgreSQL running locally or on Docker

2. **Set up the database**  
   Create a PostgreSQL database (e.g., `ecommerce_store`), and configure your credentials in:
   `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/mydatabase
   spring.datasource.username=admin
   spring.datasource.password=admin

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true

   From the root of the backend project:
    ./gradlew bootRun

   Install dependencies
   npm install
   npm run dev
   ```
   ## Technologies Used

- **Spring Boot**  
  I chose Spring Boot because I enjoy programming in Java and have been learning it for some time, especially during my summer break. Spring Boot makes it easier to build backend applications quickly and    efficiently.

- **React**  
  I picked React for the frontend since I had little prior experience with frontend development. React seems suitable for beginners and allows building interactive user interfaces with relative ease.

## ERD:
![Zrzut ekranu 2025-05-21 172546](https://github.com/user-attachments/assets/6c4d278f-458c-4cee-b850-92b4a80bc417)





