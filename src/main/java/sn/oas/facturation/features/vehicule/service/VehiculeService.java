package sn.oas.facturation.features.vehicule.service;

import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.dto.VehiculeRequest;

import java.util.List;

import org.springframework.data.domain.Page;

public interface VehiculeService {
    Page<Vehicule> getAllVehicules(int page, int size);
    Vehicule getVehiculeById(Long id);
    Vehicule createVehicule(VehiculeRequest request);
    Vehicule createVehicule(VehiculeRequest request, boolean actif);
    Vehicule updateVehicule(Long id, VehiculeRequest request);
    void deleteVehicule(Long id);
    List<Vehicule> searchVehicules(String keyword);
    Page<Vehicule> searchVehicules(String keyword, int page, int size);
    List<Vehicule> getVehiculesByClient(Long clientId);
    Page<Vehicule> getVehiculesByClient(Long clientId, int page, int size);
    List<Vehicule> getRecentVehicules();
    List<Vehicule> getVehiculesActifsByClient(Long clientId);
    void archiveVehiculeByClient(Long vehiculeId, Long clientId);
    Vehicule activerVehicule(Long vehiculeId);
}
