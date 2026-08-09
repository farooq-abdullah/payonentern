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

DROP TABLE IF EXISTS password_history;
