# Full-Stack SaaS Boilerplate (Spring Boot + Angular)

This repository contains a ready-to-use boilerplate for building modern SaaS applications. It includes both a **Spring Boot backend** (in the root) and an **Angular frontend** (in `frontend-boilerplate/`).

## ✨ Core Features

Get your subscription-based application running quickly with features like:

* **🛡️ Secure Authentication:** User registration, login, password reset (via email), and profile management using JWT.
* **🏢 Multi-Tenancy Ready:** Backend automatically isolates data based on the logged-in user's organization.
* **💸 Stripe Billing:** Integrated Stripe Checkout for subscriptions, webhook handling for status updates, and a customer portal link.
* **🧑‍💻 Admin Area:** Basic API endpoints (`/api/admin`) for managing users (requires `ROLE_ADMIN`).
* **🚀 Ready to Deploy:** Includes Docker configurations (`Dockerfile` for both backend and frontend, `docker-compose.yaml` for multi-container setup) for easy deployment.
* **⚙️ Great DX:** API documentation (Swagger), basic testing setup, and environment variable configuration.

---

## 🛠️ Tech Stack

* **Backend:** Java 21, Spring Boot 3, Spring Security, Spring Data JPA (Hibernate), PostgreSQL, JWT, Stripe API
* **Frontend:** Angular, TypeScript, Tailwind CSS
* **Serving/Proxy:** Nginx (via Docker)
* **Containerization:** Docker, Docker Compose

---

## 🚀 How to Run (Docker Compose - Recommended)

This is the easiest way to run the full application (Backend + Frontend + Database).

### Prerequisites

1.  **Docker** & **Docker Compose** installed.
2.  A **Stripe** account (Secret Key, Price ID, Webhook Secret).
3.  An **SMTP Server** (like Gmail App Password) for sending emails.

### Steps

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/jhonathanstanley10/saas-boilerplate.git](https://github.com/jhonathanstanley10/saas-boilerplate.git)
    cd saas-boilerplate
    ```

2.  **Configure Secrets:**
    * Copy the example environment file:
        ```sh
        cp .env.example .env
        ```
    * Edit the `.env` file and fill in **all** the required values (Database credentials, JWT secret, Stripe keys, Email credentials). This file is ignored by Git.

3.  **Build and Run with Docker Compose:**
    From the root directory of the project, run:
    ```sh
    docker-compose up --build -d
    ```
    * `--build` ensures the Docker images are built (needed on the first run or after code changes).
    * `-d` runs the containers in the background.

4.  **Access the Application:**
    * Frontend: Open your browser to `http://localhost` (or the port you mapped for Nginx if you changed it from `80:80`).
    * Backend API (via Nginx): `http://localhost/api/...`
    * Swagger API Docs: `http://localhost/swagger-ui.html`

### Stopping the Application

```sh
docker-compose down