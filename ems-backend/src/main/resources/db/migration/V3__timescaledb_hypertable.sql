-- Convert energy_readings into a TimescaleDB hypertable partitioned by the 'recorded_at' column
SELECT create_hypertable('energy_readings', 'recorded_at');

-- Create optimized hypertable indexes for common time-series query patterns
CREATE INDEX IF NOT EXISTS energy_readings_machine_recorded_at_idx ON energy_readings (machine_id, recorded_at DESC);
CREATE INDEX IF NOT EXISTS energy_readings_upload_recorded_at_idx ON energy_readings (upload_id, recorded_at DESC);
