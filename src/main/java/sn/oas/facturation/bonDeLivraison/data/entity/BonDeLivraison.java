package sn.oas.facturation.bonDeLivraison.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import sn.oas.facturation.facturation.data.entity.FactureTTC;

@Entity
@Table(name = "bons_de_livraison")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class BonDeLivraison extends FactureTTC {

}
