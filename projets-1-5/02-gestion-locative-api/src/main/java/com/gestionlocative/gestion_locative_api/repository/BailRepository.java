package com.gestionlocative.gestion_locative_api.repository;

import com.gestionlocative.gestion_locative_api.entity.Bail;
import com.gestionlocative.gestion_locative_api.enums.StatutBail;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accès aux données des baux (F5, F11).
 *
 * COUCHE       : Repository (persistance)
 * RÔLE         : lire et écrire des {@link Bail}, et rechercher les chevauchements
 *                de périodes sous verrou pessimiste.
 * UTILISÉ PAR  : BailService uniquement (jamais par un contrôleur).
 * NE FAIT PAS  : de règle métier ni de conversion en DTO.
 */
public interface BailRepository extends JpaRepository<Bail, Integer> {

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES DÉDUITES
    // ═════════════════════════════════════════════════════════════════════════

    // ── findAllByOrderByDateDebutDesc() ──────────────────────────────────────

    /** Retourne les baux triés par date de début décroissante. */
    List<Bail> findAllByOrderByDateDebutDesc();

    // ── findAllByIdAppartementOrderByDateDebutDesc() ─────────────────────────

    /** Retourne l'historique des baux d'un appartement (F6, F8). */
    List<Bail> findAllByIdAppartementOrderByDateDebutDesc(Integer idAppartement);

    // ── findAllByIdAppartementOrderByDateDebutAsc() ──────────────────────────

    /** Retourne les baux d'un appartement du plus ancien au plus récent (F18). */
    List<Bail> findAllByIdAppartementOrderByDateDebutAsc(Integer idAppartement);

    // ── countByIdAppartement() ───────────────────────────────────────────────

    /** Compte les baux d'un appartement, pour refuser sa suppression (F3). */
    long countByIdAppartement(Integer idAppartement);

    // ── findByStatutAndDateFinBefore() ───────────────────────────────────────

    /** Retourne les baux en cours arrivés à échéance avant une date (F16). */
    List<Bail> findByStatutAndDateFinBefore(StatutBail statut, LocalDate date);

    // ═════════════════════════════════════════════════════════════════════════
    //  REQUÊTES @QUERY
    // ═════════════════════════════════════════════════════════════════════════

    // ── findChevauchements() ─────────────────────────────────────────────────

    /**
     * Recherche les baux non résiliés qui recouvrent la période demandée (F11).
     * Le verrou pessimiste sérialise les créations concurrentes sur un même
     * appartement, en attendant la contrainte d'exclusion SQL de V1 (E-09).
     *
     * @return la liste des baux bloquants, vide si l'appartement est libre
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bail b WHERE b.idAppartement = :idAppartement "
            + "AND b.statut <> com.gestionlocative.gestion_locative_api.enums.StatutBail.RESILIE "
            + "AND b.dateDebut <= :fin "
            + "AND (b.dateFin IS NULL OR b.dateFin >= :debut)")
    List<Bail> findChevauchements(@Param("idAppartement") Integer idAppartement,
                                  @Param("debut") LocalDate debut,
                                  @Param("fin") LocalDate fin);

    // ── countEnCoursByLocataire() ────────────────────────────────────────────

    /** Compte les baux en cours d'un locataire, pour refuser sa suppression (F4). */
    @Query("SELECT COUNT(b) FROM Bail b, BailLocataire bl "
            + "WHERE bl.id.idBail = b.idBail AND bl.id.idLocataire = :idLocataire "
            + "AND b.statut = :statut")
    long countEnCoursByLocataire(@Param("idLocataire") Integer idLocataire,
                                 @Param("statut") StatutBail statut);

    // ── countEnCoursByIdImmeuble() ───────────────────────────────────────────

    /**
     * Compte les baux en cours des appartements d'un immeuble (F7).
     * La jointure passe par {@code appartement.id_immeuble} car les entités n'ont
     * pas de relation JPA : le schéma reste la source de vérité.
     */
    @Query("SELECT COUNT(b) FROM Bail b, Appartement a "
            + "WHERE a.idImmeuble = :idImmeuble AND b.idAppartement = a.idAppartement "
            + "AND b.statut = :statut")
    long countEnCoursByIdImmeuble(@Param("idImmeuble") Integer idImmeuble,
                                 @Param("statut") StatutBail statut);

    // ── findIdAppartementsOccupesLe() ────────────────────────────────────────

    /**
     * Identifiants des appartements couverts par un bail en cours à une date (F17).
     * Les baux résiliés ou clôturés sont exclus : l'appartement est de nouveau louable.
     */
    @Query("SELECT DISTINCT b.idAppartement FROM Bail b "
            + "WHERE b.statut = com.gestionlocative.gestion_locative_api.enums.StatutBail.EN_COURS "
            + "AND b.dateDebut <= :date "
            + "AND (b.dateFin IS NULL OR b.dateFin >= :date)")
    List<Integer> findIdAppartementsOccupesLe(@Param("date") LocalDate date);

    // ── findActifsByIdImmeubleEntre() ────────────────────────────────────────

    /**
     * Baux en cours dont la période recoupe un mois, pour un immeuble (F19).
     * Le prorata et le loyer à une date donnée sont calculés en service (E-06).
     */
    @Query("SELECT b FROM Bail b, Appartement a "
            + "WHERE a.idImmeuble = :idImmeuble AND b.idAppartement = a.idAppartement "
            + "AND b.statut = com.gestionlocative.gestion_locative_api.enums.StatutBail.EN_COURS "
            + "AND b.dateDebut <= :fin "
            + "AND (b.dateFin IS NULL OR b.dateFin >= :debut)")
    List<Bail> findActifsByIdImmeubleEntre(@Param("idImmeuble") Integer idImmeuble,
                                           @Param("debut") LocalDate debut,
                                           @Param("fin") LocalDate fin);
}
