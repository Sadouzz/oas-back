package sn.oas.facturation.marketplace.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.marketplace.data.enums.StatutDemandeProduit;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_demandes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantite = 1;

    @Column(length = 4000)
    private String message;

    @Column(nullable = false)
    @Builder.Default
    private StatutDemandeProduit statut = StatutDemandeProduit.EN_ATTENTE;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }
}
