-- Operational PostgreSQL Schema (Accounts & Transactional Outbox)
CREATE TABLE IF NOT EXISTS accounts (
    account_id UUID PRIMARY KEY,
    iban VARCHAR(34) UNIQUE NOT NULL,
    owner_name VARCHAR(120) NOT NULL,
    balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS outbox_events (
    event_id UUID PRIMARY KEY,
    aggregate_type VARCHAR(50) NOT NULL,
    aggregate_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    payload TEXT NOT NULL,
    processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_iban ON accounts(iban);
CREATE INDEX idx_outbox_unprocessed ON outbox_events(processed) WHERE processed = FALSE;
