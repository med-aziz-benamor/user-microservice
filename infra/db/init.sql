-- Créer la base pour Keycloak
CREATE DATABASE keycloakdb;
CREATE USER keycloak WITH PASSWORD 'keycloakpass';
GRANT ALL PRIVILEGES ON DATABASE keycloakdb TO keycloak;

-- PostgreSQL 15+ : donner accès au schema public
\connect keycloakdb
GRANT ALL ON SCHEMA public TO keycloak;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO keycloak;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO keycloak;
