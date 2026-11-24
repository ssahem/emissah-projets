# Projets 1 à 5 — API REST Spring Boot

Cinq API REST autonomes, une par projet, chacune organisée en couches (controller, service,
repository, entity, dto, mapper, exception). Aucune n'impose d'authentification : l'énoncé ne
définit ni utilisateur ni rôle.

| Projet | Dossier | Port | Racine des URL | Collection Postman |
| --- | --- | --- | --- | --- |
| 1 — Billetterie | `01-billetterie-api/` | 8081 | `/billetterie` | `postman/Billetterie-API.postman_collection.json` |
| 2 — Gestion locative | `02-gestion-locative-api/` | 8082 | `/gestion-locative` | `postman/Gestion-Locative-API.postman_collection.json` |
| 3 — Interventions terrain | `03-interventions-terrain-api/` | 8083 | `/interventions-terrain` | `postman/Interventions-Terrain-API.postman_collection.json` |
| 4 — Suivi scolaire | `04-suivi-scolaire-api/` | 8084 | `/suivi-scolaire` | `postman/Suivi-Scolaire-API.postman_collection.json` |
| 5 — Flotte de véhicules | `05-flotte-vehicules-api/` | 8085 | `/flotte-vehicules` | `postman/Flotte-Vehicules-API.postman_collection.json` |

Chaque dossier contient son propre `README.md` détaillé (fonctionnalités F1 à F19, règles métier,
migrations, arbitrages et bilan des tests).

## Lancer un projet (recommandé pour tester)

Prérequis : Docker. Aucun PostgreSQL local n'est nécessaire, aucune installation Maven non plus.

```bash
cd 01-billetterie-api          # idem pour 02-… à 05-…
cp .env.example .env           # adapter DB_PASSWORD si besoin
docker compose up --build
```

Au premier `up`, **Flyway reconstruit automatiquement le schéma** (migrations `db/migration/`)
puis **charge les données de démonstration** (`db/dev/`). L'API est alors prête à tester :

- Swagger UI : `http://localhost:<port>/swagger-ui.html`
- Santé : `http://localhost:<port>/actuator/health/readiness`

La base PostgreSQL n'expose aucun port hors du réseau Compose : seule l'API est joignable depuis
l'hôte.

## Tester avec Postman

Dans chaque dossier, importer la collection `postman/*.postman_collection.json`. Les collections
sont déclarées en `auth: noauth` (aucun jeton) et leurs variables `host` / `baseUrl` pointent déjà
sur le bon port. Les scripts des `POST` de création mémorisent les identifiants dans les variables
de collection : exécuter les créations dans l'ordre avant les consultations.

Les valeurs par défaut des collections correspondent aux données de démonstration chargées
automatiquement.

## Lancer sans Docker (développement local)

Un PostgreSQL local est attendu sur `localhost:5432` (utilisateur `root`, mot de passe `root`,
surchargeable via `DB_URL`, `DB_USER`, `DB_PASSWORD`) :

```bash
cd 01-billetterie-api
./mvnw spring-boot:run
```

Le schéma et les données de démonstration sont chargés automatiquement. Pour un schéma seul, sans
démo, utiliser `SPRING_PROFILES_ACTIVE=prod`.

## Exécuter les tests

Chaque projet contient sa propre suite (unitaires, web, intégration Testcontainers) :

```bash
cd 01-billetterie-api
./mvnw -q verify
```

Les tests d'intégration démarrent un PostgreSQL via Testcontainers : Docker est requis.
