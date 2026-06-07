package sn.oas.facturation.avoirHT.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.facturation.data.entity.FactureHT;

@Entity
@Table(name = "avoirs_ht")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AvoirHT extends FactureHT {
    // Hérite de tous les champs de Facturation et FactureHT
    // (pas de liaison explicite avec la facture d'origine selon la demande)
}
