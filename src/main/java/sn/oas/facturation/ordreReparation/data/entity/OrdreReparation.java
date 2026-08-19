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

    // Travaux demandés — liste de lignes {nom, verrouille}. La ligne issue de
    // FicheAtelier.designationTravaux (via createFromFicheAtelier) est verrouillée,
    // non modifiable/supprimable côté UI ; le chef d'atelier peut en ajouter d'autres.
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "lignes_travaux", columnDefinition = "jsonb")
    private List<LigneTravailOrdre> lignesTravaux;

    // Réception : liste de lignes {nom, etat}, comme sur FicheAtelier.lignesReception.
    // Les lignes provenant de la fiche atelier d'origine (via createFromFicheAtelier)
    // sont marquées verrouille=true : non modifiables/supprimables côté UI, seule
    // l'ajout de nouvelles lignes (verrouille=false) est permis.
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(name = "lignes_reception", columnDefinition = "jsonb")
    private List<LigneReceptionOrdre> lignesReception;

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

    @org.hibernate.annotations.Formula("((SELECT COUNT(*) FROM lignes_ordre_reparation_piece p WHERE p.ordre_reparation_id = id) > 0 OR (SELECT COUNT(*) FROM lignes_ordre_reparation_main_doeuvre m WHERE m.ordre_reparation_id = id) > 0)")
    private Boolean hasPiecesOrMo;

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

