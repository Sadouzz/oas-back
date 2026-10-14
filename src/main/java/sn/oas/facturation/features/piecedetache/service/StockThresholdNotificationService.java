package sn.oas.facturation.features.piecedetache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.features.notification.service.AgentNotificationService;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.user.data.enums.Role;

@Service
@RequiredArgsConstructor
public class StockThresholdNotificationService {
    private static final double DEFAULT_THRESHOLD = 10.0;
    private final AgentNotificationService agentNotificationService;

    public void notifyIfThresholdCrossed(PDP piece, double stockBefore) {
        if (piece == null || piece.getGarage() == null) return;
        double stockAfter = piece.getStockMagasin() == null ? 0.0 : piece.getStockMagasin();
        double threshold = piece.getSeuilMinimum() == null ? DEFAULT_THRESHOLD : piece.getSeuilMinimum();
        boolean rupture = stockBefore > 0 && stockAfter <= 0;
        boolean lowStock = stockBefore > threshold && stockAfter > 0 && stockAfter <= threshold;
        if (!rupture && !lowStock) return;
        String title = rupture ? "Rupture de stock" : "Stock faible";
        String message = "La pièce " + piece.getDesignation() + " (" + piece.getReference()
                + ") est à " + stockAfter + " unité(s) en magasin. Seuil minimum : " + threshold + ".";
        agentNotificationService.notifyRoleInGarage(Role.AGENT_MAGASIN, piece.getGarage(), title, message);
    }
}
