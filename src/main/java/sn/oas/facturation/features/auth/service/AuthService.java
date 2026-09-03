package sn.oas.facturation.features.auth.service;

import sn.oas.facturation.features.auth.dto.request.LoginRequest;
import sn.oas.facturation.features.auth.dto.request.RegisterRequest;
import sn.oas.facturation.features.auth.dto.response.AuthResponse;
import sn.oas.facturation.features.user.data.entity.Agent;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String currentToken);
    void register(RegisterRequest request);
    Agent getAgentConnecte();
}
