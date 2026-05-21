package sn.oas.facturation.auth.service;

import sn.oas.facturation.auth.dto.AuthResponse;
import sn.oas.facturation.auth.dto.LoginRequest;
import sn.oas.facturation.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void register(RegisterRequest request);
}
