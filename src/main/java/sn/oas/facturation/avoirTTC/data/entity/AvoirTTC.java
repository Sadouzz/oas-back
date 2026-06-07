package sn.oas.facturation.avoirTTC.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.facturation.data.entity.FactureTTC;

@Entity
@Table(name = "avoirs_ttc")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AvoirTTC extends FactureTTC {
    // Hérite de tous les champs de FactureTTC (montantHT, montantTVA, montantTTC, montantTimbre, etc.)
}
