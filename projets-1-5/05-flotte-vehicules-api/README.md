# Flotte de véhicules — API REST

Projet 5 de la formation : une API Spring Boot qui gère les sites, les véhicules, les salariés, les affectations, les entretiens, les alertes et le kilométrage d'une flotte. L'énoncé ne définit aucun utilisateur ni rôle : **l'API n'a donc aucune authentification**, ni jeton, ni code 401 ou 403.

- Langage : Java 21
- Framework : Spring Boot 4.1.2 (WebMVC, JPA, Validation, Actuator)
- Documentation : springdoc OpenAPI 3 (`/v3/api-docs`, `/swagger-ui.html`)
- Persistance : PostgreSQL 16, Flyway, Hibernate en `validate`
- Tests : JUnit 5, Mockito, `@WebMvcTest`, Testcontainers PostgreSQL
- Préfixe HTTP : `/flotte-vehicules/`

## Pile et structure

Le code suit une architecture en couches, dans `src/main/java/com/flottevehicules/flotte_vehicules_api` :

- `controller` : endpoints HTTP, validation des corps, documentation OpenAPI ;
- `dto/request` et `dto/response` : contrats publics sous forme de records Java ;
- `entity` : entités JPA, une par table métier ;
- `repository` : repositories Spring Data JPA et `VueRepository`, seul accès aux vues via `JdbcTemplate` ;
- `mapper` : conversion entité ↔ DTO ;
- `service` : règles métier F1 à F19 et transactions ;
- `exception` : exceptions typées et `GlobalExceptionHandler` en Problem Details RFC 9457 ;
- `config` : horloge applicative, planification F16 et OpenAPI.

Les dix vues du schéma ne sont **jamais** mappées comme entités JPA. Elles sont lues uniquement en SQL, et leur accès HTTP est limité à une liste fermée de noms.

## Démarrer en local

Prérequis : JDK 21 et PostgreSQL avec une base `flotte_vehicules`. La configuration par défaut utilise `root` / `root` sur `localhost:5432` ; elle est surchargeable par `DB_URL`, `DB_USER` et `DB_PASSWORD`.

```bash
cd projets-1-5/05-flotte-vehicules-api
./mvnw spring-boot:run
```

Flyway reconstruit le schéma puis charge le jeu de démonstration `db/dev/V9__demo.sql` : 3 sites,
5 véhicules, 4 salariés, 5 affectations, 3 types d'entretien, 4 prestataires, 6 entretiens, des
trajets, contrats et dépenses. Il contient aussi une alerte déjà émise pour le véhicule 2 afin de
vérifier la non-duplication F16. Le profil `prod` (`SPRING_PROFILES_ACTIVE=prod`) ne charge que
`db/migration`, sans démo.

Vérifications rapides :

```bash
curl http://localhost:8085/actuator/health/readiness
curl http://localhost:8085/v3/api-docs
```

## Démarrer avec Docker

Copier `.env.example` vers `.env`, renseigner `DB_PASSWORD`, puis :

```bash
docker compose up -d --build
docker compose logs -f app
docker compose down
```

Le service `db` publie PostgreSQL uniquement sur le réseau Compose ; le service `app` expose le port
8085 et démarre avec le profil `dev` : au premier `up`, Flyway reconstruit le schéma (`db/migration`,
V1 à V3) puis charge les données de démonstration (`db/dev/V9__demo.sql`). Les dépendances Maven sont
téléchargées au build de l'image.

## Préfixe et convention d'URL

Toutes les ressources métier sont sous `/flotte-vehicules/`. Les noms de ressources sont au singulier et l'action est dans le chemin, sans verbe HTTP détourné :

- création : `POST /flotte-vehicules/site/create`
- liste : `GET /flotte-vehicules/vehicule/list`
- lecture : `GET /flotte-vehicules/site/{id}`
- modification : `PUT /flotte-vehicules/site/{id}/update`
- suppression : `DELETE /flotte-vehicules/site/{id}/delete`
- action métier : `POST /flotte-vehicules/affectation/{id}/restitution`
- calcul : `GET /flotte-vehicules/vehicule/{id}/cout`

