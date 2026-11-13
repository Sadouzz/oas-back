package sn.oas.facturation.features.clientportal.dto;

import java.time.LocalDateTime;

public record ReservationBookingStatusDTO(
        boolean workflowAutorise,
        boolean plafondAtteint,
        boolean factureEchueImpayee,
        boolean demandeDerogationPossible,
        boolean demandeEnAttente,
        String motifBlocage,
        LocalDateTime dateDemandeEnAttente
) {}
