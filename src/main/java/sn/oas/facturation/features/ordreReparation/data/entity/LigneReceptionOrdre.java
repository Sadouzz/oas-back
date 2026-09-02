package sn.oas.facturation.features.ordreReparation.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Ligne de la section "Réception" d'un {@link OrdreReparation}. Les lignes issues de la
 * fiche atelier d'origine sont marquées {@code verrouille = true} : le chef d'atelier ne
 * peut pas modifier leur désignation ni les supprimer (lecture seule côté UI), il peut
 * seulement en ajouter de nouvelles (verrouille = false) via le bouton "Ajouter une ligne".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneReceptionOrdre implements Serializable {
    private String nom;
    private Boolean etat; // true = OUI, false = NON, null = non renseigné
    private Boolean verrouille;
}
