package sn.oas.facturation.rendezvous.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.rendezvous.data.entity.RendezVousDateHistory;

import java.util.List;

@Repository
public interface RendezVousDateHistoryRepository extends JpaRepository<RendezVousDateHistory, Long> {

    List<RendezVousDateHistory> findByRendezVousIdOrderByDateModificationDesc(Long rendezVousId);
}