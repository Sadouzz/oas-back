package sn.oas.facturation.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.auth.data.entity.ConnectionHistory;
import sn.oas.facturation.auth.repository.ConnectionHistoryRepository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Sort;

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
        } else {
            // X-Forwarded-For peut contenir plusieurs IPs (ex: "client, proxy1, proxy2")
            ip = ip.split(",")[0].trim();
        }
        
        // Convertir l'adresse IPv6 localhost en IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }

    @Override
    public List<ConnectionHistory> getAllConnectionHistory() {
        return connectionHistoryRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp"));
    }
}
