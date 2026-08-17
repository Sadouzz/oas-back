package sn.oas.facturation.ordreReparation.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import sn.oas.facturation.ordreReparation.data.enums.TypePieceJointe;

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
