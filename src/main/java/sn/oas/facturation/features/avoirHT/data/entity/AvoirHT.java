package sn.oas.facturation.features.avoirHT.data.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.facturation.data.entity.FactureHT;

@Entity
@Table(name = "avoirs_ht")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class AvoirHT extends FactureHT implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    // Herite de tous les champs de Facturation et FactureHT
}