Les erreurs métier sont des Problem Details avec un `type` stable, par exemple `https://api.flotte-vehicules.local/problems/affectation-chevauchement`. Les erreurs de validation champ par champ exposent un tableau `champs`.

## Fonctionnalités et endpoints

### F1 à F5 — CRUD

- **F1 — Sites** : `POST /site/create`, `GET /site/list`, `GET /site/{id}`, `PUT /site/{id}/update`, `DELETE /site/{id}/delete`. Nom, ville et adresse obligatoires ; nom unique ; liste triée par ville puis nom ; suppression refusée si le site a des véhicules.
- **F2 — Véhicules** : `POST /vehicule/create`, `GET /vehicule/list`, `GET /vehicule/{id}`, `PUT /vehicule/{id}/update`, `DELETE /vehicule/{id}/delete`. Immatriculation unique, date de mise en service au plus aujourd'hui, transfert de site autorisé, suppression refusée en cas d'affectation ou d'entretien.
- **F3 — Salariés** : `POST /salarie/create`, `GET /salarie/list`, `GET /salarie/{id}`, `PUT /salarie/{id}/update`, `DELETE /salarie/{id}/delete`. Matricule et service obligatoires, matricule figé, suppression refusée si une affectation est en cours.
- **F4 — Affectations** : `POST /affectation/create`, `GET /affectation/list`, `GET /affectation/{id}`, `PUT /affectation/{id}/update`, `DELETE /affectation/{id}/delete`. Véhicule, salarié et date de début obligatoires ; le véhicule et le salarié sont figés après le début.
- **F5 — Entretiens** : `POST /entretien/create`, `GET /entretien/list`, `GET /entretien/{id}`, `PUT /entretien/{id}/update`, `DELETE /entretien/{id}/delete`. Coût strictement positif, date et kilométrage figés, suppression avant le début de l'immobilisation seulement.

Tous les chemins ci-dessus sont préfixés par `/flotte-vehicules/`.

### F6 à F10 — Consultation et recherche

- **F6 — Parc d'un site** : `GET /flotte-vehicules/site/{id}/vehicules`. Véhicules triés par immatriculation avec état `LIBRE`, `AFFECTE` ou `IMMOBILISE`.
- **F7 — Affectations d'un salarié** : `GET /flotte-vehicules/salarie/{id}/affectations`. Historique du plus récent au plus ancien, avec indicateur `enCours`.
- **F8 — Historique d'entretien** : `GET /flotte-vehicules/vehicule/{id}/entretiens`. Tri décroissant et cumul des coûts ; cumul nul si aucun entretien.
- **F9 — Recherche** : `GET /flotte-vehicules/vehicule/list?immatriculation=AB-123&marque=Peugeot`. Critères optionnels et combinables, recherche insensible à la casse sur l'immatriculation.
- **F10 — Pagination** : `GET /flotte-vehicules/entretien/list?page=0&size=20&sort=date,desc`. Réponse `content`, `page`, `size`, `totalElements`, `totalPages` ; taille plafonnée à 100 ; tri autorisé sur `date` et `cout`.

### F11 à F14 — Règles de chevauchement et de compteur

- **F11 — Anti-chevauchement** : à la création d'une affectation, une affectation existante qui recouvre la période est refusée en 409 en citant l'affectation bloquante et son salarié. Une affectation sans date de fin bloque toute période postérieure à son début. Un salarié peut en revanche avoir plusieurs véhicules sur la même période.
- **F12 — Immobilisation** : une affectation qui recouvre un entretien est refusée en 409 en citant l'entretien et ses dates. Réciproquement, un entretien saisi après une affectation ne l'invalide pas : il crée une alerte `CONFLIT_AFFECTATION_ENTRETIEN`, visible via `GET /flotte-vehicules/alerte/list`.
- **F13 — Restitution** : `POST /flotte-vehicules/affectation/{id}/restitution` avec `date` et `km`. La date doit être postérieure au début et non future ; une affectation déjà clôturée est refusée en 409.
- **F14 — Kilométrage croissant** : le relevé d'une restitution ou d'un entretien doit être supérieur ou égal au dernier relevé connu ; un refus 400 indique la dernière valeur et sa date. L'égalité est autorisée (D-01).

