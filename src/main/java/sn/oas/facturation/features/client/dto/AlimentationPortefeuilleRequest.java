package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record AlimentationPortefeuilleRequest(
        @DecimalMin(value = "0.01", message = "Le montant doit être supérieur à zéro.")
        @Digits(integer = 13, fraction = 2) BigDecimal montant,
        @Size(max = 40) String modePaiement,
        @Size(max = 500) String commentaire) { }
