package sn.oas.facturation.features.proforma.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sn.oas.facturation.features.facturation.data.entity.FactureTTC;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@Table(name = "proformas")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Proforma extends FactureTTC {

    /** Relations directes requises également pour les proformas hors fiche atelier. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id")
    private Vehicule vehicule;

    /** Alertes calculées lors de la création, non persistées et destinées à l'agent. */
    @jakarta.persistence.Transient
    @lombok.Builder.Default
    private java.util.List<String> avertissementsFinanciers = new java.util.ArrayList<>();

    /**
     * Voir spec point 7 : un proforma n'est visible côté portail client qu'une fois
     * explicitement validé/envoyé par le chef d'atelier (POST /api/proformas/{id}/valider-envoi).
     * Avant ça, il reste "en préparation" et n'apparaît que côté agent (gestion/proformas),
     * où les prix peuvent encore être ajustés.
     */
    @Column(name = "visible_client", nullable = false)
    @org.hibernate.annotations.ColumnDefault("false")
    @Builder.Default
    private Boolean visibleClient = false;
}
