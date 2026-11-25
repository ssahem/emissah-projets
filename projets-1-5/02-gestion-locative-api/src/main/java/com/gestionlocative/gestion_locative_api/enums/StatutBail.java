package com.gestionlocative.gestion_locative_api.enums;

/**
 * Cycle de vie d'un bail (E-04, migration V4).
 *
 * COUCHE       : Enum
 * RÔLE         : porter les trois valeurs autorisées par {@code ck_bail_statut}.
 * NE FAIT PAS  : de logique métier.
 */
public enum StatutBail {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    EN_COURS,
    RESILIE,
    TERMINE
}
