package sn.oas.facturation.technicien.service;

import sn.oas.facturation.auth.data.entity.Technicien;
import sn.oas.facturation.technicien.dto.TechnicienRequest;

import java.util.List;
import java.util.Optional;

public interface TechnicienService {
    List<Technicien> getAllTechniciens();
    Optional<Technicien> getTechnicienById(Long id);
    void createTechnicien(TechnicienRequest request);
    Technicien updateTechnicien(Long id, TechnicienRequest request);
    void deleteTechnicien(Long id);
    List<Technicien> searchTechniciens(String keyword);

    /** Technicien actuellement authentifié (portail technicien). */
    Technicien getTechnicienConnecte();
}
