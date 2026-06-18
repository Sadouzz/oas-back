package sn.oas.facturation.piecedetache.data.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("PDP")
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PDP extends PieceDetache {

    private Integer qteReelle;
    private Integer stockAtelier;
    private Integer stockMagasin;
    private Double prix;
    private Integer seuilMinimum;

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    protected void calculateQteReelle() {
        this.stockAtelier = this.stockAtelier == null ? 0 : this.stockAtelier;
        this.stockMagasin = this.stockMagasin == null ? 0 : this.stockMagasin;
        this.qteReelle = this.stockAtelier + this.stockMagasin;
    }
}
