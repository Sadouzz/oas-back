package sn.oas.facturation.features.client.service;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.client.dto.ClientCreateRequest;
import sn.oas.facturation.features.client.dto.ClientCreateResponse;
import sn.oas.facturation.features.client.dto.ClientFideleRequest;
import sn.oas.facturation.features.client.dto.ClientUpdateRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface ClientService {
    Page<Client> getAllClients(int page, int size);
    List<Client> getAllClients();
    Client getClientById(Long id);
    ClientCreateResponse createClient(ClientCreateRequest request);
    Client updateClient(Long id, ClientUpdateRequest request);
    void archiveClient(Long id);
    void unarchiveClient(Long id);
    void deleteClient(Long id);
    void anonymizeClient(Long id);
    List<Client> searchClients(String keyword);
    Page<Client> searchClients(String keyword, int page, int size);
    Page<Client> getArchivedClients(int page, int size);
    List<Client> getRecentClients();
    Client getClientConnecte();

    void updateClientFideleConfig(Long id, ClientFideleRequest request);

    void removeClientFidele(Long id);


    // void toggleClientFidele(Long id);
    // void updateClientFidele(Long id, ClientFideleRequest request);
    // void updateClientRemise(Long id, ClientFideleRequest request);
    // void updateClientPlafond(Long id, ClientFideleRequest request);
    // void updateClientEcheance(Long id, ClientFideleRequest request);
    // void updateClientNinea(Long id, ClientFideleRequest request);
    // void updateClientRccm(Long id, ClientFideleRequest request);
    // void updateClientRib(Long id, ClientFideleRequest request);
}
