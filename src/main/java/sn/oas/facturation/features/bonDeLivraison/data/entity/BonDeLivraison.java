package sn.oas.facturation.features.bonDeLivraison.data.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.facturation.data.entity.FactureTTC;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bons_de_livraison", uniqueConstraints = {
        @UniqueConstraint(name = "uk_bon_livraison_ordre_reparation", columnNames = "ordre_reparation_id")
})
@NoArgsConstructor
@SuperBuilder
@lombok.Getter
@lombok.Setter
@EqualsAndHashCode(callSuper = true)
public class BonDeLivraison extends FactureTTC {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = false, unique = true)
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private OrdreReparation ordreReparation;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "bons_de_livraison_bons_de_sortie",
            joinColumns = @JoinColumn(name = "bon_de_livraison_id"),
            inverseJoinColumns = @JoinColumn(name = "bon_de_sortie_id"),
            uniqueConstraints = @UniqueConstraint(name = "uk_bl_bs", columnNames = {"bon_de_livraison_id", "bon_de_sortie_id"}))
    @lombok.Builder.Default
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private List<BonDeSortie> bonsDeSortie = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "bonDeLivraison")
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Facture facture;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "bonDeLivraison")
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private NoteDePrix noteDePrix;
}
