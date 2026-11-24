# Interventions terrain — API REST

Projet 3 de la formation. API Spring Boot en architecture en couches pour gérer agences, équipes,
techniciens, compétences, clients, interventions et le suivi de leur cycle de vie.

> **Aucune authentification** : l'énoncé ne définit ni utilisateur, ni connexion, ni rôle. Il n'y a
> donc ni `SecurityConfig`, ni jeton, ni code `401`/`403`. Toutes les requêtes s'exécutent directement.

## Pile

Java 21 · Spring Boot 4.1.2 (WebMVC, Data JPA, Validation, Actuator) · PostgreSQL 16 · Flyway ·
springdoc OpenAPI · Testcontainers.

## Démarrer

```bash
cp .env.example .env          # adapter DB_PASSWORD
docker compose up --build
```

L'API écoute sur `http://localhost:8083`. Swagger UI : `http://localhost:8083/swagger-ui.html`.
La base n'expose aucun port hors du réseau Compose.

Le service `app` démarre avec le profil `dev` : au premier `up`, Flyway reconstruit le schéma
(`db/migration`, V1 à V7) puis charge les données de démonstration (`db/dev/V8__demo.sql`).

En local sans Docker, la base `interventions_terrain` est attendue sur `localhost:5432`
(`root` / `root` par défaut, surchargeable via `DB_URL`, `DB_USER`, `DB_PASSWORD`) :

```bash
./mvnw spring-boot:run
```

