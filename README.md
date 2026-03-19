# 🧑‍💼 User Microservice — PICloud

> Microservice complet de gestion des utilisateurs avec Spring Boot, Keycloak, Kafka, Redis et Angular.

---

## 📋 Table des Matières

- [Vue d'ensemble](#-vue-densemble)
- [Stack Technique](#-stack-technique)
- [Architecture](#-architecture)
- [Structure du Projet](#-structure-du-projet)
- [Démarrage Rapide](#-démarrage-rapide)
- [Configuration Keycloak](#-configuration-keycloak)
- [API REST](#-api-rest)
- [Kafka Events — M5](#-kafka-events--m5)
- [Tests](#-tests)
- [Membres de l'équipe](#-membres-de-léquipe)

---

## 🔭 Vue d'ensemble

Le **User Microservice** est un service autonome responsable de la gestion complète du cycle de vie des utilisateurs dans l'architecture microservices PICloud. Il expose une API REST sécurisée et publie des événements asynchrones via Kafka.

**Ce que fait ce service :**
- ✅ Créer / lire / modifier / supprimer des utilisateurs (CRUD)
- ✅ Synchroniser chaque utilisateur avec Keycloak (OAuth2 / JWT)
- ✅ Sécuriser les endpoints par rôle (RBAC : ROLE_USER, ROLE_ADMIN)
- ✅ Mettre en cache les données via Redis
- ✅ Publier des événements Kafka à chaque opération CRUD
- ✅ Exposer une interface Angular connectée à Keycloak

---

## 🛠 Stack Technique

| Technologie | Version | Rôle |
|---|---|---|
| Spring Boot | 4.0.3 | API REST principale |
| Java | 25 | Langage |
| PostgreSQL | 16 | Base de données |
| Redis | 7 | Cache |
| Apache Kafka | 7.6.0 (Confluent) | Messagerie asynchrone |
| Keycloak | 24.0.1 | Authentification OAuth2 / JWT |
| Angular | 20 | Frontend SPA |
| Docker | Latest | Infrastructure containerisée |
| Flyway | Intégré | Migrations SQL versionnées |

---

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT ANGULAR :4200                    │
│              (Keycloak PKCE S256 Authentication)            │
└──────────────────────────┬──────────────────────────────────┘
                           │  JWT Bearer Token
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  SPRING BOOT API :8081                      │
│                                                             │
│  SecurityConfig ──► JwtAuthenticationConverter              │
│       │                                                     │
│  UserController ──► UserService ──► UserRepository          │
│                          │                ▼                 │
│                          │          PostgreSQL :5432        │
│                          │                                  │
│                          ├──► KeycloakAdminService          │
│                          │         ▼                        │
│                          │    Keycloak :8080                │
│                          │                                  │
│                          ├──► Redis Cache :6379             │
│                          │                                  │
│                          └──► UserEventProducer             │
│                                    ▼                        │
└────────────────────────────────────┼────────────────────────┘
                                     │
                    ┌────────────────▼──────────────┐
                    │         KAFKA :9092            │
                    │                               │
                    │  user.created                 │
                    │  user.updated                 │
                    │  user.deleted                 │
                    │  user.events.DLQ              │
                    └───────────────────────────────┘
```

---

## 📁 Structure du Projet

```
user-microservice/
├── infra/                              ← M2 — Infrastructure Docker
│   ├── docker-compose.yml
│   ├── .env.example
│   ├── db/
│   │   ├── init.sql                   ← Création des BDD
│   │   └── migrations/
│   │       └── V1__create_users.sql   ← Schéma de la table users
│   └── keycloak/
│       └── realm-export.json          ← M1 — Configuration Keycloak
│
├── user-service/                       ← Application Spring Boot
│   └── src/main/java/com/example/user_service/
│       ├── config/
│       │   ├── SecurityConfig.java    ← M3 — JWT, CORS, routes
│       │   └── RedisConfig.java       ← M4 — Cache Manager
│       ├── security/
│       │   └── JwtAuthenticationConverter.java
│       ├── model/
│       │   ├── User.java              ← M4 — Entité JPA
│       │   └── UserStatus.java
│       ├── dto/
│       │   ├── CreateUserRequest.java
│       │   ├── UpdateUserRequest.java
│       │   └── UserResponse.java
│       ├── repository/
│       │   └── UserRepository.java
│       ├── service/
│       │   ├── UserService.java       ← M4 — CRUD + Cache Redis
│       │   └── KeycloakAdminService.java
│       ├── controller/
│       │   └── UserController.java    ← M4 — REST Endpoints
│       ├── kafka/                     ← ⭐ M5 — Kafka Events
│       │   ├── UserEvent.java
│       │   ├── KafkaProducerConfig.java
│       │   ├── UserEventProducer.java
│       │   ├── KafkaConsumerConfig.java
│       │   └── UserEventConsumer.java
│       └── exception/
│           └── GlobalExceptionHandler.java
│
└── frontend/                           ← M6 — Angular App
    └── src/app/
        ├── app.config.ts              ← Initialisation Keycloak
        ├── app.routes.ts
        ├── components/
        │   ├── login/
        │   └── user-profile/
        ├── services/
        │   ├── auth.service.ts
        │   └── user.service.ts
        └── guards/
            └── auth.guard.ts
```

---

## 🚀 Démarrage Rapide

### Prérequis

- Java 17+
- Maven 3.8+
- Docker Desktop
- Node.js 18+ et Angular CLI (`npm install -g @angular/cli`)

### 1. Cloner le repo

```bash
git clone https://github.com/nacefmoula/user-microservice.git
cd user-microservice
```

### 2. Lancer l'infrastructure Docker

```bash
cd infra/
cp .env.example .env
docker compose up -d
```

Vérifier que tout tourne :

```bash
docker compose ps
```

| Container | Status | Port |
|---|---|---|
| postgres | Up (healthy) | 5432 |
| redis | Up (healthy) | 6379 |
| kafka | Up (healthy) | 9092 |
| keycloak | Up | 8080 |
| zookeeper | Up | 2181 |

### 3. Configurer Keycloak

Ouvre **http://localhost:8080** → Administration Console (admin / devpassword) puis configure le realm `myapp-realm` selon le guide M1 ou importe `infra/keycloak/realm-export.json`.

### 4. Lancer Spring Boot

```bash
cd user-service/
KEYCLOAK_CLIENT_SECRET=<ton_secret> ./mvnw spring-boot:run
```

Vérifier :

```bash
curl http://localhost:8081/actuator/health
# → {"status": "UP"}
```

### 5. Lancer Angular

```bash
cd frontend/
ng serve
# Ouvre http://localhost:4200
```

---

## 🔐 Configuration Keycloak

| Élément | Valeur |
|---|---|
| Realm | `myapp-realm` |
| Client Angular | `angular-client` (Public, PKCE S256) |
| Client Backend | `user-service` (Confidential, Service Account) |
| Rôles | `ROLE_USER`, `ROLE_ADMIN`, `ROLE_MANAGER` |
| URL JWKS | `http://localhost:8080/realms/myapp-realm/protocol/openid-connect/certs` |

**Comptes de test :**

| Email | Password | Rôle |
|---|---|---|
| admin@test.com | Test1234! | ROLE_ADMIN |
| user@test.com | Test1234! | ROLE_USER |

---

## 🌐 API REST

**Base URL :** `http://localhost:8081/api`  
**Auth :** `Authorization: Bearer <JWT>`  
**Swagger UI :** `http://localhost:8081/swagger-ui.html`

| Méthode | Endpoint | Auth | Description |
|---|---|---|---|
| `POST` | `/api/users` | Public | Créer un utilisateur |
| `GET` | `/api/users/me` | JWT | Profil de l'utilisateur connecté |
| `GET` | `/api/users` | ADMIN | Lister tous les utilisateurs |
| `GET` | `/api/users/{id}` | ADMIN | Obtenir un utilisateur par ID |
| `PATCH` | `/api/users/{id}` | ADMIN | Modifier un utilisateur |
| `DELETE` | `/api/users/{id}` | ADMIN | Supprimer un utilisateur |

**Exemple — Créer un utilisateur :**

```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "password": "SecurePass123!"
  }'
```

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "keycloakId": "abc123-...",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "status": "ACTIVE",
  "createdAt": "2026-03-19T10:30:00Z"
}
```

---

## 📨 Kafka Events — M5

> **Développé par : Aziz Benamor (Membre 5)**

### Topics

| Topic | Déclencheur | Description |
|---|---|---|
| `user.created` | `UserService.create()` | Publié après création |
| `user.updated` | `UserService.update()` | Publié après modification |
| `user.deleted` | `UserService.delete()` | Publié après suppression |
| `user.events.DLQ` | Échec après 3 tentatives | Dead Letter Queue |

### Structure UserEvent

```json
{
  "eventType": "CREATED",
  "userId": "550e8400-...",
  "keycloakId": "abc123-...",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "timestamp": "2026-03-19T10:30:00Z"
}
```

### Dead Letter Queue (DLQ)

En cas d'échec du consumer, backoff exponentiel : 1s → 2s → 4s → DLQ après 3 tentatives.

### Vérifier les topics

```bash
docker exec kafka kafka-topics \
  --bootstrap-server localhost:9092 --list
```

---

## 🧪 Tests

```bash
cd user-service/
./mvnw test -Dtest=UserServiceTest
```

```
Tests run: 7, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test | Scénario |
|---|---|
| `create_success` | Création → Keycloak + PostgreSQL + Kafka |
| `create_emailExists` | Email dupliqué → 409 Conflict |
| `findByKeycloakId_success` | User trouvé → UserResponse |
| `findByKeycloakId_notFound` | User absent → 404 Not Found |
| `update_success` | Modification → BDD + Kafka |
| `delete_success` | Soft delete → Keycloak supprimé + Kafka |
| `delete_notFound` | User absent → 404 Not Found |

---

## 👥 Membres de l'Équipe

| Membre | Couche | Branche |
|---|---|---|
| M1 | Keycloak — Realm, clients, rôles | `feature/member-1-keycloak` |
| M2 | Infrastructure — Docker, PostgreSQL, Redis, Kafka | `feature/member-2-infra` |
| M3 | Security — Spring Boot, JWT, SecurityConfig | `feature/member-3-security` |
| M4 | Domain — Entités, CRUD, Redis cache | `feature/member-4-domain` |
| **M5 — Aziz Benamor** | **Kafka Events — Producer, Consumer, DLQ** | `feature/member-5-kafka` |
| M6 | Frontend — Angular, Keycloak auth | `feature/member-6-frontend` |
| M7 | Tests & Documentation — JUnit, Swagger | `feature/member-7-tests` |

---

## 📄 Licence

Projet académique — PICloud © 2026
