package sn.oas.facturation.features.ordreReparation.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import sn.oas.facturation.features.ordreReparation.data.enums.TypePieceJointe;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

import java.time.LocalDateTime;

/**
 * Pièce jointe (photo ou PDF) rattachée au diagnostic d'un {@link OrdreReparation}.
 * Toujours accédée via son OrdreReparation parent, lui-même déjà filtré par garage
 * (@Filter garageFilter) : pas besoin d'implémenter TenantAware ici (pas de colonne
 * garage_id dédiée), cf. rapport de la tâche pour la justification détaillée.
 */
@Entity
@Table(name = "pieces_jointes_diagnostic")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PieceJointeDiagnostic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = false)
    @JsonIgnoreProperties({ "piecesJointesDiagnostic", "lignesOrdreReparationPieces", "lignesOrdreReparationMainDoeuvres", "facturations" })
    private OrdreReparation ordreReparation;

    @Column(nullable = false, length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePieceJointe type;

    @Column(columnDefinition = "TEXT")
    private String remarque;

    // Nullable : renseigné uniquement quand la pièce jointe est ajoutée depuis le portail
    // technicien (voir TechnicienPortalController), null quand ajoutée côté staff/agent.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id")
    @JsonIgnoreProperties({ "password", "authorities", "garage" })
    private Technicien technicien;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
