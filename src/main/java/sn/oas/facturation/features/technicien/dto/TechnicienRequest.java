package sn.oas.facturation.features.technicien.dto;

import lombok.Data;
import sn.oas.facturation.features.auth.data.enums.Specialite;

/**
 * DTO de création/mise à jour d'un compte Technicien depuis l'écran staff
 * gestion/techniciens. Le mot de passe n'est requis qu'à la création.
 */
@Data
public class TechnicienRequest {
    private String username;
    private String password;
    private String phone;
    private String firstName;
    private String lastName;
    private String email;
    private String adresse;
    private Specialite specialite;
    private Long garageId;
}
