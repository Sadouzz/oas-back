package sn.oas.facturation.features.client.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PortefeuilleClientResponse(Long clientId, BigDecimal solde,
                                         LocalDateTime dateCreation, LocalDateTime dateModification,
                                         List<MouvementPortefeuilleResponse> mouvements) { }
