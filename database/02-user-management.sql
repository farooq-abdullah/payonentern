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

ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS role VARCHAR(20) NOT NULL DEFAULT 'USER';

DO $$
BEGIN
    ALTER TABLE app_users
        ADD CONSTRAINT app_users_role_check CHECK (role IN ('USER', 'ADMIN'));
EXCEPTION
    WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS password_history (
    history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
