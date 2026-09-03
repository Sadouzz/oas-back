package sn.oas.facturation.features.dashboard.dto.response;

import lombok.Builder;

@Builder
public record EtatOrdreReparationDTO(
        long diagnostic,
        long attenteProforma,
        long proformaValide,
        long attentePieces,
        long attenteSortie,
        long enReparation,
        long attentePaiement,
        long termine,
        long totalActifs
) {}
