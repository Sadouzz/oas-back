package sn.oas.facturation.auth.service;

import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.dto.CreateUserResponse;

import java.util.Optional;

public interface UserService {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    Optional <User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    CreateUserResponse saveUser(User user);
    java.util.List<User> findAll();
    Optional<User> findById(Long id);
    User updateUser(Long id, sn.oas.facturation.auth.dto.UserUpdateRequest request);
    void archiveUser(Long id);
    void deleteUser(Long id);
}
