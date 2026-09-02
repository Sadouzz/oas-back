package sn.oas.facturation.features.auth.service;

import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.dto.AuthResponse;
import sn.oas.facturation.features.auth.dto.LoginRequest;
import sn.oas.facturation.features.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String currentToken);
    void register(RegisterRequest request);
    Agent getAgentConnecte();
}
