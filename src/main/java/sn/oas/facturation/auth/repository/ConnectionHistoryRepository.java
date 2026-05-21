package sn.oas.facturation.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.auth.data.entity.ConnectionHistory;

@Repository
public interface ConnectionHistoryRepository extends JpaRepository<ConnectionHistory, Long> {
}