package sn.oas.facturation.features.client.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RemiseClientRequest(@NotNull @Min(0) @Max(100) Integer montantRemise) { }
