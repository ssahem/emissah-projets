package com.billetterie.billetterie_api.enums;

/**
 * Types de spectacle autorisés par la contrainte ck_spectacle_type.
 *
 * COUCHE       : Enum
 * RÔLE         : représenter un ensemble fermé côté Java.
 * NE FAIT PAS  : de logique métier ni de duplication d’une table SQL imposée par le schéma.
 */
public enum TypeSpectacle {

    // ═════════════════════════════════════════════════════════════════════════
    //  CONSTANTES
    // ═════════════════════════════════════════════════════════════════════════

    /** Concert. */ CONCERT,
    /** Théâtre. */ THEATRE,
    /** Conférence. */ CONFERENCE
}
