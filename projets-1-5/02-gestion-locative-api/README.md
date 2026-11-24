# Gestion locative — API REST

Projet 2 de la formation. API Spring Boot en architecture en couches pour gérer propriétaires,
immeubles, appartements, locataires et baux.

> **Aucune authentification** : l'énoncé ne définit ni utilisateur, ni connexion, ni rôle. Il n'y a
> donc ni `SecurityConfig`, ni jeton, ni en-tête d'authentification. Toutes les requêtes ci-dessous
> s'exécutent directement.

## Pile

Java 21 · Spring Boot 4.1.2 (WebMVC, Data JPA, Validation, Actuator) · PostgreSQL 16 · Flyway ·
springdoc OpenAPI · Testcontainers.

## Démarrer

```bash
cp .env.example .env          # adapter DB_PASSWORD
docker compose up --build
```

L'API écoute sur `http://localhost:8082`. Swagger UI : `http://localhost:8082/swagger-ui.html`.
La base n'expose aucun port hors du réseau Compose.

Le service `app` démarre avec le profil `dev` : au premier `up`, Flyway reconstruit le schéma
(`db/migration`, V1 à V8) puis charge les données de démonstration (`db/dev/V9__demo.sql`).

En local sans Docker, la base `gestion_locative` est attendue sur `localhost:5432` (`root` / `root`
par défaut, surchargeable via `DB_URL`, `DB_USER`, `DB_PASSWORD`) :

```bash
./mvnw spring-boot:run
```

Le profil `dev` ajoute les données de démonstration `db/dev/V9__demo.sql` (2 propriétaires,
3 immeubles, 8 appartements, 5 locataires, 5 baux, 8 révisions de loyer, 4 quittances et un état
des lieux de sortie) :

```bash
SPRING_PROFILES_ACTIVE=dev ./mvnw spring-boot:run
```

Une collection Postman prête à importer est fournie dans
`postman/Gestion-Locative-API.postman_collection.json` : 38 requêtes couvrant les 35 endpoints
implémentés, avec des corps JSON et des paramètres d’exemple. Elle est déclarée en
`auth: noauth` : aucun jeton ni identifiant n’est nécessaire. Les scripts des `POST` de création
renseignent les variables `idProprietaire`, `idImmeuble`, `idAppartement`, `idLocataire` et
`idBail`.

## Préfixe et convention d'URL

Toutes les routes commencent par `/gestion-locative`. Les ressources sont au **singulier** et
l'action figure dans le chemin, comme demandé par l'énoncé :

```
/gestion-locative/proprietaire/create
/gestion-locative/proprietaire/list
/gestion-locative/proprietaire/delete/{id}
```

Exemples sans identifiants :

```bash
curl -i http://localhost:8082/gestion-locative/proprietaire/list
curl -i -X POST http://localhost:8082/gestion-locative/proprietaire/create \
  -H 'Content-Type: application/json' \
  -d '{"nom":"Diallo","prenom":"Awa","email":"awa@example.test","telephone":"0102030405"}'
```

Création : `201` avec en-tête `Location`. Lecture et mise à jour : `200`. Suppression : `204`.
Erreurs : Problem Details (RFC 9457), codes `400`, `404`, `409`, `500` générique.

## Fonctionnalités livrées — P1 à P3 (F1–F16)

Cinq ressources exposent le même jeu de routes : `list`, `get/{id}`, `create`, `update/{id}`,
`delete/{id}` :

- `proprietaire` (F1) — email unique, tri par nom, suppression refusée s'il possède des immeubles ;
- `immeuble` (F2) — `nbEtages > 0`, propriétaire existant, tri par adresse avec nombre
  d'appartements, réduction des étages refusée sous l'étage maximal occupé ;
- `appartement` (F3) — `numero` unique par immeuble, `etage <= nbEtages`, tri par étage puis
  surface, immeuble figé en modification, suppression refusée s'il existe un bail ;
- `locataire` (F4) — email unique, tri par nom, suppression refusée si un bail `EN_COURS` le
  référencie ;
- `bail` (F5) — locataire principal unique exposé (E-08), loyer positif, période libre (F11),
  durée d'au moins un mois (F12), statut initial `EN_COURS`, tri par date de début décroissante.

### Consultation et recherche — P2 (F6–F10)

- **F6** — `GET /gestion-locative/immeuble/appartements/{id}` : appartements triés par étage avec
  `surface`, `occupe` et `statutOccupation` (`LOUE`, `A_VENIR`, `LIBRE`).
- **F7** — `GET /gestion-locative/proprietaire/immeubles/{id}` : immeubles avec `nbAppartements` et
  `nbBauxEnCours`.
- **F8** — `GET /gestion-locative/appartement/baux/{id}` : baux du plus récent au plus ancien,
  locataire principal, dates, loyer courant, `loyersPasses` et `enCours`.
- **F9** — `GET /gestion-locative/appartement/list?surfaceMin=50&surfaceMax=90&pieces=3` : critères
  optionnels et combinables ; bornes inversées → `400`.
- **F10** — `GET /gestion-locative/bail/list?page=0&size=20&sort=dateDebut,desc` : `PageResponse`
  avec `content`, `page`, `size`, `totalElements`, `totalPages`.

Par défaut, `bail/list` renvoie 20 éléments triés par date de début décroissante ; toute taille
supérieure à 100 est ramenée à 100. Un identifiant inconnu sur F6, F7 ou F8 renvoie `404`, et une
liste vide est renvoyée quand la ressource existe sans enfant.

Le corps de création d'un bail accepte `provisionCharges` et `depotGarantie` : absents, ils valent
`0`. La révision initiale de loyer est créée dans la même transaction (V6). Avant la date de début,
loyer et date de fin sont modifiables ; après, seule la date de fin l'est et le loyer suit la
procédure de révision (F15). La suppression n'est autorisée que pour un bail non commencé et
nettoie ses liaisons `bail_locataire` et `revision_loyer`.

Les corps JSON sont validés avant le service (`@Valid`) : par exemple
`POST /gestion-locative/bail/create` attend `idAppartement`, `idLocataire`, `dateDebut` et
`loyerMensuel`, avec `dateFin` facultative.

### Règles métier et cycle de vie — P3 (F11–F16)

- **F11** — aucun bail ne chevauche la période demandée, sous `@Lock(PESSIMISTIC_WRITE)` ; refus
  `409` citant le bail bloquant et ses dates. La contrainte SQL `ex_bail_pas_de_chevauchement`
  reste le filet final.
- **F12** — `dateFin` doit être strictement postérieure au début et au moins un mois après, à la
  création comme à la modification ; refus `400`. Une fin nulle reste autorisée (bail sans terme).
- **F13** — `POST /gestion-locative/bail/resiliation/{id}` avec `{"dateEffet":"2026-09-30"}` :
  effet dans la période et non antérieur à aujourd'hui ; la fin est ramenée à cette date, le statut
  passe `RESILIE` et l'appartement redevient louable dès cette date. `409` si le bail est déjà
  résilié, terminé ou non commencé.
- **F14** — `POST /gestion-locative/bail/renouvellement/{id}` avec `{"dateFin":"2028-06-30"}` :
  échéance strictement postérieure et période ajoutée libre ; l'ancienne fin est conservée dans
  `historique_bail` (motif `RENOUVELLEMENT`). Un bail `TERMINE` repasse `EN_COURS` ; un bail
  `RESILIE` est refusé en `409`.
- **F15** — `POST /gestion-locative/bail/revision/{id}` avec
  `{"montant":920.00,"dateEffet":"2027-01-01"}` : effet postérieur à la dernière révision et
  compris dans la période. Le nouveau montant devient le loyer courant, l'ancien reste consultable
  dans `revision_loyer` via F8. Montant non positif → `400`, date déjà utilisée → `409`.
- **F16** — tâche planifiée chaque nuit à 01:00 UTC (`@Scheduled`, `@EnableScheduling`) : les baux
  `EN_COURS` dont la fin est antérieure à aujourd'hui passent `TERMINE`, avec `date_cloture`, une
  ligne `historique_bail` motif `CLOTURE` et un log mentionnant l'identifiant du bail. Aucune
  erreur HTTP.

Les réponses de ces trois actions exposent `statut`, `dateResiliation` et `dateCloture`. Toutes les
transitions et révisions sont journalisées dans `historique_bail` (V5) et `revision_loyer` (V6),
dans la même transaction que la modification du bail.

