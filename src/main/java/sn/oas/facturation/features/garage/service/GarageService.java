package sn.oas.facturation.features.garage.service;

import sn.oas.facturation.features.garage.dto.GarageRequest;
import sn.oas.facturation.features.garage.dto.GarageResponse;
import sn.oas.facturation.features.garage.data.entity.Garage;

import org.springframework.data.domain.Page;
import java.util.List;

public interface GarageService {
    GarageResponse createGarage(GarageRequest request);
    GarageResponse updateGarage(Long id, GarageRequest request);
    GarageResponse getGarageById(Long id);
    Page<GarageResponse> getAllGarages(boolean includeArchived, int page, int size);
    List<GarageResponse> getAllGarages(boolean includeArchived);
    void deleteGarage(Long id);
    void restoreGarage(Long id);
    Garage getGarageEntityById(Long id);
}
