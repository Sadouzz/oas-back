package sn.oas.facturation.features.proforma.dto;

import java.math.BigDecimal;

public record ProformaEmailDecisionInfoResponse(
        String numero,
        String action,
        BigDecimal montantTotal
) { }
