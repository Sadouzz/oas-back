package sn.oas.facturation.features.ficheAtelier.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneReception implements Serializable {
    private String nom;
    private Boolean etat; // true = OUI, false = NON, null = non renseigné
}
