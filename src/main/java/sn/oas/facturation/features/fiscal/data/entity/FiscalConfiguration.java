package sn.oas.facturation.features.fiscal.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "fiscal_configuration")
@Data
@NoArgsConstructor
public class FiscalConfiguration {
    /** Single system-wide configuration row; deliberately has no garage/tenant association. */
    @Id
    private Long id = 1L;

    @Column(nullable = false, precision = 7, scale = 4)
    private BigDecimal tvaRate = new BigDecimal("18.0000");

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal timbreFiscal = new BigDecimal("100.00");

}
