package sn.oas.facturation.piecedetache.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.piecedetache.data.entity.PDG;
import sn.oas.facturation.piecedetache.data.entity.PDP;
import sn.oas.facturation.piecedetache.data.entity.PDS;
import sn.oas.facturation.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.piecedetache.data.enums.StatutPiece;
import sn.oas.facturation.piecedetache.data.enums.TypePiece;
import sn.oas.facturation.piecedetache.dto.PieceDetacheRequest;
import sn.oas.facturation.piecedetache.repository.PieceDetacheRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PieceDetacheServiceImpl implements PieceDetacheService {

    private final PieceDetacheRepository pieceDetacheRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;


    @Override
    public List<PieceDetache> getAllPieces() {
        return pieceDetacheRepository.findAll();
    }

    @Override
    public List<PieceDetache> filterByStatut(StatutPiece statut) {
        return pieceDetacheRepository.findByStatut(statut);
    }

    @Override
    public List<PieceDetache> filterByType(TypePiece type) {
        return pieceDetacheRepository.findByType(type);
    }

    @Override
    public List<PieceDetache> searchPieces(String keyword) {
        return pieceDetacheRepository.searchPieces(keyword);
    }

    @Override
    public PieceDetache getById(Long id) {
        return pieceDetacheRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pièce détachée non trouvée"));
    }

    @Transactional
    @Override
    public PieceDetache create(PieceDetacheRequest request) {
        validateCreateRequest(request);

        if (pieceDetacheRepository.existsByNumeroDeSerie(request.numeroDeSerie())) {
            throw new IllegalArgumentException("Numéro de série déjà existant : " + request.numeroDeSerie());
        }

        PieceDetache piece = buildPieceFromRequest(request);
        piece.setType(request.type());
        
        
        return pieceDetacheRepository.save(piece);
    }

    @Transactional
    @Override
    public PieceDetache update(Long id, PieceDetacheRequest request) {
        PieceDetache piece = getById(id);
        piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);

        if (request.numeroDeSerie() != null
                && !request.numeroDeSerie().equalsIgnoreCase(piece.getNumeroDeSerie())) {
            if (pieceDetacheRepository.existsByNumeroDeSerie(request.numeroDeSerie())) {
                throw new IllegalArgumentException("Numéro de série déjà existant : " + request.numeroDeSerie());
            }
            piece.setNumeroDeSerie(request.numeroDeSerie());
        }

        if (request.reference() != null) piece.setReference(request.reference());
        if (request.categorie() != null) piece.setCategorie(request.categorie());
        if (request.pourcentage() != null) piece.setPourcentage(request.pourcentage());
        if (request.statut() != null) piece.setStatut(request.statut());

        if (piece instanceof PDP pdp) {
            if (request.prix() != null) pdp.setPrix(request.prix());
            if (request.seuilMinimum() != null) pdp.setSeuilMinimum(request.seuilMinimum());
        }


        return pieceDetacheRepository.save(piece);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!pieceDetacheRepository.existsById(id)) {
            throw new RuntimeException("Pièce détachée non trouvée");
        }
        pieceDetacheRepository.deleteById(id);
    }

    private void validateCreateRequest(PieceDetacheRequest request) {
        if (request.type() == null) {
            throw new IllegalArgumentException("Le type de pièce est obligatoire");
        }
        if (request.numeroDeSerie() == null || request.numeroDeSerie().isBlank()) {
            throw new IllegalArgumentException("Le numéro de série est obligatoire");
        }
        if (request.reference() == null || request.reference().isBlank()) {
            throw new IllegalArgumentException("La référence est obligatoire");
        }
        if (request.categorie() == null || request.categorie().isBlank()) {
            throw new IllegalArgumentException("La catégorie est obligatoire");
        }
        if (request.pourcentage() == null) {
            throw new IllegalArgumentException("Le pourcentage est obligatoire");
        }
        if (request.type() == TypePiece.PDP) {
            if (request.stockMagasin() == null || request.prix() == null) {
                throw new IllegalArgumentException("Le stock magasin et le prix sont obligatoires pour une PDP");
            }
        }
    }

    private PieceDetache buildPieceFromRequest(PieceDetacheRequest request) {
        StatutPiece statut = request.statut() != null ? request.statut() : StatutPiece.ACTIF;
        String numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.PC);

        return switch (request.type()) {
            case PDP -> {
                int stockMagasin = request.stockMagasin();
                yield PDP.builder()
                        .numero(numero)
                        .numeroDeSerie(request.numeroDeSerie())
                        .reference(request.reference())
                        .categorie(request.categorie())
                        .pourcentage(request.pourcentage())
                        .statut(statut)
                        .stockAtelier(0)
                        .stockMagasin(stockMagasin)
                        .qteReelle(stockMagasin)
                        .prix(request.prix())
                        .seuilMinimum(request.seuilMinimum())
                        .build();
            }
            case PDG -> PDG.builder()
                    .numero(numero)
                    .numeroDeSerie(request.numeroDeSerie())
                    .reference(request.reference())
                    .categorie(request.categorie())
                    .pourcentage(request.pourcentage())
                    .statut(statut)
                    .build();
            case PDS -> PDS.builder()
                    .numero(numero)
                    .numeroDeSerie(request.numeroDeSerie())
                    .reference(request.reference())
                    .categorie(request.categorie())
                    .pourcentage(request.pourcentage())
                    .statut(statut)
                    .build();
        };
    }
}
