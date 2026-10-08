package sn.oas.facturation.features.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import sn.oas.facturation.features.client.data.entity.CompteClient;

public interface CompteClientRepository extends JpaRepository<CompteClient, Long> {
    Optional<CompteClient> findByClientId(Long clientId);
}
