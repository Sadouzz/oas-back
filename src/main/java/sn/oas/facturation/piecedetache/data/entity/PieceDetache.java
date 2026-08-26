package sn.oas.facturation.piecedetache.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.garage.data.entity.Garage;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

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
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
@SQLDelete(sql = "UPDATE pieces_detachees SET deleted = true WHERE id=?")
@SQLRestriction("deleted = false")
public abstract class PieceDetache implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_piece", insertable = false, updatable = false)
    private TypePiece type;

    @Column(unique = true)
    private String numero;

    @Column(name = "numero_serie", nullable = false, unique = true)
    private String numeroDeSerie;

    @Column(nullable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    @Column(name = "categorie", nullable = false)
    private String designation;
    
    @Column(name = "prix_gros")
    private Double prixGros;

    @Column(name = "prix_unitaire")
    private Double prixUnitaire;
    
    @Column(name = "stock_magasin")
    @lombok.Builder.Default
    private Double stockMagasin = 0.0;
    
    @Column(name = "stock_atelier")
    @lombok.Builder.Default
    private Double stockAtelier = 0.0;

    @Column(nullable = false)
    @lombok.Builder.Default
    private Boolean deleted = false;

    @Column(nullable = false)
    private Double pourcentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @lombok.Builder.Default
    private StatutPiece statut = StatutPiece.ACTIF;

    @Column(name = "created_at", nullable = false, updatable = false)
    @lombok.Builder.Default
    @CreationTimestamp
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "update_at")
    @lombok.Builder.Default
    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
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
