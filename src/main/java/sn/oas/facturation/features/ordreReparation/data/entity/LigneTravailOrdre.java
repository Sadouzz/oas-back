package sn.oas.facturation.features.ordreReparation.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Ligne de la section "Travaux demandés" d'un {@link OrdreReparation}. Les lignes sont fixées
 * lors de la création de l'ordre et ne peuvent plus être modifiées ensuite. {@code verrouille}
 * distingue les travaux importés de la fiche atelier dans l'écran de création.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LigneTravailOrdre implements Serializable {
    private String nom;
    private Boolean verrouille;
}
