package sn.oas.facturation.features.client.data.entity;

import jakarta.persistence.*;
import lombok.*;
import sn.oas.facturation.features.client.data.enums.TypeMouvementPortefeuille;
import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.recu.data.entity.Recu;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "mouvements_portefeuille_client", indexes =
        @Index(name = "idx_mouvement_portefeuille_date", columnList = "portefeuille_id,date_creation"))
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class MouvementPortefeuilleClient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "portefeuille_id", nullable = false, updatable = false)
    private PortefeuilleClient portefeuille;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32, updatable = false)
    private TypeMouvementPortefeuille type;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal montant;

    @Column(name = "mode_paiement", length = 40, updatable = false)
    private String modePaiement;

    @Column(length = 500, updatable = false)
    private String commentaire;

    @Column(name = "acteur_id", updatable = false)
    private Long acteurId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", updatable = false)
    private Facture facture;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recu_id", updatable = false)
    private Recu recu;

    @Column(name = "source_mouvement_legacy_id", unique = true, updatable = false)
    private Long sourceMouvementLegacyId;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    void onCreate() { if (dateCreation == null) dateCreation = LocalDateTime.now(); }
}
