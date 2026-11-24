package sn.oas.facturation.features.vehicule.service;

import sn.oas.facturation.features.vehicule.data.enums.TypeHistoriqueVehicule;
import sn.oas.facturation.features.vehicule.dto.VehiculeHistoriqueResponseDto;

import java.time.LocalDate;

public interface VehiculeHistoriqueService {

    /**
     * Retourne l'historique complet d'un véhicule (infos véhicule/client, KPIs et événements filtrables/paginables).
     */
    VehiculeHistoriqueResponseDto getHistorique(
            Long vehiculeId,
            TypeHistoriqueVehicule type,
            String keyword,
            LocalDate dateDebut,
            LocalDate dateFin,
            Integer page,
            Integer size);
}
