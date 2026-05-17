package sn.oas.facturation.client.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.enums.TypeUser;
import sn.oas.facturation.auth.dto.RegisterRequest;
import sn.oas.facturation.auth.dto.UserUpdateRequest;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.auth.service.UserService;
import sn.oas.facturation.client.repository.ClientRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Override
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    /*
    @Transactional
    @Override
    public Client createClient(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already in use: " + request.username());
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use: " + request.email());
        }

        Client client = Client.builder()
                .matricule(request.matricule())
                .phone(request.phone())
                .username(request.username())
                .firstName(request.firstName())
                .lastName(request.lastName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .type(TypeUser.CLIENT)
                .enabled(true)
                .build();

        return clientRepository.save(client);
    }
    */

    @Transactional
    @Override
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
    public void archiveClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setEnabled(false);
        clientRepository.save(client);
    }

    @Transactional
    @Override
    public void unarchiveClient(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
        client.setEnabled(true);
        clientRepository.save(client);
    }

    @Transactional
    @Override
    public void deleteClient(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new RuntimeException("Client non trouvé");
        }
        clientRepository.deleteById(id);
    }

    @Override
    public List<Client> searchClients(String keyword) {
        return clientRepository.searchClients(keyword);
    }

    @Override
    public List<Client> getRecentClients() {
        return clientRepository.findTop5ByOrderByCreatedAtDesc();
    }
}
