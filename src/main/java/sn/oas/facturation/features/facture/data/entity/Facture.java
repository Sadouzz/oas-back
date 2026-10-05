package sn.oas.facturation.features.facture.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facturation.data.entity.FactureTTC;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.features.recu.data.entity.Recu;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.pdfTemplate.data.entity.PdfTemplate;

import java.math.BigDecimal;

@Entity
@Table(name = "factures")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Facture extends FactureTTC {

    /** Template version captured when the invoice was issued; null denotes legacy rendering. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pdf_template_id")
    private PdfTemplate pdfTemplate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(name = "numero_bon_de_commande")
    private String numeroBonDeCommande;

    @Column(name = "montant_autre", precision = 15, scale = 2)
    private BigDecimal montantAutre;

    @Column(name = "montant_paye", precision = 15, scale = 2)
    @lombok.Builder.Default
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(name = "reste_a_payer", precision = 15, scale = 2)
    private BigDecimal resteAPayer;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement")
    @lombok.Builder.Default
    private StatutPaiement statutPaiement = StatutPaiement.NON_PAYE;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, orphanRemoval = true)
    @lombok.Builder.Default
    private java.util.List<Recu> recus = new java.util.ArrayList<>();
}
