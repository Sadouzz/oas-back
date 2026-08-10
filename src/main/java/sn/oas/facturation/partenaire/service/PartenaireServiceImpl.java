package sn.oas.facturation.partenaire.service;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import sn.oas.facturation.partenaire.data.entity.Partenaire;
import sn.oas.facturation.partenaire.data.repository.PartenaireRepository;
import sn.oas.facturation.partenaire.dto.PartenaireRequest;
import sn.oas.facturation.partenaire.dto.PartenaireResponse;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartenaireServiceImpl implements PartenaireService {

    private final PartenaireRepository partenaireRepository;

    private PartenaireResponse mapToResponse(Partenaire partenaire) {
        PartenaireResponse response = new PartenaireResponse();
        BeanUtils.copyProperties(partenaire, response);
        return response;
    }

    @Override
    public List<PartenaireResponse> getAllPartenaires() {
        return partenaireRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PartenaireResponse getPartenaireById(Long id) {
        Partenaire partenaire = partenaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));
        return mapToResponse(partenaire);
    }

    @Override
    public PartenaireResponse createPartenaire(PartenaireRequest request) {
        Partenaire partenaire = new Partenaire();
        BeanUtils.copyProperties(request, partenaire);
        return mapToResponse(partenaireRepository.save(partenaire));
    }

    @Override
    public PartenaireResponse updatePartenaire(Long id, PartenaireRequest request) {
        Partenaire partenaire = partenaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));
        partenaire.setNom(request.getNom());
        partenaire.setDescription(request.getDescription());
        partenaire.setLogo(request.getLogo());
        partenaire.setType(request.getType());
        partenaire.setArchived(request.isArchived());
        return mapToResponse(partenaireRepository.save(partenaire));
    }

    @Override
    public void deletePartenaire(Long id) {
        partenaireRepository.deleteById(id);
    }

    @Override
    public PartenaireResponse archivePartenaire(Long id) {
        Partenaire partenaire = partenaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));
        partenaire.setArchived(true);
        return mapToResponse(partenaireRepository.save(partenaire));
    }

    @Override
    public PartenaireResponse unarchivePartenaire(Long id) {
        Partenaire partenaire = partenaireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Partenaire introuvable"));
        partenaire.setArchived(false);
        return mapToResponse(partenaireRepository.save(partenaire));
    }
}
