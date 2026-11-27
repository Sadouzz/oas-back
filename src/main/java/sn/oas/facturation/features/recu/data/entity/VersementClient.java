package sn.oas.facturation.features.recu.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Versement d'un client sur son reçu (équivalent de detail_recu_client de l'ancienne application).
 * Le montant est réparti sur les documents impayés du reçu, des plus anciens aux plus récents.
 */
@Entity
@Table(name = "versements_client")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class VersementClient implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recu_id", nullable = false)
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Recu recu;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "mode_paiement", length = 40)
    private String modePaiement;

    private String remarque;

    @Column(name = "date_versement", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime dateVersement;

    @OneToMany(mappedBy = "versement", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    @Builder.Default
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private List<LigneVersementClient> lignes = new ArrayList<>();

    public void addLigne(LigneVersementClient ligne) {
        ligne.setVersement(this);
        lignes.add(ligne);
    }
}
