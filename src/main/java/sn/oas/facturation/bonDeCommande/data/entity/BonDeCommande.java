package sn.oas.facturation.bonDeCommande.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.bonDeCommande.data.enums.StatutBonCommande;
import sn.oas.facturation.facturation.data.entity.Facturation;
import sn.oas.facturation.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.vehicule.data.entity.Vehicule;

import java.math.BigDecimal;
import java.time.LocalDate;
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
public class BonDeCommande {

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
        private Agent agent; // Agent qui a créé le bon de commande

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