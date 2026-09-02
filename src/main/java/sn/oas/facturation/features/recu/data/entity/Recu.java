package sn.oas.facturation.features.recu.data.entity;

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
import sn.oas.facturation.features.facture.data.entity.Facture;

import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(TenantListener.class)
@FilterDef(name = "garageFilter", parameters = @ParamDef(name = "garageId", type = Long.class))
@Filter(name = "garageFilter", condition = "garage_id = :garageId")
public class Recu implements TenantAware  {

    @ManyToOne(fetch = jakarta.persistence.FetchType.LAZY)
    @jakarta.persistence.JoinColumn(name = "garage_id")
    private Garage garage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "mode_paiement")
    private String modePaiement; // ESPECE, CHEQUE, VIREMENT...

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime datePaiement;

    private String remarque;
}

