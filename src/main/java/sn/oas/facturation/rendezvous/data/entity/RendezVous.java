package sn.oas.facturation.rendezvous.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.vehicule.data.entity.Vehicule;

import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendezVous {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = true)
    private Vehicule vehicule;

    @Column(name = "date_rendez_vous", nullable = false)
    private LocalDateTime dateRendezVous;

    @Column(name = "ancienne_date_rendez_vous")
private LocalDateTime ancienneDateRendezVous;

    @Column(columnDefinition = "TEXT")
    private String motif;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RendezVousStatus statut = RendezVousStatus.EN_ATTENTE;

    @Column(columnDefinition = "TEXT")
    private String commentaire;

    @Column(name = "date_creation", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime dateCreation = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.statut == null) {
            this.statut = RendezVousStatus.EN_ATTENTE;
        }
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }
}
