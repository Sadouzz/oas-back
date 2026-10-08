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
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.time.Year;
import java.util.ArrayList;
import sn.oas.facturation.features.categorie_pieces.data.entity.Categorie;
import sn.oas.facturation.features.depot_pieces.data.entity.Depot;
import sn.oas.facturation.features.depot_pieces.repository.DepotRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.piecedetache.dto.PieceStatsResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class PieceDetacheServiceImpl implements PieceDetacheService {

    private final PieceDetacheRepository pieceDetacheRepository;
    private final CategorieRepository categorieRepository;
    private final DepotRepository depotRepository;
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

    @Override
    public List<PieceDetache> filterByDepot(Long depotId) {
        return getPieces(null, depotId, null);
    }

    @Override
    public Page<PieceDetache> filterByDepot(Long depotId, int page, int size) {
        return getPieces(null, depotId, null, page, size);
    }

    @Override
    public Page<PieceDetache> getPieces(TypePiece type, Long depotId, String keyword, int page, int size) {
        return getPieces(type, null, depotId, null, keyword, page, size);
    }

    @Override
    public List<PieceDetache> getPieces(TypePiece type, Long depotId, String keyword) {
        return getPieces(type, null, depotId, null, keyword);
    }

    @Override
    public Page<PieceDetache> getPieces(TypePiece type, StatutPiece statut, Long depotId, String depotNom, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        if (type == null && statut == null && depotId == null && (depotNom == null || depotNom.isBlank()) && (keyword == null || keyword.trim().isEmpty())) {
            Page<PieceDetache> pageResult = pieceDetacheRepository.findAll(pageable);
            setEstUtiliseFlag(pageResult.getContent());
            return pageResult;
        }
        Specification<PieceDetache> spec = buildFilterSpec(type, statut, depotId, depotNom, keyword);
        Page<PieceDetache> pageResult = pieceDetacheRepository.findAll(spec, pageable);
        setEstUtiliseFlag(pageResult.getContent());
        return pageResult;
    }

    @Override
    public List<PieceDetache> getPieces(TypePiece type, StatutPiece statut, Long depotId, String depotNom, String keyword) {
        if (type == null && statut == null && depotId == null && (depotNom == null || depotNom.isBlank()) && (keyword == null || keyword.trim().isEmpty())) {
            List<PieceDetache> pieces = pieceDetacheRepository.findAll(Sort.by("id").descending());
            setEstUtiliseFlag(pieces);
            return pieces;
        }
        Specification<PieceDetache> spec = buildFilterSpec(type, statut, depotId, depotNom, keyword);
        List<PieceDetache> pieces = pieceDetacheRepository.findAll(spec, Sort.by("id").descending());
        setEstUtiliseFlag(pieces);
        return pieces;
    }

    private Specification<PieceDetache> buildFilterSpec(TypePiece type, StatutPiece statut, Long depotId, String depotNom, String keyword) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }

            if (statut != null) {
                predicates.add(cb.equal(root.get("statut"), statut));
            }

            boolean hasDepotId = (depotId != null);
            boolean hasDepotNom = (depotNom != null && !depotNom.isBlank());
            boolean hasKeyword = (keyword != null && !keyword.trim().isEmpty());

            Join<PieceDetache, Depot> depotJoin = null;
            if (hasDepotId || hasDepotNom) {
                depotJoin = root.join("depot", JoinType.LEFT);
            }

            if (hasDepotId && depotJoin != null) {
                predicates.add(cb.equal(depotJoin.get("id"), depotId));
            }
            if (hasDepotNom && depotJoin != null) {
                predicates.add(cb.like(cb.lower(depotJoin.get("nom")), "%" + depotNom.trim().toLowerCase() + "%"));
            }

            Join<PieceDetache, Categorie> catJoin = null;
            if (hasKeyword) {
                catJoin = root.join("categorie", JoinType.LEFT);
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate refPred = cb.like(cb.lower(root.get("reference")), pattern);
                Predicate desPred = cb.like(cb.lower(root.get("designation")), pattern);
                Predicate numPred = cb.like(cb.lower(root.get("numero")), pattern);
                Predicate catPred = catJoin != null ? cb.like(cb.lower(catJoin.get("nom")), pattern) : cb.disjunction();

                predicates.add(cb.or(refPred, desPred, numPred, catPred));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
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
    //@Cacheable(value = "piece_stats", keyGenerator = "tenantKeyGenerator")
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
    /*@Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })*/
    public PieceDetache create(PieceDetacheRequest request) {
        validateCreateRequest(request);

        Garage currentGarage = documentNumberGeneratorService.getCurrentGarage();
        Long garageId = currentGarage != null ? currentGarage.getId() : null;

        String ref = request.reference() != null ? request.reference().trim() : "";
        String des = request.designation() != null ? request.designation().trim() : "";

        boolean refExists = (garageId != null)
                ? pieceDetacheRepository.existsByGarageIdAndReferenceIgnoreCase(garageId, ref)
                : pieceDetacheRepository.existsByReferenceIgnoreCase(ref);
        if (refExists) {
            throw new IllegalArgumentException("Une pièce avec la référence '" + ref + "' existe déjà dans ce garage.");
        }

        boolean desExists = (garageId != null)
                ? pieceDetacheRepository.existsByGarageIdAndDesignationIgnoreCase(garageId, des)
                : pieceDetacheRepository.existsByDesignationIgnoreCase(des);
        if (desExists) {
            throw new IllegalArgumentException("Une pièce avec la désignation '" + des + "' existe déjà dans ce garage.");
        }

        PieceDetache piece = buildPieceFromRequest(request);
        piece.setType(request.type());
        
        return pieceDetacheRepository.save(piece);
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })*/
    public PieceDetache update(Long id, PieceDetacheRequest request) {
        PieceDetache piece = getById(id);
        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);

        Garage currentGarage = documentNumberGeneratorService.getCurrentGarage();
        Long garageId = currentGarage != null ? currentGarage.getId() : null;

        if (request.reference() != null
                && !request.reference().trim().equalsIgnoreCase(piece.getReference())) {
            String newRef = request.reference().trim();
            boolean refExists = (garageId != null)
                    ? pieceDetacheRepository.existsByGarageIdAndReferenceIgnoreCaseAndIdNot(garageId, newRef, id)
                    : pieceDetacheRepository.existsByReferenceIgnoreCaseAndIdNot(newRef, id);
            if (refExists) {
                throw new IllegalArgumentException("Une pièce avec la référence '" + newRef + "' existe déjà dans ce garage.");
            }
            piece.setReference(newRef);
            piece.setNumero(generatePieceNumero(piece.getType(), newRef));
        }

        if (request.designation() != null
                && !request.designation().trim().equalsIgnoreCase(piece.getDesignation())) {
            String newDes = request.designation().trim();
            boolean desExists = (garageId != null)
                    ? pieceDetacheRepository.existsByGarageIdAndDesignationIgnoreCaseAndIdNot(garageId, newDes, id)
                    : pieceDetacheRepository.existsByDesignationIgnoreCaseAndIdNot(newDes, id);
            if (desExists) {
                throw new IllegalArgumentException("Une pièce avec la désignation '" + newDes + "' existe déjà dans ce garage.");
            }
            piece.setDesignation(newDes);
        }

        if (request.categorie() != null) piece.setCategorie(resolveCategorie(request.categorie()));
        
        Depot depot = resolveDepot(request);
        if (depot != null) {
            piece.setDepot(depot);
        }

        if (piece instanceof PDP pdp) {
            if (request.prixUnitaire() != null) pdp.setPrixUnitaire(request.prixUnitaire());
            if (request.prixGros() != null) pdp.setPrixGros(request.prixGros());
            if (request.pourcentage() != null) pdp.setPourcentage(request.pourcentage());
            if (request.seuilMinimum() != null) pdp.setSeuilMinimum(request.seuilMinimum());
            if (request.stockMagasin() != null) pdp.setStockMagasin(request.stockMagasin().doubleValue());
        } else if (piece instanceof PDG pdg) {
            if (request.prixUnitaire() != null) pdg.setPrixUnitaire(request.prixUnitaire());
        }

        return pieceDetacheRepository.save(piece);
    }

    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })*/
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
    /*@Caching(evict = {
            @CacheEvict(value = "piece_stats", allEntries = true),
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_agent_magasin", allEntries = true)
    })*/
    public PieceDetache restore(Long id) {
        PieceDetache piece = pieceDetacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pièce détachée non trouvée"));
        if (piece.getStatut() == StatutPiece.ARCHIVE) {
            piece.setStatut(StatutPiece.ACTIF);
        }
        return pieceDetacheRepository.save(piece);
    }

    private Categorie resolveCategorie(String categorieNom) {
        if (categorieNom == null || categorieNom.isBlank()) return null;
        Garage currentGarage = documentNumberGeneratorService.getCurrentGarage();
        Long garageId = currentGarage != null ? currentGarage.getId() : null;
        if (garageId != null) {
            return categorieRepository.findFirstByGarageIdAndNomIgnoreCase(garageId, categorieNom.trim())
                    .or(() -> categorieRepository.findFirstByNomIgnoreCase(categorieNom.trim()))
                    .orElse(null);
        }
        return categorieRepository.findFirstByNomIgnoreCase(categorieNom.trim()).orElse(null);
    }

    private Depot resolveDepot(PieceDetacheRequest request) {
        Garage currentGarage = documentNumberGeneratorService.getCurrentGarage();
        Long garageId = currentGarage != null ? currentGarage.getId() : null;

        if (request.type() == TypePiece.PDG) {
            if (garageId != null) {
                return depotRepository.findFirstByGarageIdAndNomIgnoreCase(garageId, "PDG")
                        .or(() -> depotRepository.findFirstByNomIgnoreCase("PDG"))
                        .orElseGet(() -> {
                            Depot newDepot = Depot.builder()
                                    .nom("PDG")
                                    .description("Dépôt automatique pour pièces générées (PDG)")
                                    .garage(currentGarage)
                                    .build();
                            return depotRepository.save(newDepot);
                        });
            }
            return depotRepository.findFirstByNomIgnoreCase("PDG")
                    .orElseGet(() -> {
                        Depot newDepot = Depot.builder()
                                .nom("PDG")
                                .description("Dépôt automatique pour pièces générées (PDG)")
                                .garage(currentGarage)
                                .build();
                        return depotRepository.save(newDepot);
                    });
        }
        if (request.depotId() != null) {
            return depotRepository.findById(request.depotId()).orElse(null);
        }
        if (request.depot() != null && !request.depot().isBlank()) {
            if (garageId != null) {
                return depotRepository.findFirstByGarageIdAndNomIgnoreCase(garageId, request.depot().trim())
                        .or(() -> depotRepository.findFirstByNomIgnoreCase(request.depot().trim()))
                        .orElse(null);
            }
            return depotRepository.findFirstByNomIgnoreCase(request.depot().trim()).orElse(null);
        }
        return null;
    }

    private String generatePieceNumero(TypePiece type, String reference) {
        Garage garage = documentNumberGeneratorService.getCurrentGarage();
        String prefixe = (garage != null && garage.getPrefixe() != null && !garage.getPrefixe().isBlank())
                ? garage.getPrefixe()
                : "GAR";
        String typeCode = type != null ? type.name() : "PDP";
        String ref = reference != null ? reference.trim() : "";
        return String.format("%s-%s-%s", prefixe, typeCode, ref);
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
            if (request.stockMagasin() == null || request.prixUnitaire() == null) {
                throw new IllegalArgumentException("Le stock magasin et le prix unitaire sont obligatoires pour une PDP");
            }
        }
    }

    private PieceDetache buildPieceFromRequest(PieceDetacheRequest request) {
        StatutPiece statut = StatutPiece.ACTIF;
        String numero = generatePieceNumero(request.type(), request.reference());
        Depot depot = resolveDepot(request);
        Categorie categorie = resolveCategorie(request.categorie());

        return switch (request.type()) {
            case PDP -> {
                Double stockMagasin = request.stockMagasin() != null ? request.stockMagasin() : 0.0;
                yield PDP.builder()
                        .numero(numero)
                        .reference(request.reference())
                        .designation(request.designation())
                        .depot(depot)
                        .categorie(categorie)
                        .stockAtelier(0.0)
                        .stockMagasin(stockMagasin)
                        .qteReelle(stockMagasin)
                        .prixUnitaire(request.prixUnitaire())
                        .prixGros(request.prixGros())
                        .pourcentage(request.pourcentage() != null ? request.pourcentage() : 0.0)
                        .seuilMinimum(request.seuilMinimum())
                        .build();
            }
            case PDG -> PDG.builder()
                    .numero(numero)
                    .reference(request.reference())
                    .designation(request.designation())
                    .depot(depot)
                    .categorie(categorie)
                    .prixUnitaire(request.prixUnitaire())
                    .build();
            case PDS -> PDS.builder()
                    .numero(numero)
                    .reference(request.reference())
                    .designation(request.designation())
                    .depot(depot)
                    .categorie(categorie)
                    .build();
        };
    }
}
