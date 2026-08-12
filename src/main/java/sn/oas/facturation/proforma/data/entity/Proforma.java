package sn.oas.facturation.proforma.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.facturation.data.entity.FactureTTC;

import java.math.BigDecimal;

@Entity
@Table(name = "proformas")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Proforma extends FactureTTC {

    @Column(name = "montant_autre", precision = 15, scale = 2)
    private BigDecimal montantAutre;
}
