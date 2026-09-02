package sn.oas.facturation.features.partenaire.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.partenaire.data.entity.Partenaire;
import sn.oas.facturation.features.partenaire.dto.PartenaireRequest;

import java.util.List;

public interface PartenaireService {
    Page<Partenaire> getAllPartenaires(int page, int size);
    List<Partenaire> getAllPartenaires();
    Partenaire getPartenaireById(Long id);
    Partenaire createPartenaire(PartenaireRequest request);
    Partenaire updatePartenaire(Long id, PartenaireRequest request);
    void deletePartenaire(Long id);
    Partenaire archivePartenaire(Long id);
    Partenaire unarchivePartenaire(Long id);
}
