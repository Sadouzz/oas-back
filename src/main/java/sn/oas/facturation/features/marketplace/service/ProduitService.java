package sn.oas.facturation.features.marketplace.service;

import sn.oas.facturation.features.marketplace.data.entity.Produit;
import sn.oas.facturation.features.marketplace.dto.ProduitRequest;

import java.util.List;

public interface ProduitService {
    Produit create(ProduitRequest request);

    Produit update(Long id, ProduitRequest request);

    void delete(Long id);

    Produit getById(Long id);

    List<Produit> getAll();

    List<Produit> getDisponibles();

    List<Produit> getArchives();

    List<Produit> search(String keyword);

    Produit toggleDisponibilite(Long id, Boolean disponible);

    Produit archiver(Long id);

    Produit desarchiver(Long id);

    List<Produit> getPopulaires();
}
