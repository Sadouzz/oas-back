package sn.oas.facturation.features.piecedetache.data.entity;

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

    private Double qteReelle;
    private Double seuilMinimum;

    @jakarta.persistence.PrePersist
    @jakarta.persistence.PreUpdate
    protected void calculateQteReelle() {
        this.setStockAtelier(this.getStockAtelier() == null ? 0.0 : this.getStockAtelier());
        this.setStockMagasin(this.getStockMagasin() == null ? 0.0 : this.getStockMagasin());
        if (this.qteReelle == null) {
            this.qteReelle = this.getStockAtelier() + this.getStockMagasin();
        }
    }
}
