package sn.oas.facturation.features.bonDeLivraison.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.facturation.data.entity.FactureTTC;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bons_de_livraison", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bon_livraison_ordre_reparation", columnNames = "ordre_reparation_id"),
        @UniqueConstraint(name = "uk_bon_livraison_ordre_travail", columnNames = "ordre_travail_id")
})
@NoArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class BonDeLivraison extends FactureTTC {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "bons_de_livraison_bons_de_sortie",
            joinColumns = @JoinColumn(name = "bon_de_livraison_id"),
            inverseJoinColumns = @JoinColumn(name = "bon_de_sortie_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_bl_bs", columnNames = {"bon_de_livraison_id", "bon_de_sortie_id"}))
    @Builder.Default
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private List<BonDeSortie> bonsDeSortie = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "bonDeLivraison")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private Facture facture;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", unique = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrdreReparation ordreReparation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_travail_id", unique = true)
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private OrdreTravail ordreTravail;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "bonDeLivraison")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private NoteDePrix noteDePrix;

    @Override
    public OrdreReparation getOrdreReparation() {
        if (this.ordreReparation != null) {
            return this.ordreReparation;
        }
        OrdreReparation superOrdre = super.getOrdreReparation();
        if (superOrdre != null) {
            this.ordreReparation = superOrdre;
            return superOrdre;
        }
        if (this.bonsDeSortie != null) {
            for (BonDeSortie bs : this.bonsDeSortie) {
                if (bs != null) {
                    if (bs.getOrdreReparation() != null) {
                        this.ordreReparation = bs.getOrdreReparation();
                        super.setOrdreReparation(this.ordreReparation);
                        return this.ordreReparation;
                    }
                    if (bs.getOrdreTravail() != null && bs.getOrdreTravail().getOrdreReparation() != null) {
                        this.ordreReparation = bs.getOrdreTravail().getOrdreReparation();
                        super.setOrdreReparation(this.ordreReparation);
                        return this.ordreReparation;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void setOrdreReparation(OrdreReparation ordreReparation) {
        this.ordreReparation = ordreReparation;
        super.setOrdreReparation(ordreReparation);
    }

    public OrdreTravail getOrdreTravail() {
        if (this.ordreTravail != null) {
            return this.ordreTravail;
        }
        if (this.bonsDeSortie != null) {
            for (BonDeSortie bs : this.bonsDeSortie) {
                if (bs != null && bs.getOrdreTravail() != null) {
                    this.ordreTravail = bs.getOrdreTravail();
                    return this.ordreTravail;
                }
            }
        }
        return null;
    }

    public void setOrdreTravail(OrdreTravail ordreTravail) {
        this.ordreTravail = ordreTravail;
    }

    @PrePersist
    @PreUpdate
    protected void syncRelations() {
        if (this.ordreReparation == null) {
            this.ordreReparation = getOrdreReparation();
        }
        if (this.ordreReparation != null) {
            super.setOrdreReparation(this.ordreReparation);
        }
        if (this.ordreTravail == null) {
            this.ordreTravail = getOrdreTravail();
        }
    }
}
