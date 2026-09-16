CREATE TABLE processed_event (event_id UNIQUEIDENTIFIER NOT NULL PRIMARY KEY, event_type VARCHAR(100) NOT NULL, processed_at DATETIME2(3) NOT NULL CONSTRAINT df_processed_at DEFAULT SYSUTCDATETIME());
CREATE TABLE dim_date (date_key INT NOT NULL PRIMARY KEY, full_date DATE NOT NULL UNIQUE, year_number SMALLINT NOT NULL, month_number TINYINT NOT NULL, day_number TINYINT NOT NULL);
CREATE TABLE dim_category (category_key BIGINT IDENTITY PRIMARY KEY, category_id UNIQUEIDENTIFIER NOT NULL UNIQUE, category_code VARCHAR(50) NOT NULL, category_name NVARCHAR(120) NOT NULL, valid_from DATETIME2(3) NOT NULL, valid_to DATETIME2(3) NULL);
CREATE TABLE dim_status (status_key TINYINT NOT NULL PRIMARY KEY, status_code VARCHAR(30) NOT NULL UNIQUE);
CREATE TABLE dim_actor_role (actor_role_key TINYINT NOT NULL PRIMARY KEY, role_code VARCHAR(20) NOT NULL UNIQUE);
CREATE TABLE fact_request_transition (
 transition_key BIGINT IDENTITY PRIMARY KEY, event_id UNIQUEIDENTIFIER NOT NULL UNIQUE, request_id UNIQUEIDENTIFIER NOT NULL,
 date_key INT NOT NULL, category_key BIGINT NOT NULL, from_status_key TINYINT NULL, to_status_key TINYINT NOT NULL,
 actor_role_key TINYINT NOT NULL, occurred_at DATETIME2(3) NOT NULL,
 CONSTRAINT fk_fact_date FOREIGN KEY(date_key) REFERENCES dim_date(date_key), CONSTRAINT fk_fact_category FOREIGN KEY(category_key) REFERENCES dim_category(category_key),
 CONSTRAINT fk_fact_from_status FOREIGN KEY(from_status_key) REFERENCES dim_status(status_key), CONSTRAINT fk_fact_to_status FOREIGN KEY(to_status_key) REFERENCES dim_status(status_key),
 CONSTRAINT fk_fact_actor_role FOREIGN KEY(actor_role_key) REFERENCES dim_actor_role(actor_role_key)
);
CREATE INDEX ix_fact_request_occurred ON fact_request_transition(request_id,occurred_at);
