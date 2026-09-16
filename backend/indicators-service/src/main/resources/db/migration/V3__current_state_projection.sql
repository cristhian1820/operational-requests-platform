CREATE TABLE request_current_state (
 request_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY,
 readable_id VARCHAR(24) NOT NULL,
 category_key BIGINT NOT NULL,
 status_key TINYINT NOT NULL,
 priority VARCHAR(10) NOT NULL,
 registered_date_key INT NOT NULL,
 last_event_id UNIQUEIDENTIFIER NOT NULL UNIQUE,
 updated_at DATETIME2(3) NOT NULL,
 CONSTRAINT fk_current_category FOREIGN KEY(category_key) REFERENCES dim_category(category_key),
 CONSTRAINT fk_current_status FOREIGN KEY(status_key) REFERENCES dim_status(status_key),
 CONSTRAINT fk_current_registered_date FOREIGN KEY(registered_date_key) REFERENCES dim_date(date_key),
 CONSTRAINT ck_current_priority CHECK(priority IN ('BAJA','MEDIA','ALTA'))
);
CREATE INDEX ix_current_status ON request_current_state(status_key);
CREATE INDEX ix_current_category ON request_current_state(category_key);
CREATE INDEX ix_current_registered_date ON request_current_state(registered_date_key);

INSERT INTO dim_category(category_id,category_code,category_name,valid_from) VALUES
 ('11111111-1111-1111-1111-111111111111','ACCESO','Accesos',SYSUTCDATETIME()),
 ('22222222-2222-2222-2222-222222222222','INFRAESTRUCTURA','Infraestructura',SYSUTCDATETIME()),
 ('33333333-3333-3333-3333-333333333333','GENERAL','General',SYSUTCDATETIME());
