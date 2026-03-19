-- Extension pour générer des UUID
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Enum pour le statut utilisateur
CREATE TYPE user_status AS ENUM (
    'PENDING_VERIFICATION',
    'ACTIVE',
    'SUSPENDED',
    'DELETED'
);

-- Table principale des utilisateurs
CREATE TABLE users (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    keycloak_id  VARCHAR(255) NOT NULL UNIQUE,
    email        VARCHAR(255) NOT NULL UNIQUE,
    first_name   VARCHAR(100),
    last_name    VARCHAR(100),
    status       user_status NOT NULL DEFAULT 'PENDING_VERIFICATION',
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ,
    deleted_at   TIMESTAMPTZ
);

-- Index pour accélérer les recherches fréquentes
CREATE INDEX idx_users_keycloak_id ON users(keycloak_id);
CREATE INDEX idx_users_email       ON users(email);
CREATE INDEX idx_users_status      ON users(status);

-- Commentaires pour documenter la table
COMMENT ON TABLE  users              IS 'Utilisateurs synchronisés avec Keycloak';
COMMENT ON COLUMN users.keycloak_id IS 'Sub claim du JWT Keycloak';
COMMENT ON COLUMN users.deleted_at  IS 'NULL = actif, non-NULL = soft deleted';
