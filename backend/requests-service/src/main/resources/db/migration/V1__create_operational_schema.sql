CREATE TABLE category (
 id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, code VARCHAR(50) NOT NULL UNIQUE, name NVARCHAR(120) NOT NULL,
 active BIT NOT NULL CONSTRAINT df_category_active DEFAULT 1, created_at DATETIME2(3) NOT NULL CONSTRAINT df_category_created DEFAULT SYSUTCDATETIME()
);
CREATE TABLE operational_request (
 id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, request_number VARCHAR(24) NOT NULL UNIQUE, category_id UNIQUEIDENTIFIER NOT NULL,
 requester_id UNIQUEIDENTIFIER NOT NULL, assigned_actor_id UNIQUEIDENTIFIER NULL, status VARCHAR(30) NOT NULL, priority VARCHAR(10) NOT NULL,
 description NVARCHAR(2000) NOT NULL, version BIGINT NOT NULL CONSTRAINT df_request_version DEFAULT 0,
 created_at DATETIME2(3) NOT NULL, updated_at DATETIME2(3) NOT NULL,
 CONSTRAINT fk_request_category FOREIGN KEY(category_id) REFERENCES category(id),
 CONSTRAINT ck_request_status CHECK(status IN ('REGISTRADA','EN_ATENCION','RESUELTA','CERRADA')),
 CONSTRAINT ck_request_priority CHECK(priority IN ('BAJA','MEDIA','ALTA'))
);
CREATE INDEX ix_request_status_priority ON operational_request(status,priority,created_at);
CREATE INDEX ix_request_assigned_actor ON operational_request(assigned_actor_id) WHERE assigned_actor_id IS NOT NULL;
CREATE TABLE request_observation (
 id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, request_id UNIQUEIDENTIFIER NOT NULL, actor_id UNIQUEIDENTIFIER NOT NULL,
 actor_role VARCHAR(20) NOT NULL, body NVARCHAR(2000) NOT NULL, created_at DATETIME2(3) NOT NULL,
 CONSTRAINT fk_observation_request FOREIGN KEY(request_id) REFERENCES operational_request(id)
);
CREATE INDEX ix_observation_request_date ON request_observation(request_id,created_at);
CREATE TABLE request_status_history (
 id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, request_id UNIQUEIDENTIFIER NOT NULL, previous_status VARCHAR(30) NULL,
 new_status VARCHAR(30) NOT NULL, actor_id UNIQUEIDENTIFIER NOT NULL, actor_role VARCHAR(20) NOT NULL, occurred_at DATETIME2(3) NOT NULL,
 CONSTRAINT fk_history_request FOREIGN KEY(request_id) REFERENCES operational_request(id)
);
CREATE INDEX ix_history_request_date ON request_status_history(request_id,occurred_at);
CREATE TABLE outbox_event (
 event_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, aggregate_id UNIQUEIDENTIFIER NOT NULL, event_type VARCHAR(100) NOT NULL,
 event_version INT NOT NULL, correlation_id UNIQUEIDENTIFIER NOT NULL, payload NVARCHAR(MAX) NOT NULL, occurred_at DATETIME2(3) NOT NULL,
 status VARCHAR(20) NOT NULL CONSTRAINT df_outbox_status DEFAULT 'PENDING',
 published_at DATETIME2(3) NULL, attempts INT NOT NULL CONSTRAINT df_outbox_attempts DEFAULT 0,
 CONSTRAINT ck_outbox_status CHECK(status IN ('PENDING','PUBLISHED','FAILED')),
 CONSTRAINT ck_outbox_attempts CHECK(attempts >= 0),
 CONSTRAINT ck_outbox_payload_json CHECK(ISJSON(payload)=1)
);
CREATE INDEX ix_outbox_pending ON outbox_event(occurred_at,event_id) WHERE status = 'PENDING';
