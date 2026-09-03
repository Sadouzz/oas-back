package sn.oas.facturation.features.categorie_pieces.service;

import org.springframework.data.domain.Page;
import sn.oas.facturation.features.categorie_pieces.data.entity.Categorie;
import sn.oas.facturation.features.categorie_pieces.dto.request.CategorieRequest;

import java.util.List;

public interface CategorieService {
    Categorie createCategorie(CategorieRequest request);
    Page<Categorie> getAllCategories(int page, int size);
    List<Categorie> getAllCategories();
    Categorie getCategorieById(Long id);
    List<Categorie> getCategoriesByDepotId(Long depotId);
    Page<Categorie> getCategoriesByDepotId(Long depotId, int page, int size);
    Categorie updateCategorie(Long id, CategorieRequest request);
    void deleteCategorie(Long id);
    List<Categorie> searchCategories(String keyword);
    Page<Categorie> searchCategories(String keyword, int page, int size);
}
