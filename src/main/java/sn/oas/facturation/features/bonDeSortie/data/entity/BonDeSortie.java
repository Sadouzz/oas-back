package sn.oas.facturation.features.bonDeSortie.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.features.garage.data.entity.Garage;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "bons_de_sortie")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BonDeSortie implements TenantAware  {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "garage_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Garage garage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutBon statut = StatutBon.EN_ATTENTE;

    private String remarque;

    @Column(name = "date_validation")
    private LocalDateTime dateValidation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_emetteur_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Agent agentEmetteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_validateur_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Agent agentValidateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = true)
    @JsonIgnoreProperties({"bonsDeSortie", "hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude

    private OrdreReparation ordreReparation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_travail_id")
    @JsonIgnoreProperties({"bonsDeSortie", "hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrdreTravail ordreTravail;

    @Column(name = "montant_total_pieces", precision = 15, scale = 2)
    private BigDecimal montantTotalPieces;

    @Column(name = "montant_total_main_doeuvre", precision = 15, scale = 2)
    private BigDecimal montantTotalMainDoeuvre;

    @Column(name = "montant_total", precision = 15, scale = 2)
    private BigDecimal montantTotal;

    @Column(name = "tva_applicable")
    private Boolean tvaApplicable;

    @Column(name = "montant_tva", precision = 15, scale = 2)
    private BigDecimal montantTVA;

    @Column(name = "montant_ttc", precision = 15, scale = 2)
    private BigDecimal montantTTC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proforma_id", nullable = true)
    @JsonIgnoreProperties({"bonsDeSortie", "hibernateLazyInitializer", "handler"})
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Proforma proforma;

    @OneToMany(mappedBy = "bonDeSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneBonDeSortiePiece> lignesBonDeSortiePieces = new ArrayList<>();

    @OneToMany(mappedBy = "bonDeSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneBonDeSortieMainDoeuvre> lignesBonDeSortieMainDoeuvres = new ArrayList<>();

    public void addLignePiece(LigneBonDeSortiePiece ligne) {
        if (this.lignesBonDeSortiePieces == null) {
            this.lignesBonDeSortiePieces = new ArrayList<>();
        }
        ligne.setBonDeSortie(this);
        this.lignesBonDeSortiePieces.add(ligne);
    }

    public void addLigneMainDoeuvre(LigneBonDeSortieMainDoeuvre ligne) {
        if (this.lignesBonDeSortieMainDoeuvres == null) {
            this.lignesBonDeSortieMainDoeuvres = new ArrayList<>();
        }
        ligne.setBonDeSortie(this);
        this.lignesBonDeSortieMainDoeuvres.add(ligne);
    }

    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }
}
