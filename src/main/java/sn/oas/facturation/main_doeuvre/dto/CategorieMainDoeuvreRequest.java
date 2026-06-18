package sn.oas.facturation.main_doeuvre.dto;

import jakarta.validation.constraints.NotBlank;

public record CategorieMainDoeuvreRequest(
        @NotBlank(message = "Le nom de la catégorie est obligatoire")
        String nom) {
}
