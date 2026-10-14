package sn.oas.facturation.features.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.user.data.entity.Agent;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    @Query(value = "SELECT a.* FROM agents a JOIN users u ON u.id = a.id WHERE a.role = :role AND u.enabled = true", nativeQuery = true)
    List<Agent> findNotificationRecipientsByRole(@Param("role") String role);

    @Query(value = "SELECT a.* FROM agents a JOIN users u ON u.id = a.id WHERE a.role = :role AND a.garage_id = :garageId AND u.enabled = true", nativeQuery = true)
    List<Agent> findNotificationRecipientsByRoleAndGarage(
            @Param("role") String role, @Param("garageId") Long garageId);
}