### F15 à F19 — Calculs, alertes et reporting

- **F15 — Prochain entretien** : `GET /flotte-vehicules/vehicule/{id}/prochain-entretien`. La vue `v_echeance_entretien` fournit date estimée, kilométrage cible, seuil déclencheur et retard. Les périodicités viennent de `type_entretien` ; les données de démo utilisent 15 000 km et 12 mois. Sans entretien périodique, la date de mise en service sert de point de départ.
- **F16 — Alertes d'échéance** : tâche planifiée chaque nuit à 01:00 (`flotte-vehicules.alertes-entretiens.cron`). Seuil : moins de 1 000 km ou moins de 30 jours avant l'échéance, ou dépassement. Les alertes sont regroupées par site et ne sont pas dupliquées pour une même échéance. Le endpoint `POST /flotte-vehicules/alerte/generer` permet de déclencher manuellement la génération ; `GET /flotte-vehicules/alerte/list` et `GET /flotte-vehicules/alerte/{id}` permettent de la consulter.

  F16 reste une tâche planifiée : le endpoint manuel est un outil de démonstration et de test, pas un remplacement du cron.

- **F17 — Disponibilité** : `GET /flotte-vehicules/vehicule/disponibles?du=2026-11-23&au=2026-11-29`. Sont exclus les véhicules affectés ou immobilisés sur tout ou partie de la période ; le résultat est groupé par site avec kilométrage et prochaine échéance. Sans paramètre, la semaine en cours est utilisée.
- **F18 — Coût** : `GET /flotte-vehicules/vehicule/{id}/cout`. Retourne le coût total des entretiens, le coût au kilomètre, l'âge, le compteur et la répartition par type d'entretien (D-03). Un kilométrage nul ne provoque pas de division par zéro.
- **F19 — Kilométrage par site** : `GET /flotte-vehicules/site/{id}/kilometrage?mois=2026-11`. Total et détail par véhicule, calculés à partir des relevés de restitution du mois.

### Vues SQL

`GET /flotte-vehicules/vue/{nom}` lit une des dix vues du schéma, par exemple `v_bilan_vehicule` ou `v_echeance_entretien`. Tout autre nom est refusé en 400. Cette route est un outil de diagnostic en lecture seule.

## Schéma et migrations

Les migrations Flyway sont dans `src/main/resources/db/migration` :

- `V1__baseline.sql` : les 11 tables et les 10 vues fournies, avec colonnes, clés, contraintes et identités `GENERATED BY DEFAULT AS IDENTITY`. Les corps des vues, non fournis dans le schéma source, ont été reconstruits de façon cohérente avec leurs colonnes et les besoins F12, F15, F16, F17 et F18. **V1 est un baseline validé et n'est plus modifiable.**
- `V2__champs_enonce.sql` : ajoute `site.ville`, `UNIQUE (site.nom)`, `salarie.matricule`, `salarie.service` et `affectation.km_restitution` ; remplace `entretien_cout_check` par `cout > 0` ; redéfinit les vues qui exposent un compteur et la vue de conflit.
- `V3__alerte_vehicule.sql` : crée la table d'alertes avec une seule clé étrangère vers `affectation`, un motif, une période, une date d'alerte, une échéance et les écarts constatés. L'unicité `(id_vehicule, motif, date_echeance)` assure la traçabilité F16.
- `db/dev/V9__demo.sql` : données de démonstration du profil `dev`, identifiants explicites et séquences repositionnées.

