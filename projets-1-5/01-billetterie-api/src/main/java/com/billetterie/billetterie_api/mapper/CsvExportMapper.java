package com.billetterie.billetterie_api.mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Fabricant du CSV d'export des réservations (F19).
 *
 * COUCHE       : Mapper
 * RÔLE         : sérialiser les lignes SQL en texte CSV, avec échappement des virgules et guillemets.
 * NE FAIT PAS  : d'interprétation métier ni d'écriture de fichier.
 */
@Component
public class CsvExportMapper {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    // ── toCsv() ──────────────────────────────────────────────────────────────

    /** Construit le CSV ; l'en-tête est présent même s'il n'y a aucune ligne. */
    public String toCsv(List<Object[]> lignes) {
        StringBuilder csv = new StringBuilder("rang,numero,categorie,nom,email,statut,date_creation\n");
        for (Object[] ligne : lignes) {
            csv.append(escape(ligne[0])).append(',')
                    .append(escape(ligne[1])).append(',')
                    .append(escape(ligne[2])).append(',')
                    .append(escape(ligne[3])).append(',')
                    .append(escape(ligne[5])).append(',')
                    .append(escape(ligne[6])).append(',')
                    .append(format(ligne[7])).append('\n');
        }
        return csv.toString();
    }

    // ── escape() ─────────────────────────────────────────────────────────────

    /** Échappe une valeur selon les règles CSV (doublage des guillemets). */
    private static String escape(Object valeur) {
        if (valeur == null) {
            return "";
        }
        String texte = valeur.toString().replace("\"", "\"\"");
        return texte.indexOf(',') >= 0 || texte.indexOf('"') >= 0 || texte.indexOf('\n') >= 0
                ? "\"" + texte + "\"" : texte;
    }

    // ── format() ─────────────────────────────────────────────────────────────

    /** Formate une date SQL en ISO local. */
    private static String format(Object valeur) {
        if (valeur instanceof LocalDateTime date) {
            return date.format(DATE_FORMAT);
        }
        return escape(valeur);
    }
}
