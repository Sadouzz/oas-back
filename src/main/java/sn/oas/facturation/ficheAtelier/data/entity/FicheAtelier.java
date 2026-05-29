package sn.oas.facturation.ficheAtelier.data.entity;

import sn.oas.facturation.vehicule.data.entity.Vehicule;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fiches_atelier")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FicheAtelier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(columnDefinition = "TEXT")
    private String descriptionTravaux;

    // Depending on your DB design, these could be simple Strings, JSON strings, or
    // separate entity lists.
    // Mapped as Strings here for simplicity based on the UML diagram.
    @Column(columnDefinition = "TEXT")
    private String listeReception;

    @Column(columnDefinition = "TEXT")
    private String listeDefauts;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateSortie;

    // ── Relationship Block ───────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    // ─────────────────────────────────────────────────────────

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
    }
}