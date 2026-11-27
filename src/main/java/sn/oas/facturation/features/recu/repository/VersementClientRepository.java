package sn.oas.facturation.features.recu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.recu.data.entity.VersementClient;

@Repository
public interface VersementClientRepository extends JpaRepository<VersementClient, Long> {
}
