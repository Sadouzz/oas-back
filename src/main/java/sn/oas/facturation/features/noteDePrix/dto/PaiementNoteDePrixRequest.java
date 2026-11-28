package sn.oas.facturation.features.noteDePrix.dto;

import java.math.BigDecimal;

public record PaiementNoteDePrixRequest(BigDecimal montant, String modePaiement) {
}
