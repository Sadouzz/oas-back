package sn.oas.facturation.features.piecedetache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.piecedetache.data.enums.TypeAlerte;
import sn.oas.facturation.features.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.features.piecedetache.dto.AlerteStockResponse;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;

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
    public org.springframework.data.domain.Page<AlerteStockResponse> getAlertes(int page, int size) {
        return paginate(getAlertes(), page, size);
    }

    @Override
    public List<AlerteStockResponse> getRuptures() {
        return getAlertes().stream()
                .filter(a -> a.typeAlerte() == TypeAlerte.RUPTURE)
                .toList();
    }

    @Override
    public org.springframework.data.domain.Page<AlerteStockResponse> getRuptures(int page, int size) {
        return paginate(getRuptures(), page, size);
    }

    @Override
    public List<AlerteStockResponse> getStocksFaibles() {
        return getAlertes().stream()
                .filter(a -> a.typeAlerte() == TypeAlerte.STOCK_FAIBLE)
                .toList();
    }

    @Override
    public org.springframework.data.domain.Page<AlerteStockResponse> getStocksFaibles(int page, int size) {
        return paginate(getStocksFaibles(), page, size);
    }

    private org.springframework.data.domain.Page<AlerteStockResponse> paginate(List<AlerteStockResponse> list, int page, int size) {
        int fromIndex = Math.min(page * size, list.size());
        int toIndex = Math.min(fromIndex + size, list.size());
        List<AlerteStockResponse> subList = list.subList(fromIndex, toIndex);
        return new org.springframework.data.domain.PageImpl<>(subList, org.springframework.data.domain.PageRequest.of(page, size), list.size());
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
                pdp.getReference(),
                pdp.getDesignation(),
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