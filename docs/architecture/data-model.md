# Modelos de datos

El modelo operacional está normalizado para integridad y transacciones. El analítico es una estrella simplificada: una tabla de hechos referencia dimensiones y favorece agregaciones. Son bases físicamente separadas en local para conservar propiedad y ciclo de vida independiente.

```mermaid
erDiagram
 category ||--o{ operational_request : clasifica
 operational_request ||--o{ request_observation : contiene
 operational_request ||--o{ request_status_history : registra
 operational_request ||--o{ outbox_event : origina
```

```mermaid
erDiagram
 dim_date ||--o{ fact_request_transition : fecha
 dim_category ||--o{ fact_request_transition : categoria
 dim_status ||--o{ fact_request_transition : estado
 dim_actor_role ||--o{ fact_request_transition : rol
 processed_event { uuid event_id PK }
```
