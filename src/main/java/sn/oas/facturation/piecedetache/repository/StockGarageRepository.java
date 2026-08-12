package sn.oas.facturation.piecedetache.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.entity.StockGarage;

import java.util.Optional;

@Repository
public interface StockGarageRepository extends JpaRepository<StockGarage, Long> {
    Optional<StockGarage> findByGarageAndPiece(Garage garage, PieceDetache piece);
}
