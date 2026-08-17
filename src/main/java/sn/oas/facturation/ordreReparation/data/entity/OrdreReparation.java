package sn.oas.facturation.ordreReparation.data.entity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;
import sn.oas.facturation.garage.data.entity.Garage;

import sn.oas.facturation.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.facturation.data.entity.Facturation;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.auth.data.entity.Technicien;
import sn.oas.facturation.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "ordres_reparation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class OrdreReparation implements TenantAware  {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "garage_id")
    private Garage garage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Column(columnDefinition = "TEXT")
    private String descriptionTravaux;

    // Travaux demandés — champ texte libre distinct, même fonctionnement que
    // FicheAtelier.designationTravaux (voir spec point 2 : peut être pré-rempli
    // depuis une FicheAtelier liée, ou saisi/complété librement sur l'ordre de réparation).
    @Column(columnDefinition = "TEXT")
    private String travauxDemandes;

    // Réception : simple récapitulatif texte libre (désignation), plus une liste de
    // checkboxes. Le nom de colonne "liste_reception" est conservé pour ne pas casser
    // les données existantes ; c'est le front qui n'affiche plus qu'un champ désignation.
    @Column(columnDefinition = "TEXT")
    private String listeReception;

    @Column(columnDefinition = "TEXT")
    private String listeDefauts;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    @CreationTimestamp
    private LocalDateTime dateCreation = LocalDateTime.now();

    @Column(name = "update_at", nullable = false)
    @Builder.Default
    @UpdateTimestamp
    private LocalDateTime updatedAt = LocalDateTime.now();

    private LocalDateTime dateSortie;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut")
    @Builder.Default
    private StatutOrdreReparation statut = StatutOrdreReparation.A_FAIRE;

    // â”€â”€ Relationship Block â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicule_id", nullable = false)
    private Vehicule vehicule;

    // Lien optionnel vers la Fiche Atelier d'origine (voir spec point 8). Nullable :
    // un ordre de réparation peut aussi être créé directement, sans fiche atelier.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiche_atelier_id")
    @JsonIgnoreProperties({ "vehicule", "client", "garage" })
    private FicheAtelier ficheAtelier;

    // Noms de table/colonne de jointure conservés tels quels (fiche_mecaniciens(_reparation),
    // mecanicien_id) pour limiter l'ampleur du changement de schéma lors du remplacement de
    // Mecanicien par Technicien — voir rapport de la tâche. Seul le type Java référencé change.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "fiche_mecaniciens",
        joinColumns = @JoinColumn(name = "fiche_id"),
        inverseJoinColumns = @JoinColumn(name = "mecanicien_id")
    )
    @Builder.Default
    @JsonIgnoreProperties({ "ordresReparation", "password", "authorities", "garage" })
    private List<Technicien> techniciens = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "fiche_mecaniciens_reparation",
        joinColumns = @JoinColumn(name = "fiche_id"),
        inverseJoinColumns = @JoinColumn(name = "mecanicien_id")
    )
    @Builder.Default
    @JsonIgnoreProperties({ "ordresReparation", "password", "authorities", "garage" })
    private List<Technicien> techniciensReparation = new ArrayList<>();

    @OneToOne(mappedBy = "ordreReparation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("ordreReparation") // Ignore le champ "ordreReparation" qui est DANS le "BonDeSortie"
    private BonDeSortie bonDeSortie;

    @OneToMany(mappedBy = "ordreReparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<Facturation> facturations = new ArrayList<>();

    @OneToMany(mappedBy = "ordreReparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneOrdreReparationPiece> lignesOrdreReparationPieces = new ArrayList<>();

    @OneToMany(mappedBy = "ordreReparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<LigneOrdreReparationMainDoeuvre> lignesOrdreReparationMainDoeuvres = new ArrayList<>();

    @OneToMany(mappedBy = "ordreReparation", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @com.fasterxml.jackson.annotation.JsonIgnore
    private List<PieceJointeDiagnostic> piecesJointesDiagnostic = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.dateCreation == null) {
            this.dateCreation = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.statut == null) {
            this.statut = StatutOrdreReparation.A_FAIRE;
        }
    }
}

