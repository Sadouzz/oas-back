package sn.oas.facturation.features.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.notification.data.entity.AgentNotification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface AgentNotificationRepository extends JpaRepository<AgentNotification, Long> {
    Page<AgentNotification> findByAgentIdOrderByDateCreationDescIdDesc(Long agentId, Pageable pageable);
    long countByAgentIdAndLuFalse(Long agentId);

    @Modifying
    @Query("update AgentNotification n set n.lu = true where n.agent.id = :agentId and n.lu = false")
    int markAllAsReadForAgent(@Param("agentId") Long agentId);
}
