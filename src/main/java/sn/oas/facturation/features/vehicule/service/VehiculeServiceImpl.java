package sn.oas.facturation.features.vehicule.service;

import lombok.RequiredArgsConstructor;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.dto.VehiculeRequest;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceAlreadyExistsException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;

    @Override
    public Page<Vehicule> getAllVehicules(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return vehiculeRepository.findAll(pageable);
    }

    @Override
    public Vehicule getVehiculeById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + id));
    }

    @Transactional
    @Override
    public Vehicule createVehicule(VehiculeRequest request) {
        String immat = (request.immatriculation() != null && !request.immatriculation().trim().isEmpty())
                ? request.immatriculation().trim().toUpperCase()
                : null;
        if (immat == null) {
            throw new BadRequestException("L'immatriculation du véhicule est obligatoire.");
        }
        if (vehiculeRepository.existsByImmatriculationIgnoreCase(immat)) {
            throw new ResourceAlreadyExistsException("Immatriculation déjà existante : " + immat);
        }

        String chassis = (request.numeroChassis() != null && !request.numeroChassis().trim().isEmpty())
                ? request.numeroChassis().trim()
                : null;
        if (chassis != null && vehiculeRepository.existsByNumeroChassis(chassis)) {
            throw new ResourceAlreadyExistsException("Numéro de châssis déjà existant : " + chassis);
        }

        if (request.clientId() == null) {
            throw new BadRequestException("L'identifiant du client est obligatoire pour créer un véhicule.");
        }

        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id : " + request.clientId()));

        Vehicule vehicule = Vehicule.builder()
                .immatriculation(immat)
                .annee(request.annee())
                .modele(request.modele() != null ? request.modele().trim() : null)
                .marque(request.marque() != null ? request.marque().trim() : null)
                .kilometrage(request.kilometrage() != null ? request.kilometrage() : 0.0)
                .numeroChassis(chassis)
                .client(client)
                .build();

        return vehiculeRepository.save(vehicule);
    }

    @Transactional
    @Override
    public Vehicule updateVehicule(Long id, VehiculeRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + id));

        String immat = (request.immatriculation() != null && !request.immatriculation().trim().isEmpty())
                ? request.immatriculation().trim().toUpperCase()
                : null;
        if (immat != null && !immat.equalsIgnoreCase(vehicule.getImmatriculation())) {
            if (vehiculeRepository.existsByImmatriculationIgnoreCase(immat)) {
                throw new ResourceAlreadyExistsException("Immatriculation déjà existante : " + immat);
            }
            vehicule.setImmatriculation(immat);
        }

        String chassis = (request.numeroChassis() != null && !request.numeroChassis().trim().isEmpty())
                ? request.numeroChassis().trim()
                : null;
        if (chassis != null && !chassis.equalsIgnoreCase(vehicule.getNumeroChassis())) {
            if (vehiculeRepository.existsByNumeroChassis(chassis)) {
                throw new ResourceAlreadyExistsException("Numéro de châssis déjà existant : " + chassis);
            }
            vehicule.setNumeroChassis(chassis);
        } else if (request.numeroChassis() != null && request.numeroChassis().trim().isEmpty()) {
            vehicule.setNumeroChassis(null);
        }

        if (request.clientId() != null && !request.clientId().equals(vehicule.getClient().getId())) {
            Client client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id : " + request.clientId()));
            vehicule.setClient(client);
        }

        if (request.annee() != null) vehicule.setAnnee(request.annee());
        if (request.modele() != null) vehicule.setModele(request.modele().trim());
        if (request.marque() != null) vehicule.setMarque(request.marque().trim());
        if (request.kilometrage() != null) vehicule.setKilometrage(request.kilometrage());

        return vehiculeRepository.save(vehicule);
    }

    @Transactional
    @Override
    public void deleteVehicule(Long id) {
        if (!vehiculeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + id);
        }
        vehiculeRepository.deleteById(id);
    }

    @Override
    public List<Vehicule> searchVehicules(String keyword) {
        return vehiculeRepository.searchVehicules(keyword);
    }

    @Override
    public Page<Vehicule> searchVehicules(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return vehiculeRepository.searchVehicules(keyword, pageable);
    }

    @Override
    public List<Vehicule> getVehiculesByClient(Long clientId) {
        return vehiculeRepository.findByClientId(clientId);
    }

    @Override
    public List<Vehicule> getRecentVehicules() {
        return vehiculeRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    public List<Vehicule> getVehiculesActifsByClient(Long clientId) {
        return vehiculeRepository.findByClientIdAndArchiveParClientFalse(clientId);
    }

    @Transactional
    @Override
    public void archiveVehiculeByClient(Long vehiculeId, Long clientId) {
        Vehicule vehicule = vehiculeRepository.findById(vehiculeId)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + vehiculeId));
        
        if (!vehicule.getClient().getId().equals(clientId)) {
            throw new sn.oas.facturation.shared.exception.BadRequestException("Ce véhicule n'appartient pas à ce client");
        }

        boolean hasActiveRepairs = vehicule.getOrdresReparation() != null && 
            vehicule.getOrdresReparation().stream().anyMatch(or -> 
                !or.getStatut().name().equals("TERMINE") && !or.getStatut().name().equals("ANNULE"));

        if (hasActiveRepairs) {
             throw new sn.oas.facturation.shared.exception.BadRequestException("Impossible d'archiver un véhicule avec des réparations en cours");
        }

        vehicule.setArchiveParClient(true);
        vehiculeRepository.save(vehicule);
    }
}
