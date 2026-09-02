package sn.oas.facturation.features.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import sn.oas.facturation.features.auth.data.entity.ConnectionHistory;
import java.util.List;

public interface ConnectionHistoryService {
    void saveConnectionLog(String email, String ip, String status);
    String getClientIp(HttpServletRequest request);
    List<ConnectionHistory> getAllConnectionHistory();
}
