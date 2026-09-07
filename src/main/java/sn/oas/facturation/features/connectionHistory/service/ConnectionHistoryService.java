package sn.oas.facturation.features.connectionHistory.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import sn.oas.facturation.features.connectionHistory.data.entity.ConnectionHistory;

import java.util.List;

public interface ConnectionHistoryService {
    void saveConnectionLog(String email, String ip, String status);
    String getClientIp(HttpServletRequest request);
    List<ConnectionHistory> getAllConnectionHistory();
    Page<ConnectionHistory> getAllConnectionHistory(int page, int size);
    Page<ConnectionHistory> searchConnectionHistory(String keyword, int page, int size);
}
