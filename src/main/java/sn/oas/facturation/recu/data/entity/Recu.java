package sn.oas.facturation.recu.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import sn.oas.facturation.facture.data.entity.Facture;

import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "recus")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recu {

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
