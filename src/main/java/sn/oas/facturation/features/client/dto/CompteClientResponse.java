package sn.oas.facturation.features.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CompteClientResponse(
        Long clientId, String typeClient, String raisonSociale, String ninea,
        boolean compteExiste, boolean compteActif, BigDecimal remisePourcentage, BigDecimal soldeCredit,
        BigDecimal plafondCredit, Integer echeanceJours, BigDecimal plafondPeriode,
        BigDecimal encoursImpayes, BigDecimal facturePeriode,
        boolean facturesEnRetard, boolean reservationClientAutorisee,
        String motifBlocageReservation, String rccm, String rib,
        LocalDateTime compteCreeLe) { }
