package sn.oas.facturation.facturation.data.entity;

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
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
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
public abstract class Facturation {

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
    private Agent agent; // Agent qui a créé la facture

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bon_de_commande_id")
    private BonDeCommande bonDeCommande;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_atelier_id")
    private FicheAtelier ficheAtelier;

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
    private List<LigneFacturationPiece> lignesFacturationPieces = new ArrayList<>();
    
    @OneToMany(mappedBy = "facturation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
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