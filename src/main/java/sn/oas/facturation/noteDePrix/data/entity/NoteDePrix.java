package sn.oas.facturation.noteDePrix.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.facturation.data.entity.FactureHT;
import sn.oas.facturation.vehicule.data.entity.Vehicule;

@Entity
@Table(name = "notes_de_prix")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class NoteDePrix extends FactureHT {
    // Les champs spécifiques éventuels.
    // Client et Vehicule sont accessibles via ficheAtelier (hérité de Facturation).
}
