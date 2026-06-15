package sn.oas.facturation.ficheAtelier.dto;

import lombok.Data;
import java.time.LocalDateTime;
import sn.oas.facturation.ficheAtelier.data.enums.StatutReparation;

@Data
public class FicheAtelierRequest {
    private String numero;
    private String descriptionTravaux;
    private String listeReception;
    private String listeDefauts;
    private LocalDateTime dateSortie;
    private Long vehiculeId;
    private StatutReparation statut;
}
