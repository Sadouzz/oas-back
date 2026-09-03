package sn.oas.facturation.features.auth.dto.response;

public record AuthResponse(
        String token,
        String refreshToken,
        String tokenType,
        String username,
        String role,
        Long garageId,
        String garageName
) {
    public static AuthResponse of(String token, String refreshToken, String username, String role, Long garageId, String garageName) {
        return new AuthResponse(token, refreshToken, "Bearer", username, role, garageId, garageName);
    }

    public static AuthResponse of(String token, String username, String role, Long garageId, String garageName) {
        return new AuthResponse(token, null, "Bearer", username, role, garageId, garageName);
    }
}
