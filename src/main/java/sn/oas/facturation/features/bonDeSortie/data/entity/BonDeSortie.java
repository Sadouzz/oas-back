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
import lombok.NoArgsConstructor;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.entity.Client;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;

import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

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
public class BonDeSortie implements TenantAware  {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "garage_id")
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
    private Client client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_emetteur_id", nullable = false)
    private Agent agentEmetteur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_validateur_id")
    private Agent agentValidateur;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = true, unique = true)
    @JsonIgnoreProperties("bonDeSortie") // Ignore le champ "bonDeSortie" qui est DANS la "OrdreReparation"
    private OrdreReparation ordreReparation;

    @OneToMany(mappedBy = "bonDeSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneBonDeSortiePiece> lignesBonDeSortiePieces = new ArrayList<>();
    

    

    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }
}
