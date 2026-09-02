package sn.oas.facturation.features.fournisseur.dto;

import lombok.Data;

@Data
public class FournisseurRequest {

    private String matricule;

    private String nomEntreprise;

    private String nom;

    private String prenom;
}
