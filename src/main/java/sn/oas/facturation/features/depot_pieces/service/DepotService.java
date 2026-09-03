package sn.oas.facturation.features.depot_pieces.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.depot_pieces.data.entity.Depot;
import sn.oas.facturation.features.depot_pieces.dto.request.DepotRequest;

import java.util.List;

public interface DepotService {
    Depot createDepot(DepotRequest request);
    Page<Depot> getAllDepots(int page, int size);
    List<Depot> getAllDepots();
    Depot getDepotById(Long id);
    Depot updateDepot(Long id, DepotRequest request);
    void deleteDepot(Long id);
    List<Depot> searchDepots(String keyword);
    Page<Depot> searchDepots(String keyword, int page, int size);
}
