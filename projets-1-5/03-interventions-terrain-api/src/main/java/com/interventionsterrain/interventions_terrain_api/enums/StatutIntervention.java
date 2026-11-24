package com.interventionsterrain.interventions_terrain_api.enums;

/**
 * Statuts du cycle de vie d'une intervention (F5, F14, F18, F19).
 *
 * COUCHE       : Enum (contrat applicatif)
 * RÔLE         : noms des libellés de la table de référence {@code statut}.
 * DÉCISION     : EN_ROUTE et REPORTEE existent dans la base source mais ne font pas partie
 *                des transitions de l'énoncé ; ils restent lisibles (legacy, E-22).
 * CONTRAINTES  : jamais de ORDINAL, toujours une conversion via statut.libelle (E-14).
 */
public enum StatutIntervention {

    PLANIFIEE(false),
    EN_ROUTE(true),
    EN_COURS(false),
    TERMINEE(false),
    ECHOUEE(false),
    ANNULEE(false),
    REPORTEE(true);

    private final boolean legacy;

    StatutIntervention(boolean legacy) {
        this.legacy = legacy;
    }

    /** Indique si la valeur est héritée de la base source et non utilisable en écriture. */
    public boolean isLegacy() {
        return legacy;
    }

    /** Convertit un libellé de la base en enum, ou renvoie null si le libellé est inconnu. */
    public static StatutIntervention fromLibelle(String libelle) {
        if (libelle == null) {
            return null;
        }
        for (StatutIntervention statut : values()) {
            if (statut.name().equals(libelle)) {
                return statut;
            }
        }
        return null;
    }
}
