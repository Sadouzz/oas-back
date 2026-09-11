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
import sn.oas.facturation.features.client.dto.ClientCreateRequest;
import sn.oas.facturation.features.client.dto.ClientCreateResponse;
import sn.oas.facturation.features.client.repository.ClientRepository;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.data.enums.TypeUser;
import sn.oas.facturation.features.user.dto.request.UserUpdateRequest;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.service.VehiculeService;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
                Sort.Order.desc("updatedAt"),
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        ));
        return clientRepository.findAll(pageable);
    }

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll(Sort.by(
                Sort.Order.desc("updatedAt"),
                Sort.Order.desc("createdAt"),
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
                .phone(request.phone())
                .email(request.email())
                .adresse(request.adresse())
                .matricule(matricule)
                .type(TypeUser.CLIENT)
                .username(request.email() != null && !request.email().isBlank() ? request.email() : request.phone())
                .password(passwordEncoder.encode(rawPassword))
                .enabled(true)
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


    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public Client updateClient(Long id, UserUpdateRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        if (request.phone() != null) client.setPhone(request.phone());
        if (request.firstName() != null) client.setFirstName(request.firstName());
        if (request.lastName() != null) client.setLastName(request.lastName());
        if (request.email() != null) client.setEmail(request.email());

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

        clientRepository.save(client);
    }

    @Override
    public List<Client> searchClients(String keyword) {
        return clientRepository.searchClients(keyword);
    }

    @Override
    public Page<Client> searchClients(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt"),
                Sort.Order.desc("createdAt"),
                Sort.Order.desc("id")
        ));
        return clientRepository.searchClients(keyword, pageable);
    }

    @Override
    public Page<Client> getArchivedClients(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("updatedAt"),
                Sort.Order.desc("createdAt"),
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
}
