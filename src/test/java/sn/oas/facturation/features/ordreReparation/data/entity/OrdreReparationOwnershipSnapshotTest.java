package sn.oas.facturation.features.ordreReparation.data.entity;

import org.junit.jupiter.api.Test;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import static org.junit.jupiter.api.Assertions.assertSame;

class OrdreReparationOwnershipSnapshotTest {
    @Test
    void capturesVehicleOwnerWhenCreatingAnOrderWithoutExplicitClient() {
        Client ownerAtCreation = new Client();
        Vehicule vehicle = new Vehicule();
        vehicle.setClient(ownerAtCreation);
        OrdreReparation order = new OrdreReparation();
        order.setVehicule(vehicle);

        order.onCreate();

        assertSame(ownerAtCreation, order.getClient());
    }

    @Test
    void doesNotOverwriteExplicitClientSnapshot() {
        Client recordedClient = new Client();
        Client currentVehicleOwner = new Client();
        Vehicule vehicle = new Vehicule();
        vehicle.setClient(currentVehicleOwner);
        OrdreReparation order = new OrdreReparation();
        order.setVehicule(vehicle);
        order.setClient(recordedClient);

        order.onCreate();

        assertSame(recordedClient, order.getClient());
    }
}
