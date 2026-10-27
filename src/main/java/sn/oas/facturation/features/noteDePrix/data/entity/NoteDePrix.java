package sn.oas.facturation.features.noteDePrix.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.features.facture.data.enums.StatutPaiement;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facturation.data.entity.FactureHT;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;

import java.math.BigDecimal;

@Entity
@Table(name = "notes_de_prix")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class NoteDePrix extends FactureHT implements TenantAware {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_livraison_id", unique = true)
    @com.fasterxml.jackson.annotation.JsonIgnore
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private BonDeLivraison bonDeLivraison;


    @Column(name = "numero_bon_de_commande")
    private String numeroBonDeCommande;

    @Column(name = "montant_autre", precision = 15, scale = 2)
    private BigDecimal montantAutre;

    @Column(name = "tva_applicable")
    private Boolean tvaApplicable;

    @Column(name = "montant_tva", precision = 15, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "montant_paye", precision = 15, scale = 2)
    @lombok.Builder.Default
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @Column(name = "reste_a_payer", precision = 15, scale = 2)
    private BigDecimal resteAPayer;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_paiement")
    @lombok.Builder.Default
    private StatutPaiement statutPaiement = StatutPaiement.NON_PAYE;

    @Column(name = "mode_paiement")
    private String modePaiement;
}
