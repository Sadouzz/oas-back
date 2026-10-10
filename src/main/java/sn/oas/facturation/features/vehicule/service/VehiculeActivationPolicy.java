package sn.oas.facturation.features.vehicule.service;

import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.shared.exception.BadRequestException;

/** Central business guard for operations that require an agent-activated vehicle. */
public final class VehiculeActivationPolicy {
    private VehiculeActivationPolicy() {}

    public static void requireActive(Vehicule vehicule) {
        if (vehicule != null && !vehicule.isActif()) {
            throw new BadRequestException("Ce véhicule est en attente d'activation par un agent.");
        }
    }
}
