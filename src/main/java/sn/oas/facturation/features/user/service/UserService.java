package sn.oas.facturation.features.user.service;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.features.user.dto.request.UserUpdateRequest;
import sn.oas.facturation.features.user.dto.response.CreateUserResponse;

import org.springframework.data.domain.Page;

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
    Page<User> getAllUsers(int page, int size);
    Optional<User> findById(Long id);
    User updateUser(Long id, UserUpdateRequest request);
    void archiveUser(Long id);
    void unarchiveUser(Long id);
    void deleteUser(Long id);
    List<User> searchUsers(String keyword);
    Page<User> searchUsers(String keyword, int page, int size);
    User getCurrentUser();
    User updateCurrentUser(UserUpdateRequest request);
    void changePasswordForCurrentUser(String oldPassword, String newPassword);
    User toggleUserStatus(Long id);
}
