-- =========================================================
-- ERD init.sql (PostgreSQL)
-- DB: fininfra / USER: fin
-- =========================================================

-- 0) Extensions
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- 1) auth_users
-- =========================================================
CREATE TABLE IF NOT EXISTS auth_users (
    user_id        uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username       varchar(100) NOT NULL UNIQUE,
    password_hash  varchar(255) NOT NULL,
    role           varchar(30)  NOT NULL,
    is_active      boolean      NOT NULL DEFAULT true,
    created_at     timestamptz  NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_auth_users_role
    ON auth_users(role);

CREATE INDEX IF NOT EXISTS idx_auth_users_active
    ON auth_users(is_active);

-- =========================================================
-- 2) accounts
-- =========================================================
CREATE TABLE IF NOT EXISTS accounts (
    account_id      varchar(40)  PRIMARY KEY,           -- 예: 0001, A-00000001 등
    owner_user_id   uuid         NOT NULL,
    balance         numeric(19,2) NOT NULL DEFAULT 0.00,
    currency        char(3)      NOT NULL,              -- KRW
    status          varchar(20)  NOT NULL,              -- ACTIVE/FROZEN/CLOSED/DORMANT
    version         bigint       NOT NULL DEFAULT 0,     -- optimistic lock
    created_at      timestamptz  NOT NULL DEFAULT now(),

    CONSTRAINT fk_accounts_owner
    FOREIGN KEY (owner_user_id) REFERENCES auth_users(user_id)
    );

CREATE INDEX IF NOT EXISTS idx_accounts_owner_user_id
    ON accounts(owner_user_id);

CREATE INDEX IF NOT EXISTS idx_accounts_status
    ON accounts(status);

-- =========================================================
-- 3) idempotency_keys
-- =========================================================
CREATE TABLE IF NOT EXISTS idempotency_keys (
    idempotency_key  varchar(120) PRIMARY KEY,
    request_hash     char(64)     NOT NULL,            -- SHA-256 hex
    status           varchar(20)  NOT NULL,            -- IN_PROGRESS/COMPLETED/FAILED/EXPIRED
    first_seen_at    timestamptz  NOT NULL DEFAULT now(),
    last_seen_at     timestamptz  NOT NULL DEFAULT now()
    );

CREATE INDEX IF NOT EXISTS idx_idem_status
    ON idempotency_keys(status);

CREATE INDEX IF NOT EXISTS idx_idem_last_seen
    ON idempotency_keys(last_seen_at);

-- =========================================================
-- 4) transfer_requests
-- =========================================================
CREATE TABLE IF NOT EXISTS transfer_requests (
    transfer_id        uuid         PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key    varchar(120) NOT NULL UNIQUE,
    from_account_id    varchar(40)  NOT NULL,
    to_account_id      varchar(40)  NOT NULL,
    amount             numeric(19,2) NOT NULL,
    status             varchar(20)  NOT NULL,          -- REQUESTED/ACCEPTED/COMPLETED/FAILED/DUPLICATE
    requested_at       timestamptz  NOT NULL DEFAULT now(),
    completed_at       timestamptz  NULL,

    CONSTRAINT fk_transfer_idem
    FOREIGN KEY (idempotency_key) REFERENCES idempotency_keys(idempotency_key),

    CONSTRAINT fk_transfer_from_account
    FOREIGN KEY (from_account_id) REFERENCES accounts(account_id),

    CONSTRAINT fk_transfer_to_account
    FOREIGN KEY (to_account_id) REFERENCES accounts(account_id),

    CONSTRAINT chk_transfer_amount_positive
    CHECK (amount > 0),

    CONSTRAINT chk_transfer_accounts_different
    CHECK (from_account_id <> to_account_id)
    );

CREATE INDEX IF NOT EXISTS idx_transfer_status
    ON transfer_requests(status);

CREATE INDEX IF NOT EXISTS idx_transfer_requested_at
    ON transfer_requests(requested_at);

CREATE INDEX IF NOT EXISTS idx_transfer_from_account
    ON transfer_requests(from_account_id);

CREATE INDEX IF NOT EXISTS idx_transfer_to_account
    ON transfer_requests(to_account_id);

-- =========================================================
-- 5) outbox_events
-- =========================================================
CREATE TABLE IF NOT EXISTS outbox_events (
                                             outbox_id        uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_type   varchar(30) NOT NULL,             -- TRANSFER/ACCOUNT/LEDGER
    aggregate_id     uuid        NOT NULL,             -- 보통 transfer_id
    event_type       varchar(60) NOT NULL,             -- TransferRequested 등
    payload          jsonb       NOT NULL,
    status           varchar(20) NOT NULL DEFAULT 'NEW', -- NEW/PUBLISHED/FAILED/RETRYING
    created_at       timestamptz NOT NULL DEFAULT now(),
    published_at     timestamptz NULL
    );

CREATE INDEX IF NOT EXISTS idx_outbox_status_created
    ON outbox_events(status, created_at);

CREATE INDEX IF NOT EXISTS idx_outbox_aggregate
    ON outbox_events(aggregate_type, aggregate_id);

-- =========================================================
-- 6) ledger_events (append-only, hash chain)
-- =========================================================
CREATE TABLE IF NOT EXISTS ledger_events (
                                             event_seq     bigserial   PRIMARY KEY,
                                             transfer_id   uuid        NOT NULL,
                                             event_type    varchar(30) NOT NULL,               -- DEBIT_POSTED/CREDIT_POSTED/TRANSFER_COMPLETED/TRANSFER_FAILED
    payload       jsonb       NOT NULL,
    prev_hash     char(64)    NULL,                   -- 첫 이벤트면 NULL 또는 'GENESIS'(정책)
    hash          char(64)    NOT NULL,
    occurred_at   timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_ledger_transfer
    FOREIGN KEY (transfer_id) REFERENCES transfer_requests(transfer_id)
    );

CREATE INDEX IF NOT EXISTS idx_ledger_transfer_id
    ON ledger_events(transfer_id);

CREATE INDEX IF NOT EXISTS idx_ledger_occurred_at
    ON ledger_events(occurred_at);

-- =========================================================
-- 7) ledger_snapshots
-- =========================================================
CREATE TABLE IF NOT EXISTS ledger_snapshots (
                                                snapshot_id      uuid        PRIMARY KEY DEFAULT gen_random_uuid(),
    as_of_event_seq  bigint      NOT NULL,
    state            jsonb       NOT NULL,
    created_at       timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT fk_snapshot_event_seq
    FOREIGN KEY (as_of_event_seq) REFERENCES ledger_events(event_seq)
    );

CREATE INDEX IF NOT EXISTS idx_snapshots_as_of_event_seq
    ON ledger_snapshots(as_of_event_seq);
