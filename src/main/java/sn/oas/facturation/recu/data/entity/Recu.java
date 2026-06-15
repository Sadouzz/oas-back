package sn.oas.facturation.recu.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.facture.data.entity.Facture;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recus")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facture_id", nullable = false)
    private Facture facture;

    @Column(name = "montant_paye", nullable = false, precision = 15, scale = 2)
    private BigDecimal montantPaye;

    @Column(name = "date_paiement", nullable = false)
    private LocalDateTime datePaiement;

    @Column(name = "methode_paiement")
    private String methodePaiement;
}
