package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.Appartement;
import java.math.BigDecimal;
import java.util.List;

/**
 * Fragment de recherche dynamique des appartements (F9).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : construire la requête selon les critères réellement fournis.
 * UTILISÉ PAR  : AppartementRepository, donc AppartementService uniquement.
 * NE FAIT PAS  : de validation des bornes ni de conversion en DTO.
 */
public interface AppartementSearchRepository {

    // ═════════════════════════════════════════════════════════════════════════
    //  search()
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Retourne les appartements satisfaisant tous les critères non nuls.
     * Sans critère, tous les appartements sont renvoyés dans le même ordre que la liste.
     */
    List<Appartement> search(BigDecimal surfaceMin, BigDecimal surfaceMax, Integer nbPieces);
}
