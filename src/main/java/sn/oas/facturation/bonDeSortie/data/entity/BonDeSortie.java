package sn.oas.facturation.bonDeSortie.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.bonDeSortie.data.enums.StatutBon;

import sn.oas.facturation.vehicule.data.entity.Vehicule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "bons_de_sortie")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BonDeSortie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false, updatable = false)
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
    @JoinColumn(name = "fiche_atelier_id", nullable = false, unique = true)
    @JsonIgnoreProperties("bonDeSortie") // Ignore le champ "bonDeSortie" qui est DANS la "FicheAtelier"
    private FicheAtelier ficheAtelier;

    @OneToMany(mappedBy = "bonDeSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneBonDeSortiePiece> lignesBonDeSortiePieces = new ArrayList<>();
    
    @OneToMany(mappedBy = "bonDeSortie", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneBonDeSortieMainDoeuvre> lignesBonDeSortieMainDoeuvres = new ArrayList<>();


    

    @PrePersist
    protected void onCreate() {
        if (this.date == null) {
            this.date = LocalDateTime.now();
        }
    }
}