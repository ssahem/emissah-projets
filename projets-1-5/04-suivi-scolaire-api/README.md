# Suivi scolaire — API REST

Projet 4 de la formation. API Spring Boot en architecture en couches pour gérer classes, élèves,
matières, professeurs, enseignements, notes, trimestres et le suivi des résultats scolaires.

> **Aucune authentification** : l'énoncé ne définit ni utilisateur, ni connexion, ni rôle. Il n'y a
> donc ni `SecurityConfig`, ni jeton, ni code `401`/`403`. Toutes les requêtes s'exécutent directement.
> En conséquence, l'auteur d'une réouverture de trimestre est un **texte libre fourni par l'appelant**
> dans le corps de la requête (arbitrage A-04).

> **Priorité des sources** — l'énoncé (F1 à F19) est prioritaire sur le schéma PostgreSQL fourni,
> lui-même prioritaire sur les conventions de forme. Les écarts et les dix-neuf arbitrages sont
> consignés dans `docs/04-suivi-scolaire/audit/` hors de ce dépôt.

## Pile

Java 21 · Spring Boot 4.1.2 (WebMVC, Data JPA, Validation, Actuator) · PostgreSQL 16 · Flyway ·
springdoc OpenAPI · JdbcTemplate pour les vues · Testcontainers · Mockito · JUnit 5.

## Démarrer

### Avec Docker

```bash
cp .env.example .env          # adapter DB_PASSWORD
docker compose up --build
```

L'API écoute sur `http://localhost:8084`. Swagger UI :
`http://localhost:8084/swagger-ui.html`. La base n'expose aucun port hors du réseau Compose.

Le service `app` démarre avec le profil `dev` : au premier `up`, Flyway reconstruit le schéma
(`db/migration`, V1 à V3) puis charge les données de démonstration (`db/dev/V9__demo.sql`), et la
documentation OpenAPI reste active. Le service `db` attend un `DB_PASSWORD` non vide.

### En local sans Docker

La base `suivi_scolaire` est attendue sur `localhost:5432` (`root` / `root` par défaut,
surchargeable via `DB_URL`, `DB_USER`, `DB_PASSWORD`) :

```bash
./mvnw spring-boot:run
```

Flyway reconstruit le schéma puis charge les données de démonstration `db/dev/V9__demo.sql`.
Pour un schéma seul, sans démo, utiliser le profil `prod` (`SPRING_PROFILES_ACTIVE=prod`).

Le profil `dev` ajoute le script `db/dev/V9__demo.sql` aux emplacements Flyway. Il fournit deux
années scolaires, deux niveaux, trois classes, six élèves, quatre matières, trois professeurs,
des enseignements, trois trimestres dont un clôturé, trois types d'évaluation avec leurs poids,
quinze notes, une alerte et une réouverture journalisée. Les identifiants sont fixés explicitement
pour que la collection Postman et les tests d'intégration puissent les référencer.

La configuration est surchargeable via `DB_URL`, `DB_USER`, `DB_PASSWORD`,
`SPRING_PROFILES_ACTIVE` et `FLYWAY_BASELINE_ON_MIGRATE`. La tâche F16 est exécutée chaque lundi à
1 h UTC via `suivi-scolaire.alertes-difficulte.cron` (valeur par défaut `0 0 1 * * MON`).

## Préfixe et convention d'URL

Toutes les routes commencent par `/suivi-scolaire`. Les ressources sont au **singulier** et
l'action figure dans le chemin, comme demandé par l'énoncé :

```
/suivi-scolaire/classe/create
/suivi-scolaire/classe/list
/suivi-scolaire/note/create
```

Création : `201` avec en-tête `Location`. Lecture et mise à jour : `200`. Suppression : `204`.
Erreurs : Problem Details (RFC 9457), codes `400`, `404`, `409` et `500` générique. Les types de
problèmes utilisent la base `https://api.suivi-scolaire.local/problems/`. Aucune réponse ne porte
`401` ni `403`.

## Niveau 1 — CRUD (F1 à F5)

