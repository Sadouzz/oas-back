package sn.oas.facturation.features.technicien.service;

import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.technicien.dto.TechnicienRequest;

import org.springframework.data.domain.Page;
import java.util.List;
import java.util.Optional;

public interface TechnicienService {
    Page<Technicien> getAllTechniciens(int page, int size);
    List<Technicien> getAllTechniciens();
    Optional<Technicien> getTechnicienById(Long id);
    void createTechnicien(TechnicienRequest request);
    Technicien updateTechnicien(Long id, TechnicienRequest request);
    void deleteTechnicien(Long id);
    List<Technicien> searchTechniciens(String keyword);
    Page<Technicien> searchTechniciens(String keyword, int page, int size);

    /** Technicien actuellement authentifié (portail technicien). */
    Technicien getTechnicienConnecte();
}
