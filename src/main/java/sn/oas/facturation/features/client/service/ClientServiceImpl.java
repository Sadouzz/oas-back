package sn.oas.facturation.features.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.data.enums.TypeClient;
import sn.oas.facturation.features.client.dto.ClientCreateRequest;
import sn.oas.facturation.features.client.dto.ClientCreateResponse;
import sn.oas.facturation.features.client.dto.ClientFideleRequest;
import sn.oas.facturation.features.client.dto.ClientUpdateRequest;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.TypeUser;
import sn.oas.facturation.features.vehicule.service.VehiculeService;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserService userService;
    private final VehiculeService vehiculeService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<Client> getAllClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("createdAt").nullsLast(),
                Sort.Order.desc("id")
        ));
        return clientRepository.findAll(pageable);
    }

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll(Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("createdAt").nullsLast(),
                Sort.Order.desc("id")
        ));
    }

    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    @Transactional
    @Override
    /*@Caching(evict = {
        @CacheEvict(value = "dashboard_super_agent", allEntries = true),
        @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public ClientCreateResponse createClient(ClientCreateRequest request) {
        TypeClient clientType = request.typeClient() == null ? TypeClient.PARTICULIER : request.typeClient();
        String contactPhone = clientType == TypeClient.ENTREPRISE && hasText(request.telephoneEntreprise())
                ? request.telephoneEntreprise().trim() : request.phone();
        String contactEmail = clientType == TypeClient.ENTREPRISE && hasText(request.emailEntreprise())
                ? request.emailEntreprise().trim() : request.email();
        if (clientType == TypeClient.ENTREPRISE) {
            requireText(request.raisonSociale(), "La raison sociale est obligatoire pour une entreprise.");
            requireText(request.numeroEntreprise(), "Le NINEA est obligatoire pour une entreprise.");
            requireText(contactEmail, "L'email de l'entreprise est obligatoire.");
            requireText(contactPhone, "Le téléphone de l'entreprise est obligatoire.");
            requireText(request.adresseEntreprise(), "L'adresse de l'entreprise est obligatoire.");
            if (!contactEmail.contains("@")) throw new IllegalArgumentException("L'email de l'entreprise est invalide.");
        }
        // 1. Générer le matricule CLT-XXXXX
        String matricule = generateMatricule();

        // 2. Définir mot de passe sécurisé (fourni ou généré)
        String rawPassword = (request.password() != null && !request.password().trim().isEmpty())
                ? request.password()
                : UUID.randomUUID().toString().substring(0, 8);

        // 3. Créer l'entité Client
        Client client = Client.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(contactPhone)
                .email(contactEmail)
                .adresse(clientType == TypeClient.ENTREPRISE ? request.adresseEntreprise().trim() : request.adresse())
                .matricule(matricule)
                .type(TypeUser.CLIENT)
                .username(request.email() != null && !request.email().isBlank() ? request.email() : request.phone())
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
                .typeClient(clientType)
                .raisonSociale(clientType == TypeClient.ENTREPRISE ? request.raisonSociale().trim() : null)
                .numeroEntreprise(clientType == TypeClient.ENTREPRISE ? request.numeroEntreprise().trim() : null)
                .emailEntreprise(clientType == TypeClient.ENTREPRISE ? contactEmail : null)
                .adresseEntreprise(clientType == TypeClient.ENTREPRISE ? request.adresseEntreprise().trim() : null)
                .build();

        Client saved = clientRepository.save(client);
        return ClientCreateResponse.from(saved);
    }

    private String generateMatricule() {
        String maxMatricule = clientRepository.findMaxClientMatricule();
        long nextNumber = clientRepository.count() + 1;

        if (maxMatricule != null && maxMatricule.startsWith("CLT-")) {
            try {
                String numStr = maxMatricule.substring(4);
                nextNumber = Math.max(nextNumber, Long.parseLong(numStr) + 1);
            } catch (NumberFormatException ignored) {
            }
        }

        String matricule = String.format("CLT-%05d", nextNumber);
        while (userRepository.existsByMatricule(matricule)) {
            nextNumber++;
            matricule = String.format("CLT-%05d", nextNumber);
        }
        return matricule;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static void requireText(String value, String message) {
        if (!hasText(value)) throw new IllegalArgumentException(message);
    }


    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public Client updateClient(Long id, ClientUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        TypeClient resultingType = request.typeClient() != null ? request.typeClient()
                : client.getTypeClient() == null ? TypeClient.PARTICULIER : client.getTypeClient();
        String contactPhone = resultingType == TypeClient.ENTREPRISE && hasText(request.telephoneEntreprise())
                ? request.telephoneEntreprise().trim() : request.phone();
        String contactEmail = resultingType == TypeClient.ENTREPRISE && hasText(request.emailEntreprise())
                ? request.emailEntreprise().trim() : request.email();
        if (resultingType == TypeClient.ENTREPRISE) {
            String raisonSociale = hasText(request.raisonSociale()) ? request.raisonSociale() : client.getRaisonSociale();
            String numeroEntreprise = hasText(request.numeroEntreprise()) ? request.numeroEntreprise() : client.getNumeroEntreprise();
            String adresseEntreprise = hasText(request.adresseEntreprise()) ? request.adresseEntreprise() : client.getAdresseEntreprise();
            requireText(raisonSociale, "La raison sociale est obligatoire pour une entreprise.");
            requireText(numeroEntreprise, "Le NINEA est obligatoire pour une entreprise.");
            requireText(contactEmail != null ? contactEmail : client.getEmail(), "L'email de l'entreprise est obligatoire.");
            requireText(contactPhone != null ? contactPhone : client.getPhone(), "Le téléphone de l'entreprise est obligatoire.");
            requireText(adresseEntreprise, "L'adresse de l'entreprise est obligatoire.");
            client.setRaisonSociale(raisonSociale.trim());
            client.setNumeroEntreprise(numeroEntreprise.trim());
            client.setAdresseEntreprise(adresseEntreprise.trim());
            client.setEmailEntreprise(contactEmail != null ? contactEmail : client.getEmail());
        } else {
            client.setRaisonSociale(null);
            client.setNumeroEntreprise(null);
            client.setAdresseEntreprise(null);
            client.setEmailEntreprise(null);
        }
        client.setTypeClient(resultingType);
        if (contactPhone != null) client.setPhone(contactPhone);
        if (request.firstName() != null) client.setFirstName(request.firstName());
        if (request.lastName() != null) client.setLastName(request.lastName());
        if (contactEmail != null) client.setEmail(contactEmail);
        client.setUpdatedAt(java.time.LocalDateTime.now());

        return clientRepository.save(client);
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void archiveClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setEnabled(false);
        client.setUpdatedAt(java.time.LocalDateTime.now());
        clientRepository.save(client);
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void unarchiveClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setEnabled(true);
        client.setUpdatedAt(java.time.LocalDateTime.now());
        clientRepository.save(client);
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client non trouvé");
        }
        clientRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void anonymizeClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        client.setFirstName("[Anonymisé]");
        client.setLastName("[Anonymisé]");
        client.setEmail("anonyme-" + id + "@supprime.local");
        client.setPhone("0000000000");
        client.setUsername("anonyme-" + id);
        client.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
        client.setEnabled(false);
        client.setUpdatedAt(java.time.LocalDateTime.now());

        clientRepository.save(client);
    }

    @Override
    public List<Client> searchClients(String keyword) {
        return clientRepository.searchClients(keyword);
    }

    @Override
    public Page<Client> searchClients(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("createdAt").nullsLast(),
                Sort.Order.desc("id")
        ));
        return clientRepository.searchClients(keyword, pageable);
    }

    @Override
    public Page<Client> getArchivedClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt").nullsLast(),
                Sort.Order.desc("createdAt").nullsLast(),
                Sort.Order.desc("id")
        ));
        return clientRepository.findByEnabled(false, pageable);
    }

    @Override
    public List<Client> getRecentClients() {

        return clientRepository.findTop5ByOrderByCreatedAtDesc();
    }

    @Override
    public Client getClientConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
        if (!(user instanceof Client client)) {
            throw new IllegalStateException("Cette opération requiert un compte Client");
        }
        return client;
    }

    public void updateClientFideleConfig(Long id, ClientFideleRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));

        client.setClientFidele(true);

        // On ne met à jour que les champs qui ont été envoyés (non null)
        if (request.montantRemise() != null) {
            client.setMontantRemise(request.montantRemise());
        }
        if (request.montantPlafond() != null) {
            client.setMontantPlafond(request.montantPlafond());
        }
        if (request.echeance() != null) {
            client.setEcheance(request.echeance());
        }
        if (request.ninea() != null) {
            client.setNinea(request.ninea());
        }
        if (request.rccm() != null) {
            client.setRccm(request.rccm());
        }
        if (request.rib() != null) {
            client.setRib(request.rib());
        }

        client.setUpdatedAt(java.time.LocalDateTime.now());
        clientRepository.save(client);
    }

    @Override
    public void removeClientFidele(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client non trouvé"));
        client.setClientFidele(false);
        client.setMontantRemise(null);
        client.setMontantPlafond(null);
        client.setEcheance(null);
        client.setNinea(null);
        client.setRccm(null);
        client.setRib(null);
        client.setUpdatedAt(java.time.LocalDateTime.now());
        clientRepository.save(client);
    }

    /*
    @Override
    public void toggleClientFidele(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setClientFidele(!client.isClientFidele());
        clientRepository.save(client);
    }



    @Override
    public void updateClientFidele(Long id, ClientFideleRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setClientFidele(request.isClientFidele());
        clientRepository.save(client);
    }

    @Override
    public void updateClientNinea(Long id, ClientFideleRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setNinea(request.ninea());
        clientRepository.save(client);
    }

    @Override
    public void updateClientRccm(Long id, ClientFideleRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setRccm(request.rccm());
        clientRepository.save(client);
    }

    @Override
    public void updateClientRib(Long id, ClientFideleRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setRib(request.rib());
        clientRepository.save(client);
    }
        */
}
