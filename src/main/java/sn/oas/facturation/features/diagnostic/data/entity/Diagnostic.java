package sn.oas.facturation.features.diagnostic.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.shared.entity.BaseEntity;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity 
@Table(name = "diagnostics")
@Data 
@EqualsAndHashCode(callSuper = false)
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class Diagnostic extends BaseEntity implements TenantAware {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "garage_id")
    private Garage garage;

    // Relation avec l'Ordre de Réparation
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ordre_reparation_id", nullable = false, unique = true)
    @JsonIgnoreProperties({ "diagnostic", "vehicule", "garage", "bonDeSortie" })
    private OrdreReparation ordreReparation;

    // Technicien responsable du diagnostic
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technicien_id")
    @Deprecated
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Technicien technicien;

    /** Techniciens réellement affectés au diagnostic. Le champ technicien ci-dessus
     * reste temporairement comme référent historique pour les consommateurs legacy. */
    @ManyToMany(fetch = FetchType.LAZY)
    @BatchSize(size = 50)
    @JoinTable(name = "diagnostic_techniciens",
            joinColumns = @JoinColumn(name = "diagnostic_id"),
            inverseJoinColumns = @JoinColumn(name = "technicien_id"))
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Technicien> techniciens = new LinkedHashSet<>();

    // Contenu et observations du diagnostic
    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(columnDefinition = "TEXT")
    private String pannesDetectees;

    @Column(columnDefinition = "TEXT")
    private String recommandations;

    // Kilométrage relevé lors du diagnostic
    private Integer kilometrage;

    // Statut du diagnostic (ex: EN_COURS, TERMINE, VALIDE)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutDiagnostic statut = StatutDiagnostic.EN_COURS;

    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;

    public void remplacerTechniciens(Collection<Technicien> nouveauxTechniciens) {
        techniciens.clear();
        if (nouveauxTechniciens != null) {
            techniciens.addAll(nouveauxTechniciens);
        }
        technicien = techniciens.stream().findFirst().orElse(null);
    }

    public void ajouterTechnicien(Technicien technicien) {
        if (technicien == null) return;
        if (techniciens.isEmpty() && this.technicien != null) techniciens.add(this.technicien);
        techniciens.add(technicien);
        if (this.technicien == null) this.technicien = technicien;
    }

    public void retirerTechnicien(Long technicienId) {
        if (technicienId == null) return;
        if (techniciens.isEmpty() && this.technicien != null) techniciens.add(this.technicien);
        techniciens.removeIf(t -> technicienId.equals(t.getId()));
        if (this.technicien != null && technicienId.equals(this.technicien.getId())) {
            this.technicien = techniciens.stream().findFirst().orElse(null);
        }
    }

    // Pièces jointes & remarques
    @OneToMany(mappedBy = "diagnostic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PieceJointeDiagnostic> piecesJointes = new ArrayList<>();

    @OneToMany(mappedBy = "diagnostic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RemarqueDiagnostic> remarques = new ArrayList<>();
}
