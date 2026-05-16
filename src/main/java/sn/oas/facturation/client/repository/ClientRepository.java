package sn.oas.facturation.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.auth.data.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
}
