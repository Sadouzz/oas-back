package sn.oas.facturation.auth.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.dto.CreateUserResponse;
import sn.oas.facturation.auth.dto.UserUpdateRequest;
import sn.oas.facturation.auth.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Transactional
    @Override
    public CreateUserResponse saveUser(User user)
    {
        userRepository.save(user);
        return new CreateUserResponse(user.getUsername());
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    @Override
    public User updateUser(Long id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.phone() != null) user.setPhone(request.phone());
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.email() != null) user.setEmail(request.email());

        if (user instanceof Agent agent && request.role() != null) {
            agent.setRole(request.role());
        }

        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void archiveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void unarchiveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        user.setEnabled(true);
        userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur non trouvé");
        }
        userRepository.deleteById(id);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByPhone(String phone) {
        return userRepository.existsByPhone(phone);
    }

    @Override
    public boolean existsByMatricule(String matricule) {
        return userRepository.existsByMatricule(matricule);
    }

    @Override
    public Optional<User> findByUsernameOrEmail(String username, String email) {
        return userRepository.findByUsernameOrEmail(username, email);
    }

    @Override
    public List<User> searchUsers(String keyword) {
        return userRepository.searchUsers(keyword);
    }

    @Override
    public Client getClientById(Long clientId) {
        User user = userRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client introuvable avec l'id : " + clientId));
        if (!(user instanceof Client client)) {
            throw new IllegalArgumentException("L'utilisateur id=" + clientId + " n'est pas un client");
        }
        return client;
    }
}
