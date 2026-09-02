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

@Entity
@Table(name = "proformas")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Proforma extends FactureTTC {

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
