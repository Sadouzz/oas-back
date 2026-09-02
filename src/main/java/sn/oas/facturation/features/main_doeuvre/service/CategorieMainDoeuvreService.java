package sn.oas.facturation.features.main_doeuvre.service;

import sn.oas.facturation.features.main_doeuvre.data.entity.CategorieMainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.dto.CategorieMainDoeuvreRequest;

import java.util.List;

public interface CategorieMainDoeuvreService {
    List<CategorieMainDoeuvre> getAllCategories();
    CategorieMainDoeuvre getCategorieById(Long id);
    CategorieMainDoeuvre createCategorie(CategorieMainDoeuvreRequest request);
    CategorieMainDoeuvre updateCategorie(Long id, CategorieMainDoeuvreRequest request);
    void deleteCategorie(Long id);
}
