package sn.oas.facturation.proforma.data.entity;

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
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Proforma extends FactureTTC {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @Column(name = "numero_bon_de_commande")
    private String numeroBonDeCommande;

    @Column(name = "montant_autre", precision = 15, scale = 2)
    private BigDecimal montantAutre;
}
