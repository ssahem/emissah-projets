# billetterie-api

API REST de billetterie, organisée par couches selon `../../bookstore-par-couches/CONVENTIONS.md`.
L'énoncé est prioritaire sur les conventions : l'API n'impose donc aucune authentification. Le
`client` est un simple contact enregistré par l'exploitant, pas un compte utilisateur.

Les URL ont pour racine `/billetterie`, avec la ressource au singulier et l'action dans le chemin :
`GET /billetterie/salle/list`, `POST /billetterie/salle/create`,
`PUT /billetterie/salle/update/{id}`, `DELETE /billetterie/salle/delete/{id}`. Ce choix écarte le
préfixe `/api/v1` prévu par la convention §8, ainsi que les URL initialement écrites dans l'énoncé
(`/salles`, `/evenements`, …) : il est consigné dans le plan d'implémentation (§1.2).

## Lancer en développement

La base PostgreSQL locale `billetterie` doit être accessible avec `root` / `root`.

```bash
./mvnw spring-boot:run
```

Flyway reconstruit le schéma puis charge les données de démonstration `db/dev/V7__demo.sql`
(2 salles, 18 places, 3 événements dont un passé, 3 clients,
4 réservations `PROVISOIRE`/`PAYEE`/`ANNULEE`). Pour un schéma seul, sans démo, utiliser le profil
`prod` (`SPRING_PROFILES_ACTIVE=prod`).

Swagger UI : <http://localhost:8081/swagger-ui.html>

```bash
curl -i http://localhost:8081/billetterie/salle/list
```

Une collection Postman prête à importer est fournie dans
`postman/Billetterie-API.postman_collection.json` : 33 requêtes couvrant les 27 endpoints
implémentés (dont les variantes de recherche par email et par période), avec des corps JSON et des
paramètres d’exemple. Elle est déclarée en `auth: noauth` : aucun jeton ni identifiant n’est
nécessaire. Les scripts des `POST` de création renseignent les variables `idSalle`, `idPlace`,
`idEvenement` et `idReservation`. Une copie historique existe aussi dans
`../../docs/01-billetterie/collection/`, hors dépôt.

## Lancer avec Docker

```bash
cp .env.example .env   # puis adapter les valeurs
docker compose up --build
```

La base n'expose aucun port : seule l'API est joignable depuis l'hôte. Le service `app` démarre avec
le profil `dev` : au premier `up`, Flyway reconstruit le schéma (`db/migration`, V1 à V6) puis charge
les données de démonstration (`db/dev/V7__demo.sql`). Swagger UI reste actif.

## Tester

```bash
./mvnw verify
```

La suite compte 47 tests (unitaires, web et intégration) et joue les migrations V1 à V6.
Les tests d'intégration démarrent un PostgreSQL via Testcontainers (Docker requis).

## Organisation du code

Le code reste organisé par couches techniques, conformément aux conventions Bookstore. Deux
sous-packages facilitent la navigation :

- `dto/request/` : corps JSON reçus par les contrôleurs (`Create*Request`, `Update*Request`) ;
- `dto/response/` : réponses renvoyées par l'API (`*Response`, `PageResponse`) ;
- `enums/` : ensembles Java fermés (`TypeSpectacle`). Les statuts et les catégories restent des
  tables de référence PostgreSQL, pas des enums Java.

```
src/main/java/com/billetterie/billetterie_api/
├── controller/    ← HTTP
├── dto/
│   ├── request/   ← entrées
│   └── response/  ← sorties
├── enums/         ← TypeSpectacle
├── entity/        ← entités JPA
├── mapper/        ← conversions Entité ↔ DTO
├── repository/    ← Spring Data JPA
├── service/       ← règles métier et transactions
├── exception/     ← erreurs métier et Problem Details
└── config/        ← OpenAPI, horloge
```

L'arborescence des tests reproduit celle des sources. Le plan d'implémentation décrit chaque
couche plus en détail.

## Fonctionnalités

Le plan d'implémentation et le registre des écarts sont dans `../../docs/01-billetterie/` (hors dépôt).
Les 19 fonctionnalités de l'énoncé sont implémentées, y compris F2 (CRUD des places), F8
(recherche par période), F15 (annulation avec remboursement) et F18 (taux de remplissage).
