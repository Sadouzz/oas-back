package sn.oas.facturation.features.clientportal.dto;

import java.math.BigDecimal;

public record ClientStatsDTO(
        long devisEnAttente,
        long proformasEnAttente,
        long rdvAVenir,
        BigDecimal montantDu
) {}
