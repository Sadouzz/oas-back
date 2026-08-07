package sn.oas.facturation.rendezvous.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "rendez_vous_date_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RendezVousDateHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rendez_vous_id", nullable = false)
    private RendezVous rendezVous;

    @Column(name = "ancienne_date", nullable = false)
    private LocalDateTime ancienneDate;

    @Column(name = "nouvelle_date", nullable = false)
    private LocalDateTime nouvelleDate;

    @Column(name = "date_modification", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime dateModification = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.dateModification == null) {
            this.dateModification = LocalDateTime.now();
        }
    }
}