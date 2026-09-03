package sn.oas.facturation.features.connectionHistory.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.connectionHistory.data.entity.ConnectionHistory;

@Repository
public interface ConnectionHistoryRepository extends JpaRepository<ConnectionHistory, Long> {
}