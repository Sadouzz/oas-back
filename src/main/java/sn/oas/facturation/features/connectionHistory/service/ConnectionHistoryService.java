package sn.oas.facturation.features.connectionHistory.service;

import jakarta.servlet.http.HttpServletRequest;
import sn.oas.facturation.features.connectionHistory.data.entity.ConnectionHistory;

import java.util.List;

public interface ConnectionHistoryService {
    void saveConnectionLog(String email, String ip, String status);
    String getClientIp(HttpServletRequest request);
    List<ConnectionHistory> getAllConnectionHistory();
}
