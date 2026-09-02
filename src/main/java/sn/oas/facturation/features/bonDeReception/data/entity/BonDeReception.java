package sn.oas.facturation.features.bonDeReception.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import sn.oas.facturation.features.facturation.data.entity.FactureTTC;

@Entity
@Table(name = "bons_de_reception")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BonDeReception extends FactureTTC {

}
