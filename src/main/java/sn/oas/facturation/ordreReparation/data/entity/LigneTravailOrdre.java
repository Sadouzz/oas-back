package sn.oas.facturation.ordreReparation.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Ligne de la section "Travaux demandés" d'un {@link OrdreReparation}. La ligne issue de la
 * désignation des travaux de la fiche atelier d'origine est marquée {@code verrouille = true} :
 * non modifiable/supprimable côté UI. Le chef d'atelier peut ajouter d'autres lignes
 * (verrouille = false) via le bouton "Ajouter une ligne".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneTravailOrdre implements Serializable {
    private String nom;
    private Boolean verrouille;
}
