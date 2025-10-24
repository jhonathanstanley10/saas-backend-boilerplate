# Production-Ready SaaS Backend Boilerplate

This is a comprehensive backend boilerplate for building modern, multi-tenant SaaS applications. It's built with Spring Boot and comes pre-configured with a complete, secure, and scalable feature set, allowing you to go from idea to production in a fraction of the time.

## ✨ Core Features

This boilerplate includes everything you need to get a subscription-based application running:

* **🛡️ Secure Authentication:** Full user lifecycle management.
    * User Registration & Login (JWT-based).
    * Secure Refresh Token rotation.
    * Password Reset flow via asynchronous email.
    * User Profile management endpoints.

* **🏢 Automated Multi-Tenancy:** Securely isolate data for all your customers.
    * **Automatic "Write" Isolation:** New data (e.g., a Todo) is automatically tagged with the user's `tenantId`.
    * **Automatic "Read" Isolation:** A developer can simply call `repository.findAll()` and Hibernate automatically filters for the current user's tenant.

* **💸 Stripe Subscription Billing:** Ready-to-go monetization.
    * Stripe Checkout for new subscriptions.
    * Stripe Webhook handling to automatically update subscription status (`FREE`, `PREMIUM`) in your database.
    * Stripe Customer Portal endpoint for users to manage their own billing.

* **🧑‍💻 Admin API:** Manage your application from a secure `/api/admin` endpoint (requires `ROLE_ADMIN`).

* **🚀 Excellent Developer Experience**
    * **Containerized:** Full `Dockerfile` for reproducible builds and easy deployment.
    * **API Documentation:** Live, interactive API docs with Swagger (Springdoc).
    * **Tested:** Includes a suite of Unit and Integration tests.
    * **Secure by Default:** All secrets are loaded from environment variables, and a template file (`application.properties.template`) is provided.
    * **Robust Error Handling:** A global exception handler provides clean, predictable JSON error responses.
    * **Example CRUD:** A multi-tenant `Todo` API is included as a template for your own features.

---

## 🛠️ Tech Stack

* **Java 21** & **Spring Boot 3**
* **Database:** Spring Data JPA (Hibernate)
* **Security:** Spring Security & JWT (JSON Web Tokens)
* **Payments:** Stripe
* **Email:** Spring Boot Mail
* **Containerization:** Docker
* **Testing:** JUnit 5, Mockito
* **API Docs:** Springdoc (Swagger UI)

---

## 🚀 How to Run

### Prerequisites

1.  **Java JDK 21** or later.
2.  **Docker** & **Docker Compose**.
3.  A **PostgreSQL** database (or modify `application.properties` for your DB).
4.  An **SMTP Server** (like a Gmail "App Password") for sending emails.
5.  A **Stripe** account (with Secret Key, Price ID, and Webhook Secret).

### 1. Run Locally (for Development)

1.  **Clone the repository:**
    ```sh
    git clone https://github.com/jhonathanstanley10/backend-boilerplate.git
    cd backend-boilerplate
    ```

2.  **Create your database:**
    Create a new PostgreSQL database (e.g., `boilerplate_db`).

3.  **Configure your secrets:**
    Copy the `application.properties.template` to a new file named `application.properties`.
    ```sh
    cp application.properties.template application.properties
    ```
    This file is in the `.gitignore`, so your secrets are safe.

4.  **Edit `application.properties`:**
    Fill in all the values for your database, JWT secret, Stripe keys, and email credentials.

5.  **Run the application:**
    ```sh
    ./mvnw spring-boot:run
    ```

### 2. Run with Docker (Recommended)

This is the best way to run the application in a production-like environment. It reads all configuration from environment variables.

1.  **Build the Docker Image:**
    From the root of the project, run:
    ```sh
    docker build -t backend-boilerplate .
    ```

2.  **Run the Container:**
    Run the container by passing all your secrets as environment variables. This command connects the container to your host machine's database.

    ```sh
    docker run \
      --name boilerplate-app \
      -p 8080:8080 \
      \
      # --- Database Connection ---
      # This special hostname lets the container connect to your host's database
      -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/boilerplate_db \
      -e DB_USERNAME=your_db_username \
      -e DB_PASSWORD=your_db_password \
      \
      # --- JWT Secret ---
      -e APPLICATION_JWT_SECRET_KEY=your_very_long_and_secret_jwt_key_here \
      \
      # --- Stripe Keys ---
      -e STRIPE_API_SECRET_KEY=sk_test_... \
      -e STRIPE_API_PRICE_ID=price_... \
      -e STRIPE_API_WEBHOOK_SECRET=whsec_... \
      \
      # --- Email Credentials ---
      -e SPRING_MAIL_USERNAME=your-email@gmail.com \
      -e SPRING_MAIL_PASSWORD=your-gmail-app-password \
      \
      # --- Run the image ---
      backend-boilerplate
    ```
    *(**Note for Linux users:** You may need to replace `host.docker.internal` with your machine's IP or add `--add-host=host.docker.internal:host-gateway` to the command.)*

---

## 📖 API Documentation

Once the application is running, you can access the interactive Swagger UI to see and test all available endpoints:

**http://localhost:8080/swagger-ui.html**

---

## 🧪 Running Tests

To run the built-in unit and integration tests, use the Maven wrapper:

```sh
./mvnw test
