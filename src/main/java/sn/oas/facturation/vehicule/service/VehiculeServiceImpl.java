package sn.oas.facturation.vehicule.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.dto.VehiculeRequest;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehiculeServiceImpl implements VehiculeService {

    private final VehiculeRepository vehiculeRepository;
    private final ClientRepository clientRepository;

    @Override
    public List<Vehicule> getAllVehicules() {
        return vehiculeRepository.findAll();
    }

    @Override
    public Vehicule getVehiculeById(Long id) {
        return vehiculeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule introuvable avec l'id : " + id));
    }

    @Transactional
    @Override
    public Vehicule createVehicule(VehiculeRequest request) {
        if (request.immatriculation() != null && vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
            throw new IllegalArgumentException("Immatriculation déjà existante : " + request.immatriculation());
        }

        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id : " + request.clientId()));

        Vehicule vehicule = Vehicule.builder()
                .immatriculation(request.immatriculation())
                .annee(request.annee())
                .modele(request.modele())
                .marque(request.marque())
                .kilometrage(request.kilometrage())
                .numeroChassis(request.numeroChassis())
                .client(client)
                .build();

        return vehiculeRepository.save(vehicule);
    }

    @Transactional
    @Override
    public Vehicule updateVehicule(Long id, VehiculeRequest request) {
        Vehicule vehicule = vehiculeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));

        if (request.immatriculation() != null && !request.immatriculation().equalsIgnoreCase(vehicule.getImmatriculation())) {
            if (vehiculeRepository.existsByImmatriculation(request.immatriculation())) {
                throw new IllegalArgumentException("Immatriculation déjà existante : " + request.immatriculation());
            }
            vehicule.setImmatriculation(request.immatriculation());
        }

        if (request.clientId() != null && !request.clientId().equals(vehicule.getClient().getId())) {
            Client client = clientRepository.findById(request.clientId())
                    .orElseThrow(() -> new RuntimeException("Client non trouvé avec l'id : " + request.clientId()));
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
            throw new RuntimeException("Véhicule non trouvé");
        }
        vehiculeRepository.deleteById(id);
    }

    @Override
    public List<Vehicule> searchVehicules(String keyword) {
        return vehiculeRepository.searchVehicules(keyword);
    }

    @Override
    public List<Vehicule> getVehiculesByClient(Long clientId) {
        return vehiculeRepository.findByClientId(clientId);
    }

    @Override
    public List<Vehicule> getRecentVehicules() {
        return vehiculeRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
