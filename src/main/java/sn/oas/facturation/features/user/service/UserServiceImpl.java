package sn.oas.facturation.features.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.user.repository.UserRepository;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.dto.request.UserUpdateRequest;
import sn.oas.facturation.features.user.dto.response.CreateUserResponse;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;
import sn.oas.facturation.shared.exception.UnauthorizedException;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;
    private final GarageRepository garageRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public CreateUserResponse saveUser(User user)
    {
        userRepository.save(user);
        return new CreateUserResponse(user.getUsername());
    }

    @Override
    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            boolean isMaster = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER") || a.getAuthority().equals("MASTER"));
            if (isMaster) {
                // Récupérer le garage du MASTER connecté
                Optional<User> currentUserOpt = userRepository.findByUsername(auth.getName());
                if (currentUserOpt.isPresent() && currentUserOpt.get() instanceof Agent currentAgent && currentAgent.getGarage() != null) {
                    Long masterGarageId = currentAgent.getGarage().getId();
                    return users.stream().filter(u -> {
                        if (u instanceof Agent a) {
                            return a.getGarage() != null && a.getGarage().getId().equals(masterGarageId);
                        }
                        // Pour les autres types d'utilisateurs (ex: Super Agent sans garage, ou Client), on peut les masquer ou les afficher
                        return false; 
                    }).toList();
                }
            }
        }
        return users;
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

        if (user instanceof Agent agent) {
            if (request.role() != null) {
                agent.setRole(request.role());
            }
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            boolean isMaster = false;
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                isMaster = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MASTER") || a.getAuthority().equals("MASTER"));
            }
            if (!isMaster && request.garageId() != null) {
                Garage garage = garageRepository.findById(request.garageId())
                        .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé"));
                agent.setGarage(garage);
            }
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

    @Override
    public Page<User> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> searchUsers(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.searchUsers(keyword.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }
        String username = auth.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur connecté introuvable : " + username));
    }

    @Transactional
    @Override
    public User updateCurrentUser(UserUpdateRequest request) {
        User user = getCurrentUser();
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.email() != null) user.setEmail(request.email());
        return userRepository.save(user);
    }

    @Transactional
    @Override
    public void changePasswordForCurrentUser(String oldPassword, String newPassword) {
        User user = getCurrentUser();
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }
        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new BadRequestException("Le nouveau mot de passe doit comporter au moins 6 caractères");
        }
        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);
    }

    @Transactional
    @Override
    public User toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'id : " + id));
        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
    }
}
