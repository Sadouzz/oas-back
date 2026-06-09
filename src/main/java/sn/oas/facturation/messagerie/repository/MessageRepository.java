package sn.oas.facturation.messagerie.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.messagerie.data.entity.Message;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByClientIdOrderByDateEnvoiAsc(Long clientId);
    long countByClientIdAndExpediteurIdNotAndLu(Long clientId, Long senderId, boolean lu);

    @Query("SELECT DISTINCT m.client FROM Message m")
    List<Client> findActiveClients();
}
