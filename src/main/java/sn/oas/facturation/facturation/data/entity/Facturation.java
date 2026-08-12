package sn.oas.facturation.facturation.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.garage.data.entity.Garage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.ordreReparation.data.entity.OrdreReparation;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List; 

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public abstract class Facturation implements TenantAware  {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "garage_id")
    private Garage garage;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "facturation_seq")
    @SequenceGenerator(name = "facturation_seq", sequenceName = "facturation_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false)
    private String numero;

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime dateModification;

    @Column(nullable = false)
    private BigDecimal montantTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent; // Agent qui a crÃ©Ã© la facture

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_commande_id")
    private BonDeCommande bonDeCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("facturations")
    private OrdreReparation ordreReparation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutFacturation statut = StatutFacturation.EN_ATTENTE;

    @Column(columnDefinition = "TEXT", nullable = true)
    private String remarque;

    @Column(nullable = false)
    private Double kilometrage;
    
    @OneToMany(mappedBy = "facturation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("facturation")
    private List<LigneFacturationPiece> lignesFacturationPieces = new ArrayList<>();
    
    @OneToMany(mappedBy = "facturation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("facturation")
    private List<LigneFacturationMainDoeuvre> lignesFacturationMainDoeuvres = new ArrayList<>();



    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
        if (this.dateModification == null) {
            this.dateModification = LocalDateTime.now();
        }
    }
}
