package sn.oas.facturation.features.diagnostic.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.technicien.data.entity.Technicien;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "diagnostic_id", nullable = false)
    @JsonIgnoreProperties({ "piecesJointes", "remarques", "ordreReparation" })
    private Diagnostic diagnostic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id")
    @JsonIgnoreProperties({ "diagnostic", "vehicule", "garage", "bonDeSortie" })
    private OrdreReparation ordreReparation;

    @Column(nullable = false, length = 1000)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypePieceJointe type;

    @Column(columnDefinition = "TEXT")
    private String remarque;

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
        if (this.ordreReparation == null && this.diagnostic != null && this.diagnostic.getOrdreReparation() != null) {
            this.ordreReparation = this.diagnostic.getOrdreReparation();
        }
    }
}
