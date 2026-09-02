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
import sn.oas.facturation.features.piecedetache.dto.InventaireRequest;
import sn.oas.facturation.features.piecedetache.dto.InventaireResponse;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.piecedetache.repository.StockMouvementRepository;

@Service
@RequiredArgsConstructor
public class InventaireServiceImpl implements InventaireService {

    private final PieceDetacheRepository pieceDetacheRepository;
    private final StockMouvementRepository stockMouvementRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public InventaireResponse compterPiece(InventaireRequest request) {
        PDP pdp = getPDP(request.pieceId());
        validerRequest(request);

        Double stockMagasin = pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0.0;
        Double stockAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;
        Double ecartMagasin = request.stockMagasinPhysique() - stockMagasin;
        Double ecartAtelier = request.stockAtelierPhysique() - stockAtelier;

        boolean aUnEcart = ecartMagasin != 0 || ecartAtelier != 0;

        if (!aUnEcart) {
            return new InventaireResponse(
                    pdp.getId(), pdp.getReference(), pdp.getDesignation(),
                    stockMagasin, stockAtelier,
                    request.stockMagasinPhysique(), request.stockAtelierPhysique(),
                    0.0, 0.0, false, (StockMouvement) null
            );
        }

        Agent agent = getAgentConnecte();
        Double quantite = Math.abs(ecartMagasin) + Math.abs(ecartAtelier);

        pdp.setStockMagasin(request.stockMagasinPhysique());
        pdp.setStockAtelier(request.stockAtelierPhysique());
        pdp.setQteReelle(request.stockMagasinPhysique() + request.stockAtelierPhysique());
        pieceDetacheRepository.save(pdp);

        StockMouvement mouvement = stockMouvementRepository.save(StockMouvement.builder()
                .type(TypeMouvement.INVENTAIRE)
                .quantite(quantite)
                .stockMagasinAvant(stockMagasin)
                .stockAtelierAvant(stockAtelier)
                .stockMagasinApres(request.stockMagasinPhysique())
                .stockAtelierApres(request.stockAtelierPhysique())
                .stockReelApres(pdp.getQteReelle())
                .prenom(agent != null ? agent.getFirstName() : "")
                .nom(agent != null ? agent.getLastName() : "")
                .numDocument("INVENTAIRE")
                .typeDocument("Inventaire")
                .numeroSerie(pdp.getReference())
                .motif(request.motif())
                .piece(pdp)
                .agent(agent)
                .build());

        return new InventaireResponse(
                pdp.getId(), pdp.getReference(), pdp.getDesignation(),
                stockMagasin, stockAtelier,
                request.stockMagasinPhysique(), request.stockAtelierPhysique(),
                ecartMagasin, ecartAtelier, true, mouvement
        );
    }

    private void validerRequest(InventaireRequest request) {
        if (request.stockMagasinPhysique() == null || request.stockMagasinPhysique() < 0) {
            throw new IllegalArgumentException("Le stock magasin physique ne peut pas être négatif");
        }
        if (request.stockAtelierPhysique() == null || request.stockAtelierPhysique() < 0) {
            throw new IllegalArgumentException("Le stock atelier physique ne peut pas être négatif");
        }
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
