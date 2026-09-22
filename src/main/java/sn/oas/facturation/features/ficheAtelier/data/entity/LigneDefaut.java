package sn.oas.facturation.features.ficheAtelier.data.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LigneDefaut implements Serializable {

    @JsonAlias({"rubrique", "label", "libelle", "titre", "name", "categorie"})
    private String nom;

    private Boolean present; // keep for backward compatibility

    @JsonAlias({"defaut", "description", "valeur", "details", "remarque", "constat", "commentaire", "observation", "texte"})
    private String designation;

    private Boolean archive;

    public LigneDefaut(String nom) {
        this.nom = nom;
        this.present = false;
        this.designation = "";
        this.archive = false;
    }

    @JsonSetter("description")
    public void setDescription(String description) {
        if (this.designation == null || this.designation.isBlank()) {
            this.designation = description;
        }
    }

    @JsonSetter("defaut")
    public void setDefaut(String defaut) {
        if (this.designation == null || this.designation.isBlank()) {
            this.designation = defaut;
        }
    }

    @JsonSetter("valeur")
    public void setValeur(String valeur) {
        if (this.designation == null || this.designation.isBlank()) {
            this.designation = valeur;
        }
    }

    @JsonProperty("description")
    public String getDescription() {
        return this.designation;
    }

    @JsonProperty("defaut")
    public String getDefaut() {
        return this.designation;
    }

    @JsonProperty("valeur")
    public String getValeur() {
        return this.designation;
    }
}
