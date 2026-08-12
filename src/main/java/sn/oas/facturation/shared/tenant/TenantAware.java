package sn.oas.facturation.shared.tenant;

import sn.oas.facturation.garage.data.entity.Garage;

public interface TenantAware {
    Garage getGarage();
    void setGarage(Garage garage);
}

