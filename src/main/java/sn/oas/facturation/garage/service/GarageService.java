package sn.oas.facturation.garage.service;

import sn.oas.facturation.garage.dto.GarageRequest;
import sn.oas.facturation.garage.dto.GarageResponse;
import sn.oas.facturation.garage.data.entity.Garage;

import java.util.List;

public interface GarageService {
    GarageResponse createGarage(GarageRequest request);
    GarageResponse updateGarage(Long id, GarageRequest request);
    GarageResponse getGarageById(Long id);
    List<GarageResponse> getAllGarages(boolean includeArchived);
    void deleteGarage(Long id);
    void restoreGarage(Long id);
    Garage getGarageEntityById(Long id);
}
