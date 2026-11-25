package com.gestionlocative.gestion_locative_api.enums;

/**
 * Motifs autorisés dans la table de traçabilité des baux (migration V5).
 *
 * COUCHE       : Enum
 * RÔLE         : porter les trois valeurs de {@code ck_historique_bail_motif}.
 * UTILISÉ PAR  : l'entité HistoriqueBail et les services F13, F14 et F16.
 * NE FAIT PAS  : de logique métier.
 */
public enum MotifHistoriqueBail {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    RESILIATION,
    RENOUVELLEMENT,
    CLOTURE
}
