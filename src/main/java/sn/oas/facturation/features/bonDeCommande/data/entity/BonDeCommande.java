package sn.oas.facturation.features.bonDeCommande.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.features.garage.data.entity.Garage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.bonDeCommande.data.enums.StatutBonCommande;
import sn.oas.facturation.features.facturation.data.entity.Facturation;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "bons_de_commande")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class BonDeCommande implements TenantAware  {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "garage_id")
    private Garage garage;

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String numero;

        @Column(name = "date_commande", nullable = false, updatable = false)
        @CreationTimestamp
        private LocalDateTime dateCommande;

        @Column(name = "date_modification")
        @UpdateTimestamp
        private LocalDateTime dateModification;

        @Enumerated(EnumType.STRING)
        private StatutBonCommande statut;

        private BigDecimal montantHT;

        private BigDecimal montantTVA;

        private BigDecimal montantTTC;

        private Boolean tvaApplicable;

        private Boolean paye;

        private String observation;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "fournisseur_id")
        private Fournisseur fournisseur;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "vehicule_id")
        private Vehicule vehicule;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "agent_id")
        private Agent agent; // Agent qui a crÃ©Ã© le bon de commande

        @OneToMany(mappedBy = "bonDeCommande", cascade = CascadeType.ALL, orphanRemoval = true)
        private List<LigneBonDeCommandePiece> lignes;

        @OneToMany(mappedBy = "bonDeCommande", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private List<Facturation> facturations = new ArrayList<>();

        @PrePersist
        protected void onCreate() {
                if (this.dateCommande == null) {
                        this.dateCommande = LocalDateTime.now();
                }
        }
}
