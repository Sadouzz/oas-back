package sn.oas.facturation.features.bonDeSortie.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "bons_de_sortie_historique")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class BonDeSortieHistorique implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_sortie_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"lignesBonDeSortiePieces", "ordreReparation"})
    private BonDeSortie bonDeSortie;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "piece_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"categorie", "depot"})
    private PDP piece;

    private Double quantite;

    private Double stockMagasin;

    private Double stockAtelier;

    private Double qteReelle;

    private String prenom;

    private String nom;

    @Column(name = "num_bs")
    private String numBs;

    @Column(name = "numero_serie")
    private String numeroSerie;

    private String immatriculation;

    private String designation;

    @Column(nullable = false)
    private String statut;

    private String motif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @CreationTimestamp
    @Column(name = "date_action", nullable = false, updatable = false)
    private LocalDateTime dateAction;

    @PrePersist
    protected void onCreate() {
        if (this.dateAction == null) {
            this.dateAction = LocalDateTime.now();
        }
    }
}
