package sn.oas.facturation.client.service;

import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.dto.RegisterRequest;
import sn.oas.facturation.auth.dto.UserUpdateRequest;
import java.util.List;

public interface ClientService {
    List<Client> getAllClients();
    Client getClientById(Long id);
    Client createClient(RegisterRequest request);
    Client updateClient(Long id, UserUpdateRequest request);
    void archiveClient(Long id);
    void deleteClient(Long id);
}
