package sn.oas.facturation.features.auth.dto;

public record AuthResponse(String token, String tokenType, String username, String role, Long garageId, String garageName) {

    public static AuthResponse of(String token, String username, String role, Long garageId, String garageName) {
        return new AuthResponse(token, "Bearer", username, role, garageId, garageName);
    }
}
