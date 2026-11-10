package sn.oas.facturation.features.ordreReparation.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lignes_ordre_reparation_piece")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneOrdreReparationPiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = false)
    private OrdreReparation ordreReparation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = true) // <-- Nullable for custom pieces
    private PieceDetache piece;

    @Builder.Default
    private Boolean isCustom = false;

    private String designationPds;

    @Column(nullable = false)
    private Integer quantite;
    
    @Column(nullable = false)
    private Integer prix;

    @Builder.Default
    @Column(name = "taux_remise", nullable = false, precision = 5, scale = 2)
    private BigDecimal tauxRemise = BigDecimal.ZERO;

    @Column(name = "date_heure_saisie", updatable = false)
    private LocalDateTime dateHeureSaisie;

    @jakarta.persistence.Transient
    @com.fasterxml.jackson.annotation.JsonIgnore
    private boolean dateHeureSaisieHistoriqueInconnue;

    @PrePersist
    protected void onCreate() {
        if (dateHeureSaisie == null && !dateHeureSaisieHistoriqueInconnue) dateHeureSaisie = LocalDateTime.now();
    }
}
