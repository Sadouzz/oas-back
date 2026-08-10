package sn.oas.facturation.proforma.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.garage.data.entity.Garage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.facturation.data.entity.FactureTTC;
import sn.oas.facturation.vehicule.data.entity.Vehicule;

import java.math.BigDecimal;

@Entity
@Table(name = "proformas")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class Proforma extends FactureTTC implements TenantAware  {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "garage_id")
    private Garage garage;

    /*
     * @ManyToOne(fetch = FetchType.LAZY)
     * 
     * @JoinColumn(name = "client_id", nullable = false)
     * private Client client;
     * 
     * @ManyToOne(fetch = FetchType.LAZY)
     * 
     * @JoinColumn(name = "vehicule_id", nullable = false)
     * private Vehicule vehicule;
     * 
     * @Column(name = "numero_bon_de_commande")
     * private String numeroBonDeCommande;
     */

    /*
     * @Column(name = "montant_autre", precision = 15, scale = 2)
     * private BigDecimal montantAutre;
     */
}

