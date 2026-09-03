package sn.oas.facturation.features.client.service;

import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.user.dto.request.UserUpdateRequest;

import org.springframework.data.domain.Page;
import java.util.List;

public interface ClientService {
    Page<Client> getAllClients(int page, int size);
    List<Client> getAllClients();
    Client getClientById(Long id);
    //Client createClient(RegisterRequest request);
    Client updateClient(Long id, UserUpdateRequest request);
    void archiveClient(Long id);
    void unarchiveClient(Long id);
    void deleteClient(Long id);
    void anonymizeClient(Long id);
    List<Client> searchClients(String keyword);
    Page<Client> searchClients(String keyword, int page, int size);
    List<Client> getRecentClients();
    Client getClientConnecte();
}
