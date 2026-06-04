package sn.oas.facturation.shared.entity;

import sn.oas.facturation.garage.data.entity.Garage;

/**
 * Interface marquant les entités qui appartiennent à un Garage.
 * Le {@code GarageEntityListener} se charge d'injecter automatiquement
 * le garage de l'utilisateur connecté lors de la création (@PrePersist).
 */
public interface GarageAware {
    Garage getGarage();
    void setGarage(Garage garage);
}
