package sn.oas.facturation.features.connectionHistory.dto;

import lombok.Builder;
import sn.oas.facturation.features.connectionHistory.data.entity.ConnectionHistory;
import sn.oas.facturation.features.connectionHistory.data.enums.StatusConnectionHistory;

import java.time.LocalDateTime;

@Builder
public record ConnectionHistoryResponse(
        Long id,
        String username,
        String ipAddress,
        StatusConnectionHistory status,
        LocalDateTime timestamp
) {
    public static ConnectionHistoryResponse from(ConnectionHistory ch) {
        if (ch == null) return null;
        return ConnectionHistoryResponse.builder()
                .id(ch.getId())
                .username(ch.getUsername())
                .ipAddress(ch.getIpAddress())
                .status(ch.getStatus())
                .timestamp(ch.getTimestamp())
                .build();
    }
}
