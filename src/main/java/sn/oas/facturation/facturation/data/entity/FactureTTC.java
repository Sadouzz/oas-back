package sn.oas.facturation.facturation.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class FactureTTC extends Facturation {

    @Column(name = "montant_ht", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantHT;

    @Column(name = "montant_tva", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "montant_ttc", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @Column(name = "montant_timbre", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantTimbre;
}