Pour charger les données de démonstration (profil `dev`, jamais en production) :

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Le profil `dev` fournit une agence « Agence Paris Centre », deux techniciens (`MAT001`, `MAT002`),
deux compétences, le type `RACCORDEMENT_FIBRE` avec ses exigences et un client de démonstration.
La configuration est surchargeable via `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`SPRING_PROFILES_ACTIVE` et `FLYWAY_BASELINE_ON_MIGRATE` ; la tâche F16 utilise
`interventions-terrain.alertes-competences.cron` (par défaut `0 0 1 * * *`).

## Préfixe et convention d'URL

Toutes les routes commencent par `/interventions-terrain`. Les ressources sont au **singulier** et
l'action figure dans le chemin, comme demandé par l'énoncé :

```
/interventions-terrain/agence/create
/interventions-terrain/agence/list
/interventions-terrain/intervention/create
```

Création : `201` avec en-tête `Location`. Lecture et mise à jour : `200`. Suppression : `204`.
Erreurs : Problem Details (RFC 9457), codes `400`, `404`, `409`, `500` générique.

### Routes P1

- **Agence** — `/agence/create`, `/agence/list`, `/agence/get/{id}`, `/agence/update/{id}`, `/agence/delete/{id}`.
- **Technicien** — `/technicien/create`, `/technicien/list`, `/technicien/get/{id}`, `/technicien/update/{id}`, `/technicien/delete/{id}`.
- **Compétence** — `/competence/create`, `/competence/list`, `/competence/get/{id}`, `/competence/update/{id}`, `/competence/delete/{id}`.
- **Client** — `/client/create`, `/client/list`, `/client/get/{id}`, `/client/update/{id}`, `/client/delete/{id}`.
- **Intervention** — `/intervention/create`, `/intervention/list`, `/intervention/get/{id}`, `/intervention/update/{id}`, `/intervention/delete/{id}`.

Toutes ces routes sont préfixées par `/interventions-terrain`. L'affectation multiple (A-02) utilise
`POST /interventions-terrain/intervention/affectation/{id}`,
`GET /interventions-terrain/intervention/affectation/{id}` et
`DELETE /interventions-terrain/intervention/affectation/{id}/{idTechnicien}`. Le corps d'ajout accepte
`estResponsable` (faux par défaut) : promouvoir un second responsable renvoie `409`. Le remplacement
du responsable se fait via `PUT /intervention/update/{id}`, avec recopie du statut courant (E-12).

Une intervention n'est modifiable ou supprimable que tant que son statut est `PLANIFIEE` (F5). Son
créneau doit être futur et sa fin strictement postérieure à son début ; le type est résolu depuis
`codeType` (400 si inconnu) et l'adresse est créée dans la même transaction depuis `voie`/`commune`.

### Routes P2

- `GET /agence/techniciens/{id}` — effectif trié par nom, avec le nombre de compétences valides (F6).
- `GET /technicien/competences/{id}` — compétences obtenues, expirées signalées (F7).
- `GET /client/interventions/{id}` — historique du plus récent au plus ancien (F8).
- `GET /intervention/list?statut=…&du=…&au=…` — filtres optionnels et combinables ; le statut est
  lu sur le dernier historique de l'affectation responsable via une requête native (F9).
- `GET /technicien/planning/{id}?du=…&au=…` — semaine courante par défaut, trois mois maximum,
  avec l'adresse de l'intervention et le drapeau `annulee` (F10).

### Routes P3

- `POST /technicien/competence/{id}` — certifie une compétence ; dates cohérentes et aucun doublon
  actif, sinon `400` / `409` (F11).
- `POST /intervention/create` et `PUT /intervention/update/{id}` — contrôlent le chevauchement sous
  verrou pessimiste (toutes affectations, ANNULEE exclues) et les compétences exigées par le type,
  avec `409` / `400` détaillées (F12, F13).
- `POST /intervention/statut/{id}` — transitions `PLANIFIEE → EN_COURS → TERMINEE | ECHOUEE` et
  `PLANIFIEE → ANNULEE` ; toute autre transition renvoie `409` avec les statuts atteignables (F14).
- `POST /intervention/reprise/{id}` — crée une intervention `PLANIFIEE` liée à l'origine, qui doit
  être `ECHOUEE` et non encore reprise (F15).
- `AlerteCompetenceService` — tâche planifiée à 01:00 UTC (`interventions-terrain.alertes-competences.cron`)
  qui signale par agence les compétences expirant dans 30 jours et positionne `date_alerte` pour
  éviter les doublons (F16). Aucune table d'alerte n'est créée (E-07).

### Routes P4

- `GET /technicien/disponibles?debut=…&fin=…&type=RACCORDEMENT_FIBRE` — techniciens libres et
  compétents, groupés par agence avec leur charge du jour ; une liste vide porte `raisonFiltrage`
  (F17).
- `GET /agence/charge/{id}?du=…&au=…` — nombre d'interventions et heures occupées par technicien,
  avec un total ; les annulations sont exclues et une agence inconnue renvoie `404` (F18).
- `GET /agence/taux-reussite/{id}?mois=2026-11` — taux global et par type, avec
  `TERMINEE / (TERMINEE + ECHOUEE)` ; mois au format `yyyy-MM`, aucune clôturée → `0` (F19).
  La vue `v_delai_par_agence_mois` mesure les délais, pas la réussite : elle n'est pas utilisée ici.

## Collection Postman

`postman/Interventions-Terrain-API.postman_collection.json` regroupe 45 requêtes par jalon : socle,
CRUD, consultations, règles métier et restitution. Importez le fichier dans Postman, ajustez les
variables `host` et `baseUrl` si l'API ne tourne pas sur `localhost:8083`, puis exécutez les
requêtes `POST` de création dans l'ordre : leurs scripts mémorisent les identifiants dans les
variables de collection (`idAgence`, `idTechnicien`, `idCompetence`, `idClient`, `idIntervention`).
Les valeurs par défaut correspondent aux données de démo (`MAT001`, `MAT002`, `RACCORDEMENT_FIBRE`).

La collection est déclarée en `auth: noauth` : aucun jeton, aucun identifiant et aucun en-tête
d'autorisation n'est nécessaire ni configuré. Les requêtes qui attendent volontairement une erreur
sont signalées dans leur description (chevauchement `409`, compétence manquante `400`). F16 est une
tâche planifiée et n'a donc pas de requête HTTP.

## Migrations et schéma

La chaîne Flyway versionnée s'arrête à **V7** :

- **V1** — baseline : 20 tables avec identités, 6 vues, contraintes et index partiel du schéma source.
- **V2** — `agence.ville`, `agence.responsable`, `uq_agence_nom`.
- **V3** — `technicien.matricule` unique.
- **V4** — `competence.description`.
- **V5** — `client.adresse`, `client.commune`, `client.telephone NOT NULL`.
- **V6** — `obtention_competence.date_alerte`.
- **V7** — seed idempotent des cinq statuts de l'énoncé, dont `ANNULEE`.

`V1` est un baseline : il ne doit **jamais** être modifié. Sur une base existante, Flyway pose le
baseline en `baseline-on-migrate: true` ; en production, le baseline est explicite via
`FLYWAY_BASELINE_ON_MIGRATE=true`.

Décisions structurantes (détails dans `docs/03-interventions-terrain/` hors dépôt) :

- les 19 clés primaires sont des `GENERATED ALWAYS AS IDENTITY` : les entités utilisent
  `@GeneratedValue(strategy = IDENTITY)` ;
- un index partiel `ux_un_responsable_par_intervention` garantit un seul responsable par
  intervention ; les affectations secondaires sont ajoutées via l'endpoint dédié (A-02) ;
- les 6 vues sont lues en `JdbcTemplate`/projections, sans entité JPA ;
- les 8 fonctions et 11 triggers du schéma source ne sont **pas** reproduits : plusieurs étaient
  incompatibles avec l'énoncé (référence à une colonne inexistante, délai de trajet de 30 minutes,
  blocage de `EN_COURS`). Les règles F11 à F15 sont implémentées en service (E-24) ;
- `EN_ROUTE` et `REPORTEE`, présentes dans la base source, restent lisibles mais ne sont pas des
  transitions du cycle de vie F14 (E-22).

## État des jalons

- [x] **P0 — Socle** : dépendances, configuration, migrations V1–V7, données de démo `db/dev/V8`,
      `Clock`, Actuator, OpenAPI, Docker, `SocleIntegrationTest`.
- [x] **P1 — CRUD (F1–F5)** : agences, techniciens, compétences, clients, interventions, affectation multiple.
- [x] **P2 — Consultations (F6–F10)** : effectif d'agence, compétences, historique client, filtres, planning.
- [x] **P3 — Règles métier (F11–F16)** : disponibilité, compétences requises, statuts, reprise,
      alertes de certifications.
- [x] **P4 — Restitution (F17–F19)** : disponibilité et compétences, charge d'agence,
      taux de réussite mensuel.

- [x] **P5 — Finalisation** : README complet, collection Postman sans authentification, bilan des
      tests, `./mvnw -q verify` vert.

## Vérification

Commande retenue :

```bash
cd projets-1-5/03-interventions-terrain-api && ./mvnw -q verify
```

Bilan final : **106 tests**, 0 échec, 0 erreur, 0 ignoré.

- 57 tests de service (Mockito) : F1 à F19, dont les règles métier et la restitution ;
- 25 tests web (`@WebMvcTest`) : codes HTTP, Problem Details, paramètres et sérialisation ;
- 24 tests d'intégration (MockMvc + Testcontainers sur PostgreSQL 16) : socle, données de démo,
  parcours CRUD, filtres, chevauchements, compétences, cycle de vie, reprise, alertes, disponibilité,
  charge et taux mensuel.

Aucun test ne dépend de l'heure réelle : les tests temporels injectent un `Clock` fixe.
