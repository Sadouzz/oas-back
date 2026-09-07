package sn.oas.facturation.features.connectionHistory.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.connectionHistory.data.entity.ConnectionHistory;

import java.util.List;

@Repository
public interface ConnectionHistoryRepository extends JpaRepository<ConnectionHistory, Long> {

    @Query(value = "SELECT ch FROM ConnectionHistory ch WHERE " +
            "LOWER(ch.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ch.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT COUNT(ch) FROM ConnectionHistory ch WHERE " +
            "LOWER(ch.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ch.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<ConnectionHistory> searchConnectionHistory(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT ch FROM ConnectionHistory ch WHERE " +
            "LOWER(ch.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(ch.ipAddress) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<ConnectionHistory> searchConnectionHistory(@Param("keyword") String keyword);
}