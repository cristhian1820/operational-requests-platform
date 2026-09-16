ALTER TABLE operational_request ADD subject NVARCHAR(200) NULL;
GO
UPDATE operational_request SET subject = N'Solicitud migrada' WHERE subject IS NULL;
GO
ALTER TABLE operational_request ALTER COLUMN subject NVARCHAR(200) NOT NULL;
GO

ALTER TABLE request_status_history ADD reason NVARCHAR(500) NULL;
GO

CREATE SEQUENCE request_number_seq
    AS BIGINT
    START WITH 1
    INCREMENT BY 1
    NO CYCLE;
GO

CREATE INDEX ix_request_requester_created ON operational_request(requester_id, created_at DESC);
CREATE INDEX ix_request_category_created ON operational_request(category_id, created_at DESC);
