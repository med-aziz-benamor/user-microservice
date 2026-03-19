-- Créer la base pour Keycloak
CREATE DATABASE keycloakdb;
CREATE USER keycloak WITH PASSWORD 'keycloakpass';
GRANT ALL PRIVILEGES ON DATABASE keycloakdb TO keycloak;

-- La base userdb est déjà créée par POSTGRES_DB dans docker-compose
