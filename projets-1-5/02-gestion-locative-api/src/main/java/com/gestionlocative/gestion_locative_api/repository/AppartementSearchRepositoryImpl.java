package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.Appartement;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation JPA du fragment de recherche dynamique (F9).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : ajouter un prédicat par critère fourni, puis trier par étage et surface.
 * DÉCOUVERTE   : Spring Data associe automatiquement cette classe au fragment
 *                {@link AppartementSearchRepository} par le suffixe « Impl ».
 * NE FAIT PAS  : de HTTP, de validation métier ni de DTO.
 */
public class AppartementSearchRepositoryImpl implements AppartementSearchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    // ═════════════════════════════════════════════════════════════════════════
    //  SEARCH()
    // ═════════════════════════════════════════════════════════════════════════


    // ── search() ─────────────────────────────────────────────────────────────

    /**
     * Construit la requête à partir des seuls critères renseignés.
     */

    @Override
    public List<Appartement> search(BigDecimal surfaceMin, BigDecimal surfaceMax, Integer nbPieces) {
        // ── Étape 1/3 : préparer la requête criteria ─────────────────────────
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Appartement> requete = builder.createQuery(Appartement.class);
        Root<Appartement> appartement = requete.from(Appartement.class);
        List<Predicate> criteres = new ArrayList<>();

        // ── Étape 2/3 : n'ajouter que les critères fournis ───────────────────
        if (surfaceMin != null) {
            criteres.add(builder.greaterThanOrEqualTo(appartement.get("surface"), surfaceMin));
        }
        if (surfaceMax != null) {
            criteres.add(builder.lessThanOrEqualTo(appartement.get("surface"), surfaceMax));
        }
        if (nbPieces != null) {
            criteres.add(builder.equal(appartement.get("nbPieces"), nbPieces));
        }

        // ── Étape 3/3 : trier et exécuter ────────────────────────────────────
        requete.select(appartement)
                .where(criteres.toArray(Predicate[]::new))
                .orderBy(builder.asc(appartement.get("etage")), builder.asc(appartement.get("surface")));
        return entityManager.createQuery(requete).getResultList();
    }
}
