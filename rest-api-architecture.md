```mermaid
graph LR
    subgraph Client
        direction LR
        C[Browser / Mobile]
    end

    subgraph "SaaS API Server (Spring Boot)"
        direction LR
        SS[Spring Security Filter Chain] --> CL["Controller Layer<br>(e.g., TodoController)"];
        CL --> SL["Service Layer<br>(e.g., TodoService)"];
        SL --> RL["Repository Layer<br>(e.g., TodoRepository)"];

        subgraph AOP
          direction TB
          A["TenantFilterAspect<br>(Intercepts @Transactional)"]
        end

        CL -- Calls --> SL;
        SL -- Intercepted by --> A;
        A -- Applies Filter --> SL;
        SL -- Calls --> RL;
    end

    subgraph External Services
        direction TB
        DB[("Database<br>PostgreSQL")]; // Database symbol
        ST[Stripe API];
    end

    C -- "1. HTTP Request<br>(e.g., GET /api/todos)" --> SS;
    SS -- "2. Validate JWT<br>Set TenantContext" --> CL;
    RL -- "5. SQL Query<br>(Auto-filtered)" --> DB;
    SL -- "Calls External API<br>(e.g., POST /create-checkout)" --> ST;

    classDef default fill:#fffbe6,stroke:#333,stroke-width:2px;
    classDef client fill:transparent,stroke:#333,stroke-width:2px;
    classDef server fill:#e6f7ff,stroke:#333,stroke-width:2px;
    classDef db fill:#fff0f0,stroke:#333,stroke-width:2px;
    classDef stripe fill:#f3e8ff,stroke:#5d35b1,stroke-width:2px;
    classDef aspect fill:#fff5f5,stroke:#c92a2a,stroke-width:2px;

    class C client;
    class SS,CL,SL,RL default;
    class DB db;
    class ST stripe;
    class A aspect;
