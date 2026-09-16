# Modelos de datos

El operacional permanece normalizado. Outbox añade estado, intentos, próximo intento, lock temporal, error limitado y fecha de publicación.

El analítico conserva `fact_request_transition` como historial. `request_current_state` contiene una fila por solicitud y evita sumar cada transición como una nueva solicitud al calcular estado o categoría actuales. `processed_event.event_id` y `fact_request_transition.event_id` son únicos.

```mermaid
erDiagram
 category ||--o{ operational_request : clasifica
 operational_request ||--o{ request_status_history : registra
 operational_request ||--o{ outbox_event : origina
```

```mermaid
erDiagram
 dim_date ||--o{ fact_request_transition : fecha
 dim_category ||--o{ fact_request_transition : categoria
 dim_status ||--o{ fact_request_transition : destino
 dim_actor_role ||--o{ fact_request_transition : actor
 dim_category ||--o{ request_current_state : categoria
 dim_status ||--o{ request_current_state : estado
 dim_date ||--o{ request_current_state : registro
 processed_event { uuid event_id PK }
 request_current_state { uuid request_id PK }
```
