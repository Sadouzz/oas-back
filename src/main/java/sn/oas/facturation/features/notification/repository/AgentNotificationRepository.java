package sn.oas.facturation.features.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.notification.data.entity.AgentNotification;

import java.util.List;

@Repository
public interface AgentNotificationRepository extends JpaRepository<AgentNotification, Long> {
    List<AgentNotification> findByAgentIdOrderByDateCreationDesc(Long agentId);
}
