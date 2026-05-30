package sn.oas.facturation.vehicule.service;

import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.dto.VehiculeRequest;

import java.util.List;
import java.util.Optional;

public interface VehiculeService {
    List<Vehicule> getAllVehicules();
    Vehicule getVehiculeById(Long id);
    Vehicule createVehicule(VehiculeRequest request);
    Vehicule updateVehicule(Long id, VehiculeRequest request);
    void deleteVehicule(Long id);
    List<Vehicule> searchVehicules(String keyword);
    List<Vehicule> getVehiculesByClient(Long clientId);
    List<Vehicule> getRecentVehicules();
}
