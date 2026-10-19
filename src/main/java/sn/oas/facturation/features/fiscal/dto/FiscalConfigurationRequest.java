package sn.oas.facturation.features.fiscal.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record FiscalConfigurationRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal tvaRate,
        @NotNull @DecimalMin("0.0") @DecimalMax("100000000.0") BigDecimal timbreFiscal) {
}
