package sn.oas.facturation.features.noteDePrix.data.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.shared.tenant.TenantAware;
import sn.oas.facturation.shared.tenant.TenantListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "paiements_notes_de_prix", indexes = {
        @Index(name = "idx_paiement_note_date", columnList = "note_de_prix_id,date_paiement")
})
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class PaiementNoteDePrix implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_de_prix_id", nullable = false, updatable = false)
    private NoteDePrix noteDePrix;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "garage_id", nullable = false, updatable = false)
    @Setter
    private Garage garage;

    @Column(nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal montant;

    @Column(name = "mode_paiement", length = 40, updatable = false)
    private String modePaiement;

    @Column(name = "acteur_id", updatable = false)
    private Long acteurId;

    @CreationTimestamp
    @Column(name = "date_paiement", nullable = false, updatable = false)
    private LocalDateTime datePaiement;
}
