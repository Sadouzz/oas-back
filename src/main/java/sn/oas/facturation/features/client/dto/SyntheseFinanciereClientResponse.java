package sn.oas.facturation.features.client.dto;

import java.math.BigDecimal;
import java.util.List;

public record SyntheseFinanciereClientResponse(
        Long clientId,
        Integer echeanceJours,
        BigDecimal plafondEncours,
        BigDecimal encoursImpayes,
        BigDecimal montantDisponible,
        boolean conditionsConfigurees,
        boolean plafondAtteint,
        boolean facturesEnRetard,
        boolean derogationEnAttente,
        boolean nouvelleDemandeAutorisee,
        String motifBlocage,
        List<FactureEcheanceClientResponse> facturesBloquantes,
        List<FactureEcheanceClientResponse> prochainesEcheances) { }
