package sn.oas.facturation.recu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.recu.data.entity.Recu;

import java.util.List;

@Repository
public interface RecuRepository extends JpaRepository<Recu, Long> {
    List<Recu> findByFactureClientIdOrderByDatePaiementDesc(Long clientId);
}
