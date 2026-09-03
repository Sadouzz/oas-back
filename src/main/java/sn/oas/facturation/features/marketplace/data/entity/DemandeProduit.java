package sn.oas.facturation.features.marketplace.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.marketplace.data.enums.StatutDemandeProduit;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

@Entity
@Table(name = "marketplace_demandes")
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

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