- **F17** — `GET /gestion-locative/appartement/libres?date=2026-12-01` : logements non couverts par
  un bail `EN_COURS` à cette date, groupés par immeuble, avec surface et dernier loyer connu issu de
  `revision_loyer` (`null` si le bien n'a jamais été loué). Sans paramètre, la date du jour est
  utilisée ; un format invalide donne `400`.
- **F18** — `GET /gestion-locative/appartement/occupation/{id}` : périodes `LOUE`/`VACANT`
  ordonnées, locataire principal (E-08) et loyer applicable reconstitué palier par palier depuis
  `revision_loyer`. Les bornes sont inclusives, `fin: null` marque une période ouverte. Un logement
  jamais loué est vacant depuis sa date de création (V8). Appartement inconnu → `404`.
- **F19** — `GET /gestion-locative/immeuble/revenu/{id}?mois=2026-11` : somme des loyers des baux
  actifs au prorata journalier, détail par appartement et taux d'occupation en pourcentage. Un
  immeuble entièrement vacant renvoie `0` et `0 %`. Mois mal formé → `400`, immeuble inconnu →
  `404`.

## Schéma et migrations

Flyway gère huit migrations :

- `V1` — baseline : 13 tables, 2 vues, contraintes nommées et `ex_bail_pas_de_chevauchement` ;
- `V2` — `immeuble.id_proprietaire` (E-01) ;
- `V3` — `immeuble.nb_etages` (E-02) ;
- `V4` — `bail.statut`, `date_resiliation`, `date_cloture` (E-04) ;
- `V5` — `historique_bail` (E-05) ;
- `V6` — `revision_loyer` (E-06) ;
- `V7` — `SET NOT NULL` sur les deux téléphones (E-07) ;
- `V8` — `appartement.date_creation`, borne initiale de la vacance F18.

La démonstration porte la version `V9` et n'est chargée que par le profil `dev` (location
`classpath:db/dev`). Elle ne doit pas être numérotée `V8` : Flyway refuserait deux migrations
de même version, l'une dans `db/migration` et l'autre dans `db/dev`.

Sur une base déjà existante, `baseline-on-migrate: true` et `baseline-version: 1` marquent `V1`
comme appliquée : `V1` ne s'exécute que sur une base vierge. `V1` n'est jamais modifiée.

Les vues `v_solde_quittance` et `v_restitution_depot` sont lues via `JdbcTemplate`, sans entité JPA.

## Arbitrages documentés

- **E-07** — les colonnes réellement nullables et exigées par l'énoncé sont les deux `telephone` ;
  `V7` les rend obligatoires.
- **E-08** — `bail_locataire` garde sa clé primaire composite pour une éventuelle colocation, mais
  l'API expose un seul `idLocataire` à la création et le « locataire principal » en réponse.
- **E-09** — la contrainte d'exclusion `ex_bail_pas_de_chevauchement` est reproduite en `V1`. Le
  service garde un contrôle applicatif sous transaction, avec `@Lock(PESSIMISTIC_WRITE)`, pour
  renvoyer un `409` lisible citant le bail bloquant au lieu d'une erreur SQL brute ; la contrainte
  reste le filet final en cas de concurrence.
- **E-10** — `appartement.date_creation` ajouté en V8 : F18 peut dater la vacance d'un logement
  jamais loué depuis sa mise en service, sans modifier le baseline V1.

## Tests

```bash
./mvnw verify
```

Les tests d'intégration démarrent un PostgreSQL Testcontainers et appliquent les migrations depuis
une base vierge. Les tests temporels utilisent un bean `Clock` fixe `@Primary`. La suite couvre
78 tests : 4 de socle, 37 unitaires de service, 17 web et 20 d'intégration (CRUD, consultation,
cycle de vie, restitution et revenus). Les contrôleurs sont testés sans base via `@WebMvcTest`, les
règles métier via Mockito et les parcours complets via MockMvc sur la base Testcontainers.

## Références

Les conventions et checklists appliquées se trouvent dans `docs/helper/CONVENTIONS.md` et
`docs/helper/01-helper-controller.md` à `08-helper-spring-security.md` (hors dépôt). Le plan
d'implémentation et le registre des écarts sont dans `docs/02-gestion-locative/`.
