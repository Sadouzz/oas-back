package sn.oas.facturation.ficheAtelier.data.entity;

import sn.oas.facturation.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.facturation.data.entity.Facturation;
import sn.oas.facturation.mecanicien.data.entity.Mecanicien;
import sn.oas.facturation.ficheAtelier.data.enums.StatutReparation;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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

    @Column(nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "update_at", nullable = false)
    @Builder.Default
    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime dateSortie;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    @Builder.Default
    private StatutReparation statut = StatutReparation.A_FAIRE;

    // ── Relationship Block ───────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "fiche_mecaniciens",
        joinColumns = @JoinColumn(name = "fiche_id"),
        inverseJoinColumns = @JoinColumn(name = "mecanicien_id")
    )
    @Builder.Default
    @JsonIgnoreProperties("fichesAtelier")
    private List<Mecanicien> mecaniciens = new ArrayList<>();

    @OneToOne(mappedBy = "ficheAtelier", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("ficheAtelier") // Ignore le champ "ficheAtelier" qui est DANS le "BonDeSortie"
    private BonDeSortie bonDeSortie;

    @OneToMany(mappedBy = "ficheAtelier", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Facturation> facturations = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutReparation.A_FAIRE;
        }
    }
}