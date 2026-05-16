package sn.oas.facturation.auth.service;

import jakarta.servlet.http.HttpServletRequest;

public interface ConnectionHistoryService {
    void saveConnectionLog(String email, String ip, String status);
    String getClientIp(HttpServletRequest request);
}
