package sn.oas.facturation.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.auth.data.entity.ConnectionHistory;
import sn.oas.facturation.auth.repository.ConnectionHistoryRepository;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ConnectionHistoryServiceImpl implements ConnectionHistoryService{
    private final ConnectionHistoryRepository connectionHistoryRepository;
    @Override
    public void saveConnectionLog(String username, String ip, String status) {
        ConnectionHistory log = ConnectionHistory.builder()
                .username(username)
                .ipAddress(ip)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
        connectionHistoryRepository.save(log);
    }

    @Override
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
