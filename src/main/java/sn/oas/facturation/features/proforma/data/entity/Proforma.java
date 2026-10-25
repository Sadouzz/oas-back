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
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.ArrayList;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import java.math.BigDecimal;

@Entity
@Table(name = "proformas")
@Data
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Proforma extends FactureTTC {



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

    @OneToMany(mappedBy = "proforma", fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"proforma", "hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<BonDeSortie> bonsDeSortie = new ArrayList<>();

    public BonDeSortie getBs() {
        if (bonsDeSortie == null || bonsDeSortie.isEmpty()) {
            return null;
        }
        return bonsDeSortie.get(bonsDeSortie.size() - 1);
    }

    /** TTC métier : HT + TVA + timbre, recalculé à partir des montants historiques du document. */
    public BigDecimal calculerMontantTtcAvecTimbre() {
        return (getMontantHT() == null ? BigDecimal.ZERO : getMontantHT())
                .add(getMontantTVA() == null ? BigDecimal.ZERO : getMontantTVA())
                .add(getMontantTimbre() == null ? BigDecimal.ZERO : getMontantTimbre());
    }
}
