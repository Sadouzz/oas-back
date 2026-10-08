package sn.oas.facturation.features.client.data.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Journal append-only des crédits ajoutés au compte client. */
@Entity
@Table(name = "mouvements_credit_client", indexes = @Index(name = "idx_mouvement_credit_compte_date", columnList = "compte_client_id,date_creation"))
@Getter @NoArgsConstructor @AllArgsConstructor @Builder
public class MouvementCreditClient {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compte_client_id", nullable = false, updatable = false)
    private CompteClient compteClient;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal montant;

    @Column(length = 500, updatable = false)
    private String commentaire;

    @Column(name = "acteur_id", updatable = false)
    private Long acteurId;

    @Column(name = "date_creation", nullable = false, updatable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    void onCreate() { if (dateCreation == null) dateCreation = LocalDateTime.now(); }
}
