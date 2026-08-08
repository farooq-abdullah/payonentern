ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS password_changed_at TIMESTAMPTZ;

UPDATE app_users
SET password_changed_at = created_at
WHERE password_changed_at IS NULL;

ALTER TABLE app_users
    ALTER COLUMN password_changed_at SET NOT NULL,
    ALTER COLUMN password_changed_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS must_change_password BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS password_history (
    history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