Les cinq ressources suivent la même convention, préfixée par `/suivi-scolaire` :

- **Classe** — `/classe/create`, `/classe/list`, `/classe/get/{id}`, `/classe/update/{id}`, `/classe/delete/{id}` (F1).
- **Élève** — `/eleve/create`, `/eleve/list`, `/eleve/get/{id}`, `/eleve/update/{id}`, `/eleve/delete/{id}` (F2).
- **Matière** — `/matiere/create`, `/matiere/list`, `/matiere/get/{id}`, `/matiere/update/{id}`, `/matiere/delete/{id}` (F3).
- **Professeur** — `/professeur/create`, `/professeur/list`, `/professeur/get/{id}`, `/professeur/update/{id}`, `/professeur/delete/{id}` (F4).
- **Note** — `/note/create`, `/note/list`, `/note/get/{id}`, `/note/update/{id}`, `/note/delete/{id}` (F5).

Points de validation principaux :

- **F1** — nom, niveau et effectif maximal obligatoires ; effectif positif ; nom unique par niveau ;
  l'effectif maximal ne peut pas passer sous l'effectif actuel ; suppression refusée si la classe a
  des élèves.
- **F2** — nom, prénom, date de naissance et classe obligatoires ; date dans le passé ; classe non
  pleine ; date de naissance figée en modification ; suppression refusée si l'élève a des notes.
- **F3** — libellé unique, coefficient strictement positif ; coefficient modifiable tant qu'aucun
  trimestre n'est clôturé ; suppression refusée si la matière a des notes ou un professeur affecté.
- **F4** — nom, email et matière principale obligatoires ; email unique ; suppression refusée si le
  professeur est affecté à une classe. Les matières exposées sont déduites de `enseignement` (A-02).
- **F5** — valeur comprise entre 0 et 20 ; trimestre entre 1 et 3 et non clôturé ; élève et matière
  figés en modification ; suppression possible seulement si le trimestre est ouvert.

## Niveau 2 — Consultations (F6 à F10)

- `GET /suivi-scolaire/classe/eleves/{id}` — élèves triés par nom puis prénom, effectif actuel et
  effectif maximal (F6).
- `GET /suivi-scolaire/eleve/notes/{id}?trimestre=1` — notes groupées par matière ; sans trimestre,
  toutes les notes sont renvoyées ; un trimestre hors de `1..3` donne `400` (F7).
- `GET /suivi-scolaire/classe/notes/{id}?matiere={id}&trimestre=2` — notes groupées par élève et
  moyenne de classe ; une matière non enseignée dans la classe donne `400` (F8).
- `GET /suivi-scolaire/eleve/list?nom=kouassi` — recherche partielle insensible à la casse sur le
  nom, avec la classe de chaque élève. Le paramètre est optionnel : absent, la route CRUD renvoie la
  liste complète ; présent mais vide, la réponse est `400` (F9).
- `GET /suivi-scolaire/note/list?page=0&size=30&sort=valeur,desc` — page de notes avec
  `totalElements` et `totalPages` ; `size` est plafonné à 100 (F10). Sans aucun paramètre de
  pagination, la même URL renvoie la liste groupée par matière de F5 : cette réponse conserve le
  contrat de consultation du niveau 1 et évite de dupliquer une route.

## Niveau 3 — Règles métier (F11 à F16)

- `POST /suivi-scolaire/eleve/affectation/{id}` avec `idClasseDestination` — clôture l'ancienne
  inscription et en crée une nouvelle ; refus `409` si la classe est pleine, `400` si le niveau
  diffère. Les notes restent rattachées à l'élève (F11).
- `POST /suivi-scolaire/professeur/affectations/{id}` avec `idMatiere` et `idClasse` — une seule
  matière par professeur et par classe ; le conflit `409` cite le professeur déjà affecté (F12).
