package sn.oas.facturation.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sn.oas.facturation.auth.data.entity.ConnectionHistory;

public interface ConnectionHistoryRepository extends JpaRepository<ConnectionHistory, Long> {
}