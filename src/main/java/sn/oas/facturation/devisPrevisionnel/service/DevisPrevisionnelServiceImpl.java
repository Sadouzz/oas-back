package sn.oas.facturation.devisPrevisionnel.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.service.AuthService;
import sn.oas.facturation.auth.service.UserService;
import sn.oas.facturation.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.devisPrevisionnel.dto.DevisPrevisionnelRequest;
import sn.oas.facturation.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.service.VehiculeService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DevisPrevisionnelServiceImpl implements DevisPrevisionnelService {

    private final DevisPrevisionnelRepository devisPrevisionnelRepository;
    private final VehiculeService vehiculeService;
    private final AuthService authService;
    private final UserService userService;

    @Transactional
    @Override
    public DevisPrevisionnel creer(DevisPrevisionnelRequest request) {
        Agent agent = authService.getAgentConnecte();
        Client client = userService.getClientById(request.clientId());
        Vehicule vehicule = getVehicule(request.vehiculeId());

        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Le véhicule ne correspond pas au client");
        }

        DevisPrevisionnel devis = DevisPrevisionnel.builder()
                .notesReparation(request.notesReparation())
                .montantTotal(request.montantTotal())
                .kilometrageVehicule(request.kilometrageVehicule())
                .vehicule(vehicule)
                .client(client)
                .agent(agent)
                .build();

        return devisPrevisionnelRepository.save(devis);
    }

    @Transactional
    @Override
    public DevisPrevisionnel modifier(Long id, DevisPrevisionnelRequest request) {
        DevisPrevisionnel devis = getById(id);

        Client client = userService.getClientById(request.clientId());
        Vehicule vehicule = getVehicule(request.vehiculeId());

        if (!vehicule.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Le véhicule ne correspond pas au client");
        }

        devis.setNotesReparation(request.notesReparation());
        devis.setMontantTotal(request.montantTotal());
        devis.setKilometrageVehicule(request.kilometrageVehicule());
        devis.setVehicule(vehicule);
        devis.setClient(client);

        return devisPrevisionnelRepository.save(devis);
    }

    @Transactional
    @Override
    public void supprimer(Long id) {
        if (!devisPrevisionnelRepository.existsById(id)) {
            throw new RuntimeException("Devis prévisionnel introuvable avec l'id : " + id);
        }
        devisPrevisionnelRepository.deleteById(id);
    }

    @Override
    public DevisPrevisionnel getById(Long id) {
        return devisPrevisionnelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Devis prévisionnel introuvable avec l'id : " + id));
    }

    @Override
    public List<DevisPrevisionnel> getAll() {
        return devisPrevisionnelRepository.findAll();
    }

    @Override
    public List<DevisPrevisionnel> getByClient(Long clientId) {
        return devisPrevisionnelRepository.findByClientId(clientId);
    }

    @Override
    public List<DevisPrevisionnel> getByVehicule(Long vehiculeId) {
        return devisPrevisionnelRepository.findByVehiculeId(vehiculeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DevisPrevisionnel> getClientDevis(Client client) {
        return devisPrevisionnelRepository.findByClientIdOrderByDateCreationDesc(client.getId());
    }

    @Override
    @Transactional
    public DevisPrevisionnel clientAccepter(Client client, Long id) {
        DevisPrevisionnel devis = getById(id);
        if (!devis.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à ce devis");
        }
        devis.setStatut(sn.oas.facturation.facturation.data.enums.StatutFacturation.ACCEPTE);
        return devisPrevisionnelRepository.save(devis);
    }

    @Override
    @Transactional
    public DevisPrevisionnel clientRefuser(Client client, Long id) {
        DevisPrevisionnel devis = getById(id);
        if (!devis.getClient().getId().equals(client.getId())) {
            throw new IllegalArgumentException("Accès non autorisé à ce devis");
        }
        devis.setStatut(sn.oas.facturation.facturation.data.enums.StatutFacturation.REJETE);
        return devisPrevisionnelRepository.save(devis);
    }

    private Vehicule getVehicule(Long vehiculeId) {
        return vehiculeService.getVehiculeById(vehiculeId);
    }

}
