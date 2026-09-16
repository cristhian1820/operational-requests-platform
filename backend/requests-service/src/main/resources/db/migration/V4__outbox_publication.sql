ALTER TABLE outbox_event DROP CONSTRAINT ck_outbox_status;
GO
ALTER TABLE outbox_event ADD
 next_attempt_at DATETIME2(3) NULL,
 locked_at DATETIME2(3) NULL,
 last_error NVARCHAR(500) NULL;
GO
ALTER TABLE outbox_event ADD CONSTRAINT ck_outbox_status
 CHECK(status IN ('PENDING','PROCESSING','PUBLISHED','FAILED'));
GO
DROP INDEX ix_outbox_pending ON outbox_event;
GO
CREATE INDEX ix_outbox_ready ON outbox_event(status,next_attempt_at,occurred_at,event_id)
 INCLUDE(attempts,locked_at);
