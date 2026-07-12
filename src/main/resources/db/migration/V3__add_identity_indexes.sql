CREATE INDEX idx_login_attempts_username_created ON login_attempts (username_attempted, created_at);
CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor_created ON audit_logs (actor_id, created_at);
