-- Performance indexes for foreign keys and frequent filter fields
CREATE INDEX IF NOT EXISTS idx_users_factory_id ON users (factory_id);
CREATE INDEX IF NOT EXISTS idx_machines_factory_id ON machines (factory_id);
CREATE INDEX IF NOT EXISTS idx_alerts_factory_id_status ON alerts (factory_id, status);
CREATE INDEX IF NOT EXISTS idx_ai_insights_factory_id_status ON ai_insights (factory_id, status);
