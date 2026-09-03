package sn.oas.facturation.features.depot_pieces.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.depot_pieces.data.entity.Depot;
import sn.oas.facturation.features.depot_pieces.dto.request.DepotRequest;
import sn.oas.facturation.features.depot_pieces.repository.DepotRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DepotServiceImpl implements DepotService {

    private final DepotRepository depotRepository;

    @Override
    public Depot createDepot(DepotRequest request) {
        Depot depot = Depot.builder()
                .nom(request.nom())
                .description(request.description())
                .build();
        return depotRepository.save(depot);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Depot> getAllDepots(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return depotRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Depot> getAllDepots() {
        return depotRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Depot getDepotById(Long id) {
        return depotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dépôt non trouvé avec l'id : " + id));
    }

    @Override
    public Depot updateDepot(Long id, DepotRequest request) {
        Depot depot = getDepotById(id);
        depot.setNom(request.nom());
        depot.setDescription(request.description());
        return depotRepository.save(depot);
    }

    @Override
    public void deleteDepot(Long id) {
        if (!depotRepository.existsById(id)) {
            throw new RuntimeException("Dépôt non trouvé avec l'id : " + id);
        }
        depotRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Depot> searchDepots(String keyword) {
        return depotRepository.searchDepots(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Depot> searchDepots(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return depotRepository.searchDepots(keyword, pageable);
    }
}
