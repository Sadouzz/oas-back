package sn.oas.facturation.ficheAtelier.data.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LigneDefaut implements Serializable {
    private String nom;
    private Boolean present; // keep for backward compatibility
    private String designation;
}
