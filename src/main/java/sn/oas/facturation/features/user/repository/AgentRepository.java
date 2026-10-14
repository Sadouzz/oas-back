package sn.oas.facturation.features.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sn.oas.facturation.features.user.data.entity.Agent;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    @Query("SELECT a FROM Agent a WHERE CAST(a.role AS String) = :role AND a.enabled = true")
    List<Agent> findNotificationRecipientsByRole(@Param("role") String role);

    @Query("SELECT a FROM Agent a WHERE CAST(a.role AS String) = :role AND a.garage.id = :garageId AND a.enabled = true")
    List<Agent> findNotificationRecipientsByRoleAndGarage(
            @Param("role") String role, @Param("garageId") Long garageId);
}
