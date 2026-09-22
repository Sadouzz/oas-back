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
public class LigneReception implements Serializable {

    @JsonAlias({"label", "libelle", "titre", "name", "designation"})
    private String nom;

    @JsonAlias({"valeur", "status", "statut", "conforme", "reponse", "checked", "present", "choix", "resultat"})
    private Boolean etat; // true = OUI, false = NON, null = non renseigné

    private Boolean archive;

    public LigneReception(String nom) {
        this.nom = nom;
        this.etat = null;
        this.archive = false;
    }

    public static Boolean parseEtat(Object value) {
        if (value == null) return null;
        if (value instanceof Boolean b) return b;
        if (value instanceof Number n) return n.intValue() == 1;
        String s = value.toString().trim().toLowerCase();
        if (s.equals("oui") || s.equals("true") || s.equals("1") || s.equals("ok") || s.equals("vrai")) {
            return true;
        }
        if (s.equals("non") || s.equals("false") || s.equals("0") || s.equals("faux")) {
            return false;
        }
        return null;
    }

    @JsonSetter("etat")
    public void setEtatFromRaw(Object value) {
        this.etat = parseEtat(value);
    }

    @JsonSetter("valeur")
    public void setValeur(Object value) {
        if (this.etat == null) {
            this.etat = parseEtat(value);
        }
    }

    @JsonSetter("statut")
    public void setStatut(Object value) {
        if (this.etat == null) {
            this.etat = parseEtat(value);
        }
    }

    @JsonSetter("status")
    public void setStatus(Object value) {
        if (this.etat == null) {
            this.etat = parseEtat(value);
        }
    }

    @JsonSetter("conforme")
    public void setConforme(Object value) {
        if (this.etat == null) {
            this.etat = parseEtat(value);
        }
    }

    @JsonSetter("reponse")
    public void setReponse(Object value) {
        if (this.etat == null) {
            this.etat = parseEtat(value);
        }
    }

    @JsonProperty("valeur")
    public String getValeur() {
        return this.etat == null ? null : (this.etat ? "OUI" : "NON");
    }

    @JsonProperty("statut")
    public String getStatut() {
        return this.etat == null ? null : (this.etat ? "OUI" : "NON");
    }

    @JsonProperty("etatStr")
    public String getEtatStr() {
        return this.etat == null ? null : (this.etat ? "OUI" : "NON");
    }

    @JsonProperty("oui")
    public Boolean isOui() {
        return Boolean.TRUE.equals(this.etat);
    }

    @JsonProperty("non")
    public Boolean isNon() {
        return Boolean.FALSE.equals(this.etat);
    }
}