- `PUT /suivi-scolaire/type-evaluation/poids/{code}` avec `poids` — poids strictement positif ;
  `409` dès qu'un trimestre est clôturé (F13). La table `type_evaluation` du schéma fourni n'a pas
  de colonne `code` : `{code}` est résolu successivement par les alias `DEV`, `INTERRO`, `ORAL`, par
  le libellé insensible à la casse, puis par l'identifiant numérique.
- `POST /suivi-scolaire/trimestre/{numero}/cloture` — exige au moins une note pour chaque
  enseignement de chaque classe ; le `409` liste les classes et matières manquantes. Après clôture,
  les notes du trimestre ne sont plus créables, modifiables ni supprimables (F14).
- `POST /suivi-scolaire/trimestre/{numero}/reouverture` avec `motif` et `auteur` — motif d'au moins
  vingt caractères, seul le dernier trimestre clôturé peut être rouvert ; date, motif et auteur sont
  journalisés (F15).
- F16 est une tâche planifiée hebdomadaire sans endpoint de déclenchement : elle signale les élèves
  dont la moyenne générale du trimestre en cours est inférieure à 8, ou qui ont chuté de plus de
  quatre points depuis le trimestre précédent. Un élève n'est signalé qu'une fois par trimestre.
  L'endpoint facultatif `GET /suivi-scolaire/alerte/list?trimestre=2` permet de consulter les alertes
  persistées.

## Niveau 4 — Restitution (F17 à F19)

- `GET /suivi-scolaire/eleve/moyennes/{id}?trimestre=1` — moyenne pondérée par le poids du type
  d'évaluation pour chaque matière enseignée dans la classe, avec le nombre de notes de l'élève ;
  une matière sans note est signalée par `evaluee: false` et une moyenne absente (F17).
- `GET /suivi-scolaire/eleve/moyenne-generale/{id}?trimestre=1` — moyenne pondérée par les
  coefficients des matières, avec le détail contribution par contribution ; les matières non
  évaluées sont exclues et une absence totale de notes donne une moyenne absente, jamais zéro (F18).
- `GET /suivi-scolaire/classe/classement/{id}?trimestre=1` — rangs densifiés (les ex æquo partagent
  le même rang), moyenne de classe, meilleure et plus faible moyenne ; les élèves sans note sont en
  fin de liste, sans rang (F19).

Les moyennes et les rangs sont lus exclusivement dans les vues SQL via `JdbcTemplate` (`VueRepository`).
Aucune vue n'est mappée comme entité JPA et aucune écriture n'y est effectuée.

## Collection Postman

`postman/Suivi-Scolaire-API.postman_collection.json` regroupe 41 requêtes en huit groupes : socle,
classes, élèves, matières, professeurs, notes, règles métier et alertes. Importez le fichier dans
Postman, ajustez les variables `host` et `baseUrl` si l'API ne tourne pas sur `localhost:8084`, puis
exécutez les requêtes `POST` de création dans l'ordre : leurs scripts mémorisent les identifiants
dans les variables de collection (`idClasse`, `idEleve`, `idMatiere`, `idProfesseur`,
`idEnseignement`, `idNote`). Les valeurs par défaut correspondent aux données de démo.

La collection est déclarée en `auth: noauth` : aucun jeton, aucun identifiant et aucun en-tête
d'autorisation n'est nécessaire ni configuré. Chaque description indique le code attendu et les cas
d'erreur (`400`, `404`, `409`). F16 étant une tâche planifiée, seule sa consultation HTTP est
présente dans la collection.

## Migrations et schéma

La chaîne Flyway versionnée s'arrête à **V3** :

- **V1** — baseline exact du schéma fourni : 19 tables avec `GENERATED BY DEFAULT AS IDENTITY`,
  7 vues reconstruites, clés primaires, clés étrangères, contraintes `UNIQUE` et `CHECK`. Cette
  migration ne doit **jamais** être modifiée. Les trois colonnes `smallint` du schéma
  (`trimestre.numero`, `note.numero_trimestre`, `bulletin.numero_trimestre`) sont conservées telles
  quelles ; les entités correspondantes déclarent `@JdbcTypeCode(SqlTypes.SMALLINT)` pour que la
  validation Hibernate accepte le type sans altérer le baseline.
