package sn.oas.facturation.features.partenaire.service;

import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import sn.oas.facturation.features.partenaire.data.entity.Partenaire;
import sn.oas.facturation.features.partenaire.dto.PartenaireRequest;
import sn.oas.facturation.features.partenaire.dto.PartenaireResponse;
import sn.oas.facturation.features.partenaire.repository.PartenaireRepository;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PartenaireServiceImpl implements PartenaireService {

    private final PartenaireRepository partenaireRepository;

    @Override
    public org.springframework.data.domain.Page<Partenaire> getAllPartenaires(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return partenaireRepository.findAll(pageable);
    }

    @Override
    public List<Partenaire> getAllPartenaires() {
        return partenaireRepository.findAll();
    }

    @Override
    public Partenaire getPartenaireById(Long id) {
        return partenaireRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Partenaire non trouvé avec l'id : " + id));
    }

    @Override
    public Partenaire createPartenaire(PartenaireRequest request) {
        Partenaire partenaire = new Partenaire();
        BeanUtils.copyProperties(request, partenaire);
        return partenaireRepository.save(partenaire);
    }

    @Override
    public Partenaire updatePartenaire(Long id, PartenaireRequest request) {
        Partenaire partenaire = partenaireRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Partenaire non trouvé avec l'id : " + id));
        partenaire.setNom(request.getNom());
        partenaire.setDescription(request.getDescription());
        partenaire.setLogo(request.getLogo());
        partenaire.setType(request.getType());
        partenaire.setArchived(request.isArchived());
        return partenaireRepository.save(partenaire);
    }

    @Override
    public void deletePartenaire(Long id) {
        if (!partenaireRepository.existsById(id)) {
            throw new sn.oas.facturation.shared.exception.ResourceNotFoundException("Partenaire non trouvé avec l'id : " + id);
        }
        partenaireRepository.deleteById(id);
    }

    @Override
    public Partenaire archivePartenaire(Long id) {
        Partenaire partenaire = partenaireRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Partenaire non trouvé avec l'id : " + id));
        partenaire.setArchived(true);
        return partenaireRepository.save(partenaire);
    }

    @Override
    public Partenaire unarchivePartenaire(Long id) {
        Partenaire partenaire = partenaireRepository.findById(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Partenaire non trouvé avec l'id : " + id));
        partenaire.setArchived(false);
        return partenaireRepository.save(partenaire);
    }
}
