package sn.oas.facturation.features.rendezvous.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.rendezvous.data.enums.StatutDemandeDerogationPlafond;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes_derogation_plafond", indexes = {
        @Index(name = "idx_derogation_plafond_statut_date", columnList = "statut,date_demande"),
        @Index(name = "idx_derogation_plafond_client_statut", columnList = "client_id,statut")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeDerogationPlafond {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rendez_vous_id")
    private RendezVous rendezVous;

    @Column(name = "date_rendez_vous", nullable = false)
    private LocalDateTime dateRendezVous;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private StatutDemandeDerogationPlafond statut = StatutDemandeDerogationPlafond.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut_rendez_vous_demande", nullable = false, length = 20)
    private sn.oas.facturation.features.rendezvous.data.enums.RendezVousStatus statutRendezVousDemande;

    @Column(nullable = false, length = 20)
    private String origine;

    @Column(name = "demandeur", nullable = false, length = 255)
    private String demandeur;

    @Column(name = "plafond_snapshot", precision = 15, scale = 2)
    private BigDecimal plafondSnapshot;

    @Column(name = "montant_consomme_snapshot", precision = 15, scale = 2)
    private BigDecimal montantConsommeSnapshot;

    @Column(name = "echeance_jours_snapshot")
    private Integer echeanceJoursSnapshot;

    @Column(name = "date_demande", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateDemande;

    @Column(name = "decisionnaire", length = 255)
    private String decisionnaire;

    @Column(name = "date_decision")
    private LocalDateTime dateDecision;

    @Column(name = "motif_decision", columnDefinition = "TEXT")
    private String motifDecision;

    @PrePersist
    void onCreate() {
        if (dateDemande == null) dateDemande = LocalDateTime.now();
        if (statut == null) statut = StatutDemandeDerogationPlafond.EN_ATTENTE;
    }
}
