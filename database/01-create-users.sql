CREATE TABLE system_functions (
    function_code VARCHAR(50) PRIMARY KEY
);

CREATE TABLE roles (
    role_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE UNIQUE INDEX roles_one_default_idx
    ON roles (is_default)
    WHERE is_default;

CREATE TABLE role_functions (
    role_id BIGINT NOT NULL REFERENCES roles(role_id) ON DELETE CASCADE,
    function_code VARCHAR(50) NOT NULL REFERENCES system_functions(function_code) ON DELETE CASCADE,
    PRIMARY KEY (role_id, function_code)
);

CREATE TABLE app_users (
    user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role_id BIGINT NOT NULL REFERENCES roles(role_id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    password_changed_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    must_change_password BOOLEAN NOT NULL DEFAULT FALSE,
    failed_login_count INTEGER NOT NULL DEFAULT 0,
    failed_login_window_started_at TIMESTAMPTZ,
    locked_until TIMESTAMPTZ
);

CREATE TABLE password_history (
    history_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    password_hash VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX shortcutforgettinghistory
    ON password_history (user_id, history_id DESC);

CREATE TABLE password_reset_tokens (
    token_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES app_users(user_id) ON DELETE CASCADE,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX password_reset_tokens_lookup_idx
    ON password_reset_tokens (token_hash, expires_at)
    WHERE used_at IS NULL;

CREATE TABLE audit_log (
    audit_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    actor_user_id BIGINT REFERENCES app_users(user_id) ON DELETE SET NULL,
    actor_username VARCHAR(50),
    action VARCHAR(50) NOT NULL,
    target_type VARCHAR(50) NOT NULL,
    target_id BIGINT,
    target_label VARCHAR(254),
    successful BOOLEAN NOT NULL,
    details VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX audit_log_created_at_idx ON audit_log (created_at DESC, audit_id DESC);
CREATE INDEX audit_log_action_idx ON audit_log (action, created_at DESC);

INSERT INTO system_functions (function_code) VALUES
    ('VIEW_USERS'),
    ('EDIT_USER'),
    ('DELETE_USER'),
    ('RESET_PASSWORD'),
    ('MANAGE_ROLES'),
    ('CHANGE_OWN_PASSWORD'),
    ('UNLOCK_USER'),
    ('VIEW_AUDIT_LOG');

INSERT INTO roles (role_name, is_default) VALUES
    ('Administrator', FALSE),
    ('User', TRUE);

INSERT INTO role_functions (role_id, function_code)
SELECT role.role_id, system_function.function_code
FROM roles role
CROSS JOIN system_functions system_function
WHERE role.role_name = 'Administrator';

INSERT INTO role_functions (role_id, function_code)
SELECT role.role_id, system_function.function_code
FROM roles role
JOIN system_functions system_function
    ON system_function.function_code IN ('VIEW_USERS', 'CHANGE_OWN_PASSWORD')
WHERE role.role_name = 'User';
