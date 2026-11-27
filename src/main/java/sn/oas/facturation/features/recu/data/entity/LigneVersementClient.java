package sn.oas.facturation.features.recu.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.noteDePrix.data.entity.NoteDePrix;
import sn.oas.facturation.features.recu.data.entity.RecuFacture;

import java.math.BigDecimal;

/** Part d'un versement client imputée à une facture (avec son reçu) ou à une note de prix. */
@Entity
@Table(name = "lignes_versement_client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneVersementClient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "versement_id", nullable = false)
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private VersementClient versement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id")
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Facture facture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recu_id")
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private RecuFacture recuFacture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_de_prix_id")
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private NoteDePrix noteDePrix;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;
}
