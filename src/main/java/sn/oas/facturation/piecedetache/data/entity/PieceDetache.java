package sn.oas.facturation.piecedetache.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;

@Entity
@Table(name = "pieces_detachees")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type_piece", discriminatorType = DiscriminatorType.STRING)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class PieceDetache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_piece", insertable = false, updatable = false)
    private TypePiece type;

    @Column(name = "numero_serie", nullable = false, unique = true)
    private String numeroDeSerie;

    @Column(nullable = false)
    private String reference;

    @Column(nullable = false)
    private String categorie;

    @Column(nullable = false)
    private Double pourcentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private StatutPiece statut = StatutPiece.ACTIF;

    @Column(name = "created_at", nullable = false, updatable = false)
    @lombok.Builder.Default
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = java.time.LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutPiece.ACTIF;
        }
    }

    @PostLoad
    protected void syncTypeFromSubclass() {
        if (this instanceof PDP) {
            this.type = TypePiece.PDP;
        } else if (this instanceof PDG) {
            this.type = TypePiece.PDG;
        } else if (this instanceof PDS) {
            this.type = TypePiece.PDS;
        }
    }
}
