package com.flottevehicules.flotte_vehicules_api.mapper;

import com.flottevehicules.flotte_vehicules_api.dto.response.EntretienResponse;
import com.flottevehicules.flotte_vehicules_api.entity.Entretien;
import com.flottevehicules.flotte_vehicules_api.entity.Prestataire;
import com.flottevehicules.flotte_vehicules_api.entity.TypeEntretien;
import com.flottevehicules.flotte_vehicules_api.repository.PrestataireRepository;
import com.flottevehicules.flotte_vehicules_api.repository.TypeEntretienRepository;
import org.springframework.stereotype.Component;

/**
 * Conversion Entretien → EntretienResponse, avec libellé du type et nom du prestataire.
 *
 * COUCHE       : Mapper (interface HTTP)
 * RÔLE         : Conversion Entretien → EntretienResponse, avec libellé du type et nom du prestataire.
 * DÉPEND DE    : TypeEntretienRepository, PrestataireRepository.
 * NE FAIT PAS  : de calcul de cumul (rôle du service).
 */
@Component
public class EntretienMapper {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES ET CHAMPS
    // ═════════════════════════════════════════════════════════════════════════

    private final TypeEntretienRepository typeEntretienRepository;
    private final PrestataireRepository prestataireRepository;

    public EntretienMapper(TypeEntretienRepository typeEntretienRepository,
                           PrestataireRepository prestataireRepository) {
        this.typeEntretienRepository = typeEntretienRepository;
        this.prestataireRepository = prestataireRepository;
    }

    /** Convertit une entité en réponse ; alerteCreee est renseignée par le service. */

    // ── toResponse() ─────────────────────────────────────────────────────────

    /**
     * Exécute toResponse().
     */

    public EntretienResponse toResponse(Entretien entretien, boolean alerteCreee) {
        String libelle = typeEntretienRepository.findById(entretien.getIdTypeEntretien())
                .map(TypeEntretien::getLibelle).orElse(null);
        String prestataire = prestataireRepository.findById(entretien.getIdPrestataire())
                .map(Prestataire::getNom).orElse(null);
        return new EntretienResponse(
                entretien.getIdEntretien(), entretien.getDateDebut(), entretien.getDateFin(),
                entretien.getIdVehicule(), entretien.getCout(), entretien.getKmCompteur(),
                entretien.getIdTypeEntretien(), libelle, entretien.getIdPrestataire(),
                prestataire, alerteCreee);
    }
}
