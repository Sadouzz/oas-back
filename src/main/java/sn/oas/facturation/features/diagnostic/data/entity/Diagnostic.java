package sn.oas.facturation.features.diagnostic.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
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
import java.util.List;

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
    private Technicien technicien;

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

    // Pièces jointes & remarques
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "diagnostic_id")
    @Builder.Default
    private List<PieceJointeDiagnostic> piecesJointes = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "diagnostic_id")
    @Builder.Default
    private List<RemarqueDiagnostic> remarques = new ArrayList<>();
}
