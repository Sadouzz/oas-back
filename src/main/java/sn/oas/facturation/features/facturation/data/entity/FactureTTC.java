package sn.oas.facturation.features.facturation.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
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

    /** Remise entreprise capturée à l'émission; les modifications ultérieures du compte ne la changent pas. */
    @Column(name = "taux_remise_client", precision = 5, scale = 2)
    private BigDecimal tauxRemiseClient;

    @Column(name = "montant_remise_client", precision = 15, scale = 2)
    private BigDecimal montantRemiseClient;
}