Hibernate est en `ddl-auto: validate` : il ne modifie jamais le schéma. Les vues sont lues avec `JdbcTemplate` dans `VueRepository`, jamais mappées comme entités. Le SQL de démonstration a été vérifié avec `psql` dans un schéma temporaire, puis annulé par `ROLLBACK`.

## Arbitrages

### A-01 — Site courant et site d'origine

Le schéma fourni n'a pas de `affectation.id_site` et aucune table `transfert_site` n'a été ajoutée. Le site d'un véhicule reste donc `vehicule.id_site`, et aucun backfill n'a été réalisé.

Limite documentée : après un transfert en cours de mois, F19 compte les relevés de restitution sur le **site courant**, pas sur le site d'origine au moment de l'affectation. Le endpoint F19 indique cette limite dans sa description OpenAPI.

### A-02 — Stockage unique des alertes

Une seule table `alerte_vehicule` sert aux alertes de conflit F12 et d'échéance F16. Elle n'a qu'une clé étrangère vers `affectation` : `id_vehicule` est une colonne simple afin de pouvoir alerter un véhicule sans affectation en cours. Aucune clé étrangère vers `vehicule` ni vers `entretien` n'a été ajoutée.

### D-01 — Kilométrage croissant

La règle est « supérieur ou égal » au dernier relevé connu. L'égalité est acceptée : un véhicule qui n'a pas roulé depuis la dernière relevée peut être restitué avec le même compteur. Le refus indique toujours la dernière valeur et sa date.

### D-02 — Date d'un entretien

« La date » d'un entretien est `date_debut`. Sans `date_fin`, l'immobilisation dure une seule journée : `COALESCE(date_fin, date_debut)` est utilisé dans les contrôles F12 et F17 et dans la vue `v_conflit_affectation_entretien` redéfinie en V2.

### D-03 — Coût demandé par F18

F18 somme uniquement `cout_entretiens`, tel qu'exposé par `v_cout_total_vehicule`, ou `cout_total` de `v_bilan_vehicule`. Les achats, contrats et dépenses ne sont pas inclus dans le coût total affiché, bien qu'ils soient présents dans le schéma et dans la vue de ventilation.

## Collection Postman

La collection `postman/Flotte-Vehicules-API.postman_collection.json` contient tous les endpoints, des corps d'exemple, les codes attendus en description et des scripts de capture d'identifiants. Elle déclare `auth: noauth`.

Procédure conseillée avec le profil `dev` : exécuter les créations de site, salarié, véhicule, affectation, entretien, puis les consultations et enfin `POST /alerte/generer`. Les variables `idSite`, `idSalarie`, `idVehicule`, `idAffectation` et `idEntretien` sont renseignées automatiquement par les réponses 201.

## Tests

```bash
./mvnw -q verify
```

La suite couvre :

- les services par tests unitaires Mockito, avec horloge fixe au 24 novembre 2026 ;
- la couche web par `@WebMvcTest` (import `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`) et `tools.jackson.databind.json.JsonMapper`, avec vérification des codes 200, 201, 204, 400, 404 et 409 et des Problem Details ;
- l'intégration par Testcontainers `postgres:16-alpine`, `@DynamicPropertySource` et un bean `Clock` fixe `@Primary` : parcours complet F1 à F19, conflits F11/F12/F14, non-duplication F16, disponibilité F17, recherche F9, échéance F15 et lecture des vues.

Les tests d'intégration ne chargent pas `V9__demo.sql` : ils insèrent leurs propres référentiels et nettoient la base avant chaque scénario. Aucun commit ni push n'a été effectué.

## Références

Les documents de travail (énoncé, schéma PostgreSQL de référence, audit, plan, conventions) sont
dans le dossier `docs/` hors de ce dépôt :

- Énoncé : `docs/05-flotte-vehicules/enonce/flotte_vehicules.md`
- Audit d'affinité schéma / énoncé : `docs/05-flotte-vehicules/audit/20261124_affinite_schema_enonce.md`
- Conventions de code : `docs/helper/CONVENTIONS.md`
- Projet 3, modèle d'architecture et de tests : `../03-interventions-terrain-api`
