package sn.oas.facturation.features.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.enums.Role;

import java.util.List;

@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    List<Agent> findByRole(Role role);
}
