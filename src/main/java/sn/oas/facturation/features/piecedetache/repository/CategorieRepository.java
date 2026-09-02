package sn.oas.facturation.features.piecedetache.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.piecedetache.data.entity.Categorie;
@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {
    java.util.Optional<Categorie> findByNom(String nom);
}
