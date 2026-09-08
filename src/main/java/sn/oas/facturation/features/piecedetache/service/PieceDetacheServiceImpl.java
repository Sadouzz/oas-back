package sn.oas.facturation.features.piecedetache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.piecedetache.data.entity.PDG;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PDS;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.categorie_pieces.repository.CategorieRepository;
import sn.oas.facturation.features.piecedetache.dto.PieceDetacheRequest;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import sn.oas.facturation.features.piecedetache.dto.PieceStatsResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class PieceDetacheServiceImpl implements PieceDetacheService {

    private final PieceDetacheRepository pieceDetacheRepository;
    private final CategorieRepository categorieRepository;
    private final AlerteService alerteService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    public Page<PieceDetache> getAllPieces(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PieceDetache> pageResult = pieceDetacheRepository.findAll(pageable);
        setEstUtiliseFlag(pageResult.getContent());
        return pageResult;
    }

    @Override
    public List<PieceDetache> getAllPieces() {
        List<PieceDetache> pieces = pieceDetacheRepository.findAll();
        setEstUtiliseFlag(pieces);
        return pieces;
    }

    @Override
    public List<PieceDetache> filterByType(TypePiece type) {
        List<PieceDetache> pieces = pieceDetacheRepository.findByType(type);
        setEstUtiliseFlag(pieces);
        return pieces;
    }

    @Override
    public Page<PieceDetache> filterByType(TypePiece type, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PieceDetache> pageResult = pieceDetacheRepository.findByType(type, pageable);
        setEstUtiliseFlag(pageResult.getContent());
        return pageResult;
    }

    @Override
    public List<PieceDetache> searchPieces(String keyword) {
        List<PieceDetache> pieces = pieceDetacheRepository.searchPieces(keyword);
        setEstUtiliseFlag(pieces);
        return pieces;
    }

    @Override
    public Page<PieceDetache> searchPieces(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<PieceDetache> pageResult = pieceDetacheRepository.searchPieces(keyword, pageable);
        setEstUtiliseFlag(pageResult.getContent());
        return pageResult;
    }

    private void setEstUtiliseFlag(List<PieceDetache> pieces) {
        if (pieces.isEmpty()) return;
        List<Long> usedIds = pieceDetacheRepository.getUsedPiecesIds();
        Set<Long> usedIdsSet = new HashSet<>(usedIds);
        for (PieceDetache p : pieces) {
            p.setEstUtilise(usedIdsSet.contains(p.getId()));
        }
    }

    @Override
    public PieceDetache getById(Long id) {
        return pieceDetacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pièce détachée non trouvée"));
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "piece_stats", keyGenerator = "tenantKeyGenerator")
    public PieceStatsResponse getStats() {
        List<PieceDetache> allPieces = pieceDetacheRepository.findAll();
        long totalArticles = allPieces.size();
        double valeurStock = allPieces.stream()
                .mapToDouble(p -> {
                    double stock = (p.getStockMagasin() != null ? p.getStockMagasin() : 0.0)
                            + (p.getStockAtelier() != null ? p.getStockAtelier() : 0.0);
                    double prix = p.getPrixUnitaire() != null ? p.getPrixUnitaire()
                            : (p.getPrixGros() != null ? p.getPrixGros() : 0.0);
                    return stock * prix;
                })
                .sum();
        long ruptures = alerteService.getRuptures().size();
        long stockCritique = alerteService.getStocksFaibles().size();
        return new PieceStatsResponse(totalArticles, valeurStock, stockCritique, ruptures);
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })
    public PieceDetache create(PieceDetacheRequest request) {
        validateCreateRequest(request);

        if (pieceDetacheRepository.existsByReference(request.reference())) {
            throw new IllegalArgumentException("Référence déjà existante : " + request.reference());
        }

        PieceDetache piece = buildPieceFromRequest(request);
        piece.setType(request.type());
        
        
        return pieceDetacheRepository.save(piece);
    }

    @Transactional
    @Override
    @Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })
    public PieceDetache update(Long id, PieceDetacheRequest request) {
        PieceDetache piece = getById(id);
        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);

        if (request.reference() != null
                && !request.reference().equalsIgnoreCase(piece.getReference())) {
            if (pieceDetacheRepository.existsByReference(request.reference())) {
                throw new IllegalArgumentException("Référence déjà existante : " + request.reference());
            }
            piece.setReference(request.reference());
        }

        if (request.designation() != null) piece.setDesignation(request.designation());
        if (request.categorie() != null) piece.setCategorie(categorieRepository.findByNom(request.categorie()).orElse(null));
        if (piece instanceof PDP pdp) {
            if (request.prix() != null) pdp.setPrixUnitaire(request.prix());
            if (request.seuilMinimum() != null) pdp.setSeuilMinimum(request.seuilMinimum());
            if (request.stockMagasin() != null) pdp.setStockMagasin(request.stockMagasin().doubleValue());
        }


        return pieceDetacheRepository.save(piece);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })
    public void delete(Long id) {
        if (!pieceDetacheRepository.existsById(id)) {
            throw new RuntimeException("Pièce détachée non trouvée");
        }
        
        if (pieceDetacheRepository.isPieceUsed(id)) {
            // L'article est utilisé dans une relation, on l'archive
            PieceDetache piece = pieceDetacheRepository.findById(id).orElseThrow();
            piece.setStatut(StatutPiece.ARCHIVE);
            pieceDetacheRepository.save(piece);
        } else {
            // Aucune relation, on peut supprimer
            pieceDetacheRepository.deleteById(id);
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })
    public PieceDetache restore(Long id) {
        PieceDetache piece = pieceDetacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pièce détachée non trouvée"));
        if (piece.getStatut() == StatutPiece.ARCHIVE) {
            piece.setStatut(StatutPiece.ACTIF);
        }
        return pieceDetacheRepository.save(piece);
    }

    private void validateCreateRequest(PieceDetacheRequest request) {
        if (request.type() == null) {
            throw new IllegalArgumentException("Le type de pièce est obligatoire");
        }
        if (request.reference() == null || request.reference().isBlank()) {
            throw new IllegalArgumentException("La référence est obligatoire");
        }
        if (request.designation() == null || request.designation().isBlank()) {
            throw new IllegalArgumentException("La désignation est obligatoire");
        }
        if (request.categorie() == null || request.categorie().isBlank()) {
            throw new IllegalArgumentException("La catégorie est obligatoire");
        }
        // if (request.pourcentage() == null) {
        //     throw new IllegalArgumentException("Le pourcentage est obligatoire");
        // }
        if (request.type() == TypePiece.PDP) {
            if (request.stockMagasin() == null || request.prix() == null) {
                throw new IllegalArgumentException("Le stock magasin et le prix sont obligatoires pour une PDP");
            }
        }
    }

    private PieceDetache buildPieceFromRequest(PieceDetacheRequest request) {
        StatutPiece statut = StatutPiece.ACTIF;
        String numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.PC);

        return switch (request.type()) {
            case PDP -> {
                Double stockMagasin = request.stockMagasin() != null ? request.stockMagasin() : 0.0;
                yield PDP.builder()
                        .numero(numero)
                        .reference(request.reference())
                        .designation(request.designation())
                        .categorie(request.categorie() != null ? categorieRepository.findByNom(request.categorie()).orElse(null) : null)
                        .stockAtelier(0.0)
                        .stockMagasin(stockMagasin)
                        .qteReelle(stockMagasin)
                        .prixUnitaire(request.prix())
                        .seuilMinimum(request.seuilMinimum())
                        .build();
            }
            case PDG -> PDG.builder()
                    .numero(numero)
                    .reference(request.reference())
                    .designation(request.designation())
                    .categorie(request.categorie() != null ? categorieRepository.findByNom(request.categorie()).orElse(null) : null)
                    .build();
            case PDS -> PDS.builder()
                    .numero(numero)
                    .reference(request.reference())
                    .designation(request.designation())
                    .categorie(request.categorie() != null ? categorieRepository.findByNom(request.categorie()).orElse(null) : null)
                    .build();
        };
    }
}
