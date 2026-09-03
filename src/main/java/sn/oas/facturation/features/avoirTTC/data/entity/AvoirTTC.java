package sn.oas.facturation.features.avoirTTC.data.entity;

import jakarta.persistence.*;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.facturation.data.entity.FactureTTC;

@Entity
@Table(name = "avoirs_ttc")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class AvoirTTC extends FactureTTC {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    // Herite de tous les champs de FactureTTC (montantHT, montantTVA, montantTTC, montantTimbre, etc.)
}
