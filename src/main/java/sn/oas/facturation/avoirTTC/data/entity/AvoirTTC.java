package sn.oas.facturation.avoirTTC.data.entity;

import jakarta.persistence.*;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.facturation.data.entity.FactureTTC;

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
