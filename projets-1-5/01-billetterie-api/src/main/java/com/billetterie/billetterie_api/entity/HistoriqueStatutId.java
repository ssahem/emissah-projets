package com.billetterie.billetterie_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/** Clé composite de historique_statut : (id_reservation, id_statut).
 * COUCHE : Entité (clé embarquée) · NE FAIT PAS : de logique métier. */
@Embeddable
public class HistoriqueStatutId implements Serializable {
    @Column(name = "id_reservation")
    private Integer idReservation;
    @Column(name = "id_statut")
    private Integer idStatut;
    protected HistoriqueStatutId() {}
    public HistoriqueStatutId(Integer idReservation, Integer idStatut) {
        this.idReservation = idReservation;
        this.idStatut = idStatut;
    }
    public Integer getIdReservation() { return idReservation; }
    public Integer getIdStatut() { return idStatut; }
    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (!(o instanceof HistoriqueStatutId other)) { return false; }
        return Objects.equals(idReservation, other.idReservation) && Objects.equals(idStatut, other.idStatut);
    }
    @Override
    public int hashCode() { return Objects.hash(idReservation, idStatut); }
}
