package sn.oas.facturation.garage.service;

import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.dto.GarageRequest;

import java.util.List;
import java.util.Optional;

public interface GarageService {
    Garage createGarage(GarageRequest request);
    List<Garage> getAllGarages();
    Optional<Garage> getGarageById(Long id);
    Garage updateGarage(Long id, GarageRequest request);
    void deleteGarage(Long id);
}
