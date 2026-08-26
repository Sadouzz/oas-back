package sn.oas.facturation.piecedetache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.enums.TypeAlerte;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.piecedetache.dto.AlerteStockResponse;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlerteServiceImpl implements AlerteService {

    private static final Double SEUIL_GLOBAL = 10.0;

    private final PieceDetacheRepository pieceDetacheRepository;

    @Override
    public List<AlerteStockResponse> getAlertes() {
        return getAllPDP().stream()
                .map(this::evaluer)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public List<AlerteStockResponse> getRuptures() {
        return getAlertes().stream()
                .filter(a -> a.typeAlerte() == TypeAlerte.RUPTURE)
                .toList();
    }

    @Override
    public List<AlerteStockResponse> getStocksFaibles() {
        return getAlertes().stream()
                .filter(a -> a.typeAlerte() == TypeAlerte.STOCK_FAIBLE)
                .toList();
    }

    private Optional<AlerteStockResponse> evaluer(PDP pdp) {
        Double seuil = pdp.getSeuilMinimum() != null ? pdp.getSeuilMinimum() : SEUIL_GLOBAL;

        TypeAlerte type;
        if (pdp.getStockMagasin() == 0) {
            type = TypeAlerte.RUPTURE;
        } else if (pdp.getStockMagasin() <= seuil) {
            type = TypeAlerte.STOCK_FAIBLE;
        } else {
            return Optional.empty();
        }

        return Optional.of(new AlerteStockResponse(
                pdp.getId(),
                pdp.getNumeroDeSerie(),
                pdp.getReference(),
                pdp.getCategorie() != null ? pdp.getCategorie().getNom() : null,
                pdp.getStockMagasin(),
                pdp.getStockAtelier(),
                pdp.getQteReelle(),
                seuil,
                type
        ));
    }

    private List<PDP> getAllPDP() {
        return pieceDetacheRepository.findByType(TypePiece.PDP).stream()
                .map(p -> (PieceDetache) org.hibernate.Hibernate.unproxy(p))
                .filter(p -> p instanceof PDP)
                .map(p -> (PDP) p)
                .toList();
    }
}