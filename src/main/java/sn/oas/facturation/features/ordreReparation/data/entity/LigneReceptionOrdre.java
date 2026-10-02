package sn.oas.facturation.features.ordreReparation.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
public class LigneReceptionOrdre implements Serializable {
    private String nom;
    private Boolean etat; // true = OUI, false = NON, null = non renseigné
    private Boolean verrouille;

    @JsonSetter("valeur")
    public void setValeur(Object value) {
        if (this.etat == null && value != null) {
            this.etat = "OUI".equalsIgnoreCase(value.toString()) || "true".equalsIgnoreCase(value.toString());
        }
    }

    @JsonSetter("statut")
    public void setStatut(Object value) {
        if (this.etat == null && value != null) {
            this.etat = "OUI".equalsIgnoreCase(value.toString()) || "true".equalsIgnoreCase(value.toString());
        }
    }

    public String getValeur() {
        return this.etat == null ? null : (this.etat ? "OUI" : "NON");
    }

    public String getStatut() {
        return this.etat == null ? null : (this.etat ? "OUI" : "NON");
    }

    public Boolean isOui() {
        return Boolean.TRUE.equals(this.etat);
    }

    public Boolean isNon() {
        return Boolean.FALSE.equals(this.etat);
    }
}
