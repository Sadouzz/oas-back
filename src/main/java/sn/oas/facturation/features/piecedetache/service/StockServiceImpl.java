package sn.oas.facturation.features.piecedetache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.data.entity.Agent;
import sn.oas.facturation.features.auth.data.entity.User;
import sn.oas.facturation.features.auth.repository.UserRepository;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.entity.StockMouvement;
import sn.oas.facturation.features.piecedetache.data.enums.TypeMouvement;
import sn.oas.facturation.features.piecedetache.dto.AjustementStockRequest;
import sn.oas.facturation.features.piecedetache.dto.EntreeStockRequest;
import sn.oas.facturation.features.piecedetache.dto.SortieStockRequest;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.StockMouvementRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockMouvementRepository stockMouvementRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public StockMouvement entree(EntreeStockRequest request) {
        PDP pdp = getPDP(request.pieceId());
        Agent agent = getAgentConnecte();

        if (request.quantite() == null || request.quantite() <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro");
        }

        Double magasinAvant = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
        Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

        pdp.setStockMagasin(magasinAvant + request.quantite());
        pdp.setQteReelle(pdp.getStockMagasin() + pdp.getStockAtelier());
        pieceDetacheRepository.save(pdp);

        return stockMouvementRepository.save(StockMouvement.builder()
                .type(TypeMouvement.ENTREE)
                .quantite(request.quantite().doubleValue())
                .stockMagasinAvant(magasinAvant)
                .stockAtelierAvant(atelierAvant)
                .stockMagasinApres(pdp.getStockMagasin())
                .stockAtelierApres(pdp.getStockAtelier())
                .stockReelApres(pdp.getQteReelle())
                .prenom(agent != null ? agent.getFirstName() : "")
                .nom(agent != null ? agent.getLastName() : "")
                .numDocument("ENTREE")
                .typeDocument("Entrée stock")
                .numeroSerie(pdp.getReference())
                .motif(request.motif())
                .piece(pdp)
                .agent(agent)
                .build());
    }

    @Transactional
    @Override
    public StockMouvement sortie(SortieStockRequest request) {
        PDP pdp = getPDP(request.pieceId());
        Agent agent = getAgentConnecte();

        if (request.quantite() == null || request.quantite() <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro");
        }
        Double stockMagasinDisponible = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
        if (stockMagasinDisponible < request.quantite()) {
            throw new IllegalArgumentException(
                    "Stock magasin insuffisant. Disponible : " + stockMagasinDisponible);
        }

        Double magasinAvant = stockMagasinDisponible;
        Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;

        pdp.setStockMagasin(magasinAvant - request.quantite());
        pdp.setStockAtelier(atelierAvant + request.quantite());
        pdp.setQteReelle(pdp.getStockMagasin() + pdp.getStockAtelier());
        pieceDetacheRepository.save(pdp);

        return stockMouvementRepository.save(StockMouvement.builder()
                .type(TypeMouvement.SORTIE_MAGASIN_VERS_ATELIER)
                .quantite(request.quantite().doubleValue())
                .stockMagasinAvant(magasinAvant)
                .stockAtelierAvant(atelierAvant)
                .stockMagasinApres(pdp.getStockMagasin())
                .stockAtelierApres(pdp.getStockAtelier())
                .stockReelApres(pdp.getQteReelle())
                .prenom(agent != null ? agent.getFirstName() : "")
                .nom(agent != null ? agent.getLastName() : "")
                .numDocument("SORTIE")
                .typeDocument("Sortie magasin")
                .numeroSerie(pdp.getReference())
                .motif(request.motif())
                .piece(pdp)
                .agent(agent)
                .build());
    }

    @Transactional
    @Override
    public StockMouvement ajustement(AjustementStockRequest request) {
        PDP pdp = getPDP(request.pieceId());
        Agent agent = getAgentConnecte();

        if (request.stockMagasin() == null || request.stockMagasin() < 0) {
            throw new IllegalArgumentException("Le stock magasin ne peut pas être négatif");
        }
        if (request.stockAtelier() == null || request.stockAtelier() < 0) {
            throw new IllegalArgumentException("Le stock atelier ne peut pas être négatif");
        }

        Double magasinAvant = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
        Double atelierAvant = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;
        Double quantite = Math.abs(
                (request.stockMagasin() + request.stockAtelier()) - (magasinAvant + atelierAvant));

        pdp.setStockMagasin(request.stockMagasin() != null ? request.stockMagasin().doubleValue() : 0.0);
        pdp.setStockAtelier(request.stockAtelier() != null ? request.stockAtelier().doubleValue() : 0.0);
        pdp.setQteReelle((request.stockMagasin() != null ? request.stockMagasin().doubleValue() : 0.0) + (request.stockAtelier() != null ? request.stockAtelier().doubleValue() : 0.0));
        pieceDetacheRepository.save(pdp);

        return stockMouvementRepository.save(StockMouvement.builder()
                .type(TypeMouvement.AJUSTEMENT)
                .quantite(quantite.doubleValue())
                .stockMagasinAvant(magasinAvant)
                .stockAtelierAvant(atelierAvant)
                .stockMagasinApres(pdp.getStockMagasin())
                .stockAtelierApres(pdp.getStockAtelier())
                .stockReelApres(pdp.getQteReelle())
                .prenom(agent != null ? agent.getFirstName() : "")
                .nom(agent != null ? agent.getLastName() : "")
                .numDocument("AJUSTEMENT")
                .typeDocument("Ajustement")
                .numeroSerie(pdp.getReference())
                .motif(request.motif())
                .piece(pdp)
                .agent(agent)
                .build());
    }

    @Override
    public List<StockMouvement> getHistoriquePiece(Long pieceId) {
        PDP pdp = getPDP(pieceId);
        return stockMouvementRepository.findByPieceOrderByDateOperationDesc(pdp);
    }

    @Override
    public List<StockMouvement> getHistoriquePieceByType(Long pieceId, TypeMouvement type) {
        PDP pdp = getPDP(pieceId);
        return stockMouvementRepository.findByPieceAndTypeOrderByDateOperationDesc(pdp, type);
    }

    @Override
    public List<StockMouvement> getHistoriqueGlobal(LocalDateTime debut, LocalDateTime fin, Long pieceId, String categorie, TypeMouvement type) {
        return stockMouvementRepository.findFiltered(debut, fin, pieceId, categorie, type);
    }

    private PDP getPDP(Long pieceId) {
        PieceDetache piece = pieceDetacheRepository.findById(pieceId)
                .orElseThrow(() -> new RuntimeException("Pièce introuvable avec l'id : " + pieceId));
        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
        if (!(piece instanceof PDP pdp)) {
            throw new IllegalArgumentException("La pièce id=" + pieceId + " n'est pas une PDP");
        }
        return pdp;
    }

    private Agent getAgentConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
        if (!(user instanceof Agent agent)) {
            throw new IllegalStateException("Cette opération requiert un compte Agent");
        }
        return agent;
    }
}