- **V2** — ajoute uniquement `classe.effectif_max`, `UNIQUE (classe.nom, classe.id_niveau)`,
  `inscription.date_fin`, `matiere.coefficient`, `professeur.email`, `professeur.telephone`,
  `trimestre.est_cloture` et `trimestre.date_cloture`. La vue `v_moyenne_generale` y est recréée
  pour utiliser le coefficient global de la matière au lieu de la table `coefficient` par niveau.
- **V3** — crée uniquement `historique_reouverture_trimestre` et `alerte_difficulte`, avec une
  seule clé étrangère chacune, respectivement vers `trimestre` et `inscription`.
- **V9** — jeu de démonstration, chargé uniquement par le profil `dev` depuis `db/dev`.

Sur une base existante, Flyway pose le baseline en `baseline-on-migrate: true` ; en production, le
baseline est explicite via `FLYWAY_BASELINE_ON_MIGRATE=true`.

### Arbitrages A-01 à A-05

- **A-01** — aucune clé étrangère n'est ajoutée de `note` vers `inscription`.
  `inscription.numero_inscription` possède déjà une contrainte d'unicité dans le schéma fourni : le
  lien est résolu en service par `numero_inscription`, avec une erreur explicite si la résolution
  échoue.
- **A-02** — aucune colonne `professeur.id_matiere_principale`. Un professeur peut enseigner
  plusieurs matières : elles sont retrouvées par `SELECT DISTINCT ... FROM enseignement`.
- **A-03** — `note.numero_trimestre` est conservé sans clé étrangère. L'existence du trimestre et
  son état de clôture sont validés en service ; le numéro est limité à `1..3`.
- **A-04** — `historique_reouverture_trimestre` porte une seule clé étrangère vers `trimestre`.
  Le motif fait au moins vingt caractères, seul le dernier trimestre clôturé peut être rouvert et
  l'auteur est fourni par l'appelant faute d'authentification.
- **A-05** — `alerte_difficulte` porte une seule clé étrangère vers `inscription`, avec
  `numero_trimestre` en `INTEGER` et une contrainte d'unicité `(id_inscription, numero_trimestre)`.
  Aucune clé étrangère vers `trimestre`, `classe` ou `eleve` n'est introduite.

## État des jalons

- [x] **P0 — Socle** : dépendances, configuration, migrations V1–V3, données de démo `db/dev/V9`,
      `Clock` injecté, Actuator, OpenAPI, Docker.
- [x] **P1 — CRUD (F1–F5)** : classes, élèves, matières, professeurs et notes.
- [x] **P2 — Consultations (F6–F10)** : effectif, notes par élève et par classe, recherche, pagination.
- [x] **P3 — Règles métier (F11–F16)** : affectations, poids, clôture, réouverture tracée, alertes.
- [x] **P4 — Restitution (F17–F19)** : moyennes par matière, moyenne générale, classement.
- [x] **P5 — Finalisation** : README complet, collection Postman sans authentification, bilan des
      tests, `./mvnw -q verify` vert.

## Vérification

Commande retenue :

```bash
cd projets-1-5/04-suivi-scolaire-api && ./mvnw -q verify
```

Bilan final : **109 tests**, 0 échec, 0 erreur, 0 ignoré.

- 69 tests de service (Mockito) : CRUD, consultations, règles métier, alertes et restitution.
- 20 tests web (`@WebMvcTest` avec `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
  et `JsonMapper`) : codes HTTP, paramètres, sérialisation et Problem Details.
- 20 tests d'intégration (MockMvc + Testcontainers sur `postgres:16-alpine`, `@DynamicPropertySource`)
  : migrations, schéma final, vues, parcours CRUD, règles métier, alertes et restitution.

Aucun test ne dépend de l'heure réelle : les tests temporels injectent un `Clock` fixe via une
configuration dédiée. Les tests d'intégration reconstruisent leur jeu de données avant chaque cas et
alignent les séquences d'identité après les insertions à identifiant explicite.
