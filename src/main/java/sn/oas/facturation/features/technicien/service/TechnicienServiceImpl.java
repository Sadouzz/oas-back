package sn.oas.facturation.features.technicien.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Technicien;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.auth.data.enums.TypeUser;
import sn.oas.facturation.features.auth.dto.RegisterRequest;
import sn.oas.facturation.features.auth.repository.UserRepository;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.technicien.dto.TechnicienRequest;
import sn.oas.facturation.features.technicien.repository.TechnicienRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TechnicienServiceImpl implements TechnicienService {

    private final TechnicienRepository technicienRepository;
    private final UserRepository userRepository;
    private final GarageRepository garageRepository;
    private final AuthService authService;

    @Override
    public Page<Technicien> getAllTechniciens(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return technicienRepository.findAll(pageable);
    }

    @Override
    public List<Technicien> getAllTechniciens() {
        return technicienRepository.findAll();
    }

    @Override
    public Optional<Technicien> getTechnicienById(Long id) {
        return technicienRepository.findById(id);
    }

    @Override
    public List<Technicien> searchTechniciens(String keyword) {
        return technicienRepository.searchTechniciens(keyword);
    }

    @Override
    public Page<Technicien> searchTechniciens(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, org.springframework.data.domain.Sort.by("id").descending());
        return technicienRepository.searchTechniciens(keyword, pageable);
    }

    @Transactional
    @Override
    public void createTechnicien(TechnicienRequest request) {
        // Délègue à AuthServiceImpl.register(), point d'entrée commun à tous les types
        // d'utilisateurs (Client/Agent/Technicien) — cf. spec technicien.
        RegisterRequest registerRequest = new RegisterRequest(
                null,
                request.getPhone(),
                request.getUsername(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                TypeUser.TECHNICIEN,
                null,
                request.getGarageId(),
                request.getAdresse(),
                request.getSpecialite()
        );
        authService.register(registerRequest);
    }

    @Transactional
    @Override
    public Technicien updateTechnicien(Long id, TechnicienRequest request) {
        Technicien technicien = technicienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        if (request.getPhone() != null) technicien.setPhone(request.getPhone());
        if (request.getFirstName() != null) technicien.setFirstName(request.getFirstName());
        if (request.getLastName() != null) technicien.setLastName(request.getLastName());
        if (request.getEmail() != null) technicien.setEmail(request.getEmail());
        if (request.getAdresse() != null) technicien.setAdresse(request.getAdresse());
        if (request.getSpecialite() != null) technicien.setSpecialite(request.getSpecialite());
        if (request.getGarageId() != null) {
            Garage garage = garageRepository.findById(request.getGarageId())
                    .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé"));
            technicien.setGarage(garage);
        }

        return technicienRepository.save(technicien);
    }

    @Transactional
    @Override
    public void deleteTechnicien(Long id) {
        if (!technicienRepository.existsById(id)) {
            throw new RuntimeException("Technicien non trouvé");
        }
        technicienRepository.deleteById(id);
    }

    @Override
    public Technicien getTechnicienConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
        if (!(user instanceof Technicien technicien)) {
            throw new IllegalStateException("Cette opération requiert un compte Technicien");
        }
        return technicien;
    }
}
