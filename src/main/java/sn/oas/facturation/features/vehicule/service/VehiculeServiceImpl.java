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
    @Caching(evict = {
        @CacheEvict(value = "vehicules_page", allEntries = true),
        @CacheEvict(value = "dashboard_super_agent", allEntries = true),
        @CacheEvict(value = "dashboard_agent", allEntries = true)
    })
    public Vehicule createVehicule(VehiculeRequest request) {
        if (request.immatriculation() != null && vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
            throw new ResourceAlreadyExistsException("Immatriculation déjà existante : " + request.immatriculation());
        }

        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id : " + request.clientId()));

        Vehicule vehicule = Vehicule.builder()
                .immatriculation(request.immatriculation())
                .annee(request.annee())
                .modele(request.modele())
                .marque(request.marque())
                .kilometrage(request.kilometrage() != null ? request.kilometrage() : 0.0)
                .numeroChassis(request.numeroChassis())
                .client(client)
                .build();

        return vehiculeRepository.save(vehicule);
    }

    @Transactional
    @Override
    @Caching(evict = {
        @CacheEvict(value = "clients_page", allEntries = true),
        @CacheEvict(value = "dashboard_super_agent", allEntries = true),
        @CacheEvict(value = "dashboard_agent", allEntries = true)
})
    public Vehicule updateVehicule(Long id, VehiculeRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Véhicule non trouvé avec l'id : " + id));

        if (request.immatriculation() != null && !request.immatriculation().equalsIgnoreCase(vehicule.getImmatriculation())) {
            if (vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
                throw new ResourceAlreadyExistsException("Immatriculation déjà existante : " + request.immatriculation());
            }
            vehicule.setImmatriculation(request.immatriculation());
        }

        if (request.clientId() != null && !request.clientId().equals(vehicule.getClient().getId())) {
            Client client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé avec l'id : " + request.clientId()));
            vehicule.setClient(client);
        }

        if (request.annee() != null) vehicule.setAnnee(request.annee());
        if (request.modele() != null) vehicule.setModele(request.modele());
        if (request.marque() != null) vehicule.setMarque(request.marque());
        if (request.kilometrage() != null) vehicule.setKilometrage(request.kilometrage());
        if (request.numeroChassis() != null) vehicule.setNumeroChassis(request.numeroChassis());

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
