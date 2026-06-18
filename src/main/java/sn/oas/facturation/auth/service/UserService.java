package sn.oas.facturation.auth.service;

import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.dto.CreateUserResponse;
import sn.oas.facturation.auth.dto.UserUpdateRequest;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional <User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByMatricule(String matricule);
    Optional<User> findByUsernameOrEmail(String username, String email);
    CreateUserResponse saveUser(User user);
    Client getClientById(Long clientId);
    List<User> getAllUsers();
    Optional<User> findById(Long id);
    User updateUser(Long id, UserUpdateRequest request);
    void archiveUser(Long id);
    void unarchiveUser(Long id);
    void deleteUser(Long id);
    List<User> searchUsers(String keyword);
}
