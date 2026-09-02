package sn.oas.facturation.features.garage.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.dto.GarageRequest;

import java.util.List;

public interface GarageService {
    Garage createGarage(GarageRequest request);
    Garage updateGarage(Long id, GarageRequest request);
    Garage getGarageById(Long id);
    Page<Garage> getAllGarages(boolean includeArchived, int page, int size);
    List<Garage> getAllGarages(boolean includeArchived);
    void deleteGarage(Long id);
    void restoreGarage(Long id);
    Garage getGarageEntityById(Long id);
}
