package sn.oas.facturation.marketplace.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.marketplace.data.entity.Produit;
import sn.oas.facturation.marketplace.dto.ProduitRequest;
import sn.oas.facturation.marketplace.repository.ProduitRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProduitServiceImpl implements ProduitService {

    private final ProduitRepository produitRepository;

    @Override
    @Transactional
    public Produit create(ProduitRequest request) {
        Produit produit = Produit.builder()
                .nom(request.nom())
                .description(request.description())
                .prix(request.prix())
                .mediaUrl(request.mediaUrl())
                .disponible(request.disponible() != null ? request.disponible() : true)
                .build();
        return produitRepository.save(produit);
    }

    @Override
    @Transactional
    public Produit update(Long id, ProduitRequest request) {
        Produit produit = getById(id);
        produit.setNom(request.nom());
        produit.setDescription(request.description());
        produit.setPrix(request.prix());
        produit.setMediaUrl(request.mediaUrl());
        produit.setDisponible(request.disponible() != null ? request.disponible() : produit.getDisponible());
        return produitRepository.save(produit);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Produit produit = getById(id);
        produit.setArchive(true);
        produitRepository.save(produit);
    }

    @Override
    @Transactional(readOnly = true)
    public Produit getById(Long id) {
        return produitRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable avec l'id : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produit> getAll() {
        return produitRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produit> getDisponibles() {
        return produitRepository.findByArchiveFalseAndDisponibleTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produit> getArchives() {
        return produitRepository.findByArchiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produit> search(String keyword) {
        return produitRepository.searchDisponibles(keyword);
    }

    @Override
    @Transactional
    public Produit toggleDisponibilite(Long id, Boolean disponible) {
        Produit produit = getById(id);
        produit.setDisponible(disponible != null ? disponible : !Boolean.TRUE.equals(produit.getDisponible()));
        return produitRepository.save(produit);
    }

    @Override
    @Transactional
    public Produit archiver(Long id) {
        Produit produit = getById(id);
        produit.setArchive(true);
        return produitRepository.save(produit);
    }

    @Override
    @Transactional
    public Produit desarchiver(Long id) {
        Produit produit = getById(id);
        produit.setArchive(false);
        return produitRepository.save(produit);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Produit> getPopulaires() {
        return produitRepository.findTop5ByArchiveFalseAndDisponibleTrueOrderByIdDesc();
    }
}
