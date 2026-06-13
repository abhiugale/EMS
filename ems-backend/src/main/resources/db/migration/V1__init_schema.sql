CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE factories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    address TEXT,
    timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    contract_demand_kw NUMERIC(10, 2) NOT NULL,
    tariff_inr_per_kwh NUMERIC(8, 2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    factory_id UUID REFERENCES factories(id) ON DELETE SET NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'ENERGY_MGR', 'SUPERVISOR', 'VIEWER')),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE machines (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    factory_id UUID REFERENCES factories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    machine_type VARCHAR(100),
    baseline_kwh NUMERIC(10, 3) NOT NULL DEFAULT 0.000,
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (factory_id, name)
);

CREATE TABLE uploads (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    factory_id UUID REFERENCES factories(id) ON DELETE CASCADE,
    uploaded_by UUID REFERENCES users(id) ON DELETE SET NULL,
    filename VARCHAR(255) NOT NULL,
    original_name VARCHAR(255),
    row_count INTEGER DEFAULT 0,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PROCESSING', 'SUCCESS', 'FAILED')),
    error_message TEXT,
    factory_timezone VARCHAR(50) DEFAULT 'Asia/Kolkata',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE energy_readings (
    id BIGSERIAL,
    machine_id UUID NOT NULL REFERENCES machines(id) ON DELETE CASCADE,
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    energy_kwh NUMERIC(10,3),
    active_kw NUMERIC(8,2),
    apparent_kva NUMERIC(8,2),
    reactive_kvar NUMERIC(8,2),
    power_factor NUMERIC(4,3),
    frequency NUMERIC(5,2),
    voltage_r NUMERIC(7,2),
    voltage_y NUMERIC(7,2),
    voltage_b NUMERIC(7,2),
    current_r NUMERIC(7,2),
    current_y NUMERIC(7,2),
    current_b NUMERIC(7,2),
    parts_produced INTEGER,
    upload_id UUID REFERENCES uploads(id) ON DELETE SET NULL,
    source VARCHAR(20) DEFAULT 'excel',
    PRIMARY KEY (id, recorded_at)
);

CREATE TABLE ai_insights (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    factory_id UUID NOT NULL REFERENCES factories(id) ON DELETE CASCADE,
    machine_id UUID REFERENCES machines(id) ON DELETE SET NULL,
    insight_type VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    savings_potential_kwh NUMERIC(10,2),
    savings_potential_inr NUMERIC(10,2),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    resolution_notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by UUID REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE alerts (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    factory_id UUID NOT NULL REFERENCES factories(id) ON DELETE CASCADE,
    machine_id UUID REFERENCES machines(id) ON DELETE SET NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    threshold_value NUMERIC(10,2),
    actual_value NUMERIC(10,2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP WITH TIME ZONE,
    resolved_by UUID REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE alert_notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    alert_id UUID NOT NULL REFERENCES alerts(id) ON DELETE CASCADE,
    channel VARCHAR(50) NOT NULL CHECK (channel IN ('EMAIL', 'WHATSAPP', 'SMS')),
    sent_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50) NOT NULL,
    error_message TEXT
);

CREATE TABLE reports (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    factory_id UUID NOT NULL REFERENCES factories(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL CHECK (type IN ('DAILY', 'MONTHLY', 'PEAK', 'MACHINE')),
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    file_url VARCHAR(512),
    generated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
