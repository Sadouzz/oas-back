package sn.oas.facturation.features.ordreReparation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sn.oas.facturation.features.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.features.bonDeCommande.data.entity.LigneBonDeCommandePiece;
import sn.oas.facturation.features.bonDeCommande.data.enums.StatutBonCommande;
import sn.oas.facturation.features.bonDeCommande.repository.BonDeCommandeRepository;
import sn.oas.facturation.features.devisPrevisionnel.data.entity.DevisPrevisionnel;
import sn.oas.facturation.features.devisPrevisionnel.repository.DevisPrevisionnelRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.facturation.data.enums.StatutFacturation;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
import sn.oas.facturation.features.piecedetache.repository.PieceDetacheRepository;
import sn.oas.facturation.features.proforma.repository.ProformaRepository;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationRequest;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.repository.VehiculeRepository;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.technicien.repository.TechnicienRepository;
import sn.oas.facturation.features.user.data.enums.Role;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationPiece;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneReceptionOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneTravailOrdre;
import sn.oas.facturation.features.ordreReparation.data.entity.LigneOrdreReparationMainDoeuvre;
import sn.oas.facturation.features.ordreReparation.dto.LigneOrdreReparationPieceRequest;
import sn.oas.facturation.features.ordreReparation.dto.LigneOrdreReparationMainDoeuvreRequest;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.main_doeuvre.repository.MainDoeuvreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

import sn.oas.facturation.features.notification.service.AgentNotificationService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import sn.oas.facturation.features.proforma.service.ProformaService;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.proforma.dto.ProformaCreateRequest;
import sn.oas.facturation.features.proforma.dto.ProformaUpdateRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationPieceRequest;
import sn.oas.facturation.features.facturation.dto.LigneFacturationMainDoeuvreRequest;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationLightDTO;
import sn.oas.facturation.features.ordreReparation.dto.OrdreReparationResponseDTO;
import sn.oas.facturation.features.ordreReparation.dto.VehiculeLightDTO;
import sn.oas.facturation.features.ordreReparation.dto.responses.ProformaSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.ClientLightDTO;
import sn.oas.facturation.features.diagnostic.data.entity.Diagnostic;
import sn.oas.facturation.features.diagnostic.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.features.diagnostic.data.entity.RemarqueDiagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.TypePieceJointe;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.repository.DiagnosticRepository;
import sn.oas.facturation.features.diagnostic.repository.PieceJointeDiagnosticRepository;
import sn.oas.facturation.features.diagnostic.repository.RemarqueDiagnosticRepository;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.dto.FicheAtelierDetailsResponse;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.notification.service.EmailService;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.LigneProformaMoDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs.LigneProformaPieceDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.ClientHeaderDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.LigneMoOrdreDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.LignePieceOrdreDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.OrdreReparationSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.PieceSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepAssignationResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepBonSortieResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepDiagnosticResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepLivraisonResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepPaiementResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepPiecesMoResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepPretALivrerResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepProformaResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepReceptionResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepReparationResponseDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.VehiculeHeaderDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.BaseStepDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepApprovisionnementDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepAssignationDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepBonSortieDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepDiagnosticDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepLivraisonDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepPaiementDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepPiecesMoDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepPretALivrerDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepProformaDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepReceptionDto;
import sn.oas.facturation.features.ordreReparation.dto.steps.StepReparationDto;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdreReparationServiceImpl implements OrdreReparationService {

    private final OrdreReparationRepository ordreReparationRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TechnicienRepository technicienRepository;
    private final ProformaRepository proformaRepository;
    private final PieceDetacheRepository pieceDetacheRepository;
    private final MainDoeuvreRepository mainDoeuvreRepository;
    private final AgentNotificationService agentNotificationService;
    private final EmailService emailService;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;
    private final DiagnosticRepository diagnosticRepository;
    private final PieceJointeDiagnosticRepository pieceJointeDiagnosticRepository;
    private final RemarqueDiagnosticRepository remarqueDiagnosticRepository;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final DevisPrevisionnelRepository devisPrevisionnelRepository;
    private final BonDeCommandeRepository bonDeCommandeRepository;

    @Autowired
    @Lazy
    private ProformaService proformaService;

    @Override
    /*@Caching(evict = {
        @CacheEvict(value = "dashboard_super_agent", allEntries = true),
        @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
        @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public OrdreReparation createOrdreReparation(OrdreReparationRequest request) {
        if (request.getStatut() == StatutOrdreReparation.LIVRE) {
            throw new IllegalArgumentException("La livraison doit passer par la restitution du véhicule.");
        }
        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
        } else {
            throw new RuntimeException("L'ID du véhicule est obligatoire");
        }

        String numero = request.getNumero();
        if (numero == null || numero.trim().isEmpty()) {
            numero = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.OR);
        }

        OrdreReparation ordreReparation = OrdreReparation.builder()
                .numero(numero)
                .descriptionTravaux(request.getDescriptionTravaux())
                .lignesTravaux(request.getLignesTravaux())
                .lignesReception(request.getLignesReception())
                .listeDefauts(request.getListeDefauts())
                .vehicule(vehicule)
                .client(vehicule.getClient())
                .statut(request.getStatut() != null ? request.getStatut() : StatutOrdreReparation.RECEPTION)
                .build();

        if (request.getLignesPieces() != null) {
            for (LigneOrdreReparationPieceRequest ligneReq : request.getLignesPieces()) {
                PieceDetache piece = null;
                Integer prix = ligneReq.prix();
                
                if (Boolean.TRUE.equals(ligneReq.isCustom())) {
                    if (prix == null) prix = 0;
                } else {
                    piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                            .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                    piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                    if (prix == null) prix = (piece.getPrixUnitaire() != null ? piece.getPrixUnitaire().intValue() : 0);
                }

                ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                        .ordreReparation(ordreReparation)
                        .piece(piece)
                        .isCustom(Boolean.TRUE.equals(ligneReq.isCustom()))
                        .designationPds(ligneReq.designationPds())
                        .quantite(ligneReq.quantite())
                        .prix(prix)
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            for (LigneOrdreReparationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ordreReparation.getLignesOrdreReparationMainDoeuvres().add(LigneOrdreReparationMainDoeuvre.builder()
                        .ordreReparation(ordreReparation)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }

        OrdreReparation savedOrdre = ordreReparationRepository.save(ordreReparation);

        agentNotificationService.notifyRole(Role.CHEF_ATELIER,
                "Nouvel Ordre de Réparation",
                "Un nouvel ordre de réparation (" + savedOrdre.getNumero() + ") a été créé.");
        
        if (savedOrdre.getVehicule() != null && savedOrdre.getVehicule().getClient() != null) {
            String clientEmail = savedOrdre.getVehicule().getClient().getEmail();
            if (clientEmail != null && !clientEmail.isEmpty()) {
                emailService.sendHtmlEmail(
                        clientEmail,
                        "Création de votre Ordre de Réparation",
                        "<p>Bonjour,</p><p>Nous vous informons de la création de l'ordre de réparation <b>" + savedOrdre.getNumero() + "</b> pour votre véhicule " + savedOrdre.getVehicule().getImmatriculation() + ".</p><p>Cordialement.</p>"
                );
            }
        }

        return savedOrdre;
    }

    @Override
    public Page<OrdreReparation> getAllOrdresReparation(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("updatedAt").nullsLast(), Sort.Order.desc("id")));
        return ordreReparationRepository.findAll(pageable);
    }

    @Override
    public List<OrdreReparationLightDTO> getAllOrdresReparation() {
        return ordreReparationRepository.findAllWithVehiculeAndClient().stream()
                .map(this::mapToLightDTO)
                .collect(Collectors.toList());
    }

    private OrdreReparationLightDTO mapToLightDTO(OrdreReparation f) {
        ClientLightDTO clientDTO = null;
        var ownerAtWork = f.getClient() != null ? f.getClient() : (f.getVehicule() == null ? null : f.getVehicule().getClient());
        if (ownerAtWork != null) {
            clientDTO = ClientLightDTO.builder()
                    .id(ownerAtWork.getId())
                    .firstName(ownerAtWork.getFirstName())
                    .lastName(ownerAtWork.getLastName())
                    .phone(ownerAtWork.getPhone())
                    .build();
        }

        VehiculeLightDTO vehiculeDTO = null;
        if (f.getVehicule() != null) {
            vehiculeDTO = VehiculeLightDTO.builder()
                    .id(f.getVehicule().getId())
                    .immatriculation(f.getVehicule().getImmatriculation())
                    .marque(f.getVehicule().getMarque())
                    .modele(f.getVehicule().getModele())
                    .client(clientDTO)
                    .build();
        }

        return OrdreReparationLightDTO.builder()
                .id(f.getId())
                .numero(f.getNumero())
                .descriptionTravaux(f.getDescriptionTravaux())
                .dateCreation(f.getDateCreation())
                .dateSortie(f.getDateSortie())
                .statut(f.getStatut())
                .vehicule(vehiculeDTO)
                .hasPiecesOrMo(f.getHasPiecesOrMo())
                .build();
    }

    @Override
    public Optional<OrdreReparation> getOrdreReparationById(Long id) {
        return ordreReparationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdreReparationResponseDTO getOrdreReparationResponseById(Long id) {
        OrdreReparation o = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        return mapToResponseDTO(o);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdreReparationSummaryDto getOrdreReparationSummary(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ordre de Réparation non trouvé"));
                
        OrdreReparationSummaryDto dto = new OrdreReparationSummaryDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        dto.setHasDiagnostic(ordre.getDiagnostic() != null);
        
        boolean hasPieces = ordre.getLignesOrdreReparationPieces() != null && !ordre.getLignesOrdreReparationPieces().isEmpty();
        boolean hasMo = ordre.getLignesOrdreReparationMainDoeuvres() != null && !ordre.getLignesOrdreReparationMainDoeuvres().isEmpty();
        dto.setHasPiecesMo(hasPieces || hasMo);

        if (ordre.getVehicule() != null) {
            VehiculeHeaderDto vehiculeDto = new VehiculeHeaderDto();
            vehiculeDto.setId(ordre.getVehicule().getId());
            vehiculeDto.setImmatriculation(ordre.getVehicule().getImmatriculation());
            vehiculeDto.setMarque(ordre.getVehicule().getMarque());
            vehiculeDto.setModele(ordre.getVehicule().getModele());
            
            if (ordre.getVehicule().getClient() != null) {
                ClientHeaderDto clientDto = new ClientHeaderDto();
                clientDto.setId(ordre.getVehicule().getClient().getId());
                clientDto.setFirstName(ordre.getVehicule().getClient().getFirstName());
                clientDto.setLastName(ordre.getVehicule().getClient().getLastName());
                vehiculeDto.setClient(clientDto);
            }
            dto.setVehicule(vehiculeDto);
        }

        return dto;
    }

    private OrdreReparationResponseDTO mapToResponseDTO(OrdreReparation o) {
        // Véhicule + Client
        OrdreReparationResponseDTO.VehiculeDto vehiculeDto = null;
        if (o.getVehicule() != null) {
            OrdreReparationResponseDTO.ClientDto clientDto = null;
            var ownerAtWork = o.getClient() != null ? o.getClient() : o.getVehicule().getClient();
            if (ownerAtWork != null) {
                clientDto = OrdreReparationResponseDTO.ClientDto.builder()
                        .id(ownerAtWork.getId())
                        .firstName(ownerAtWork.getFirstName())
                        .lastName(ownerAtWork.getLastName())
                        .phone(ownerAtWork.getPhone())
                        .build();
            }
            vehiculeDto = OrdreReparationResponseDTO.VehiculeDto.builder()
                    .id(o.getVehicule().getId())
                    .immatriculation(o.getVehicule().getImmatriculation())
                    .marque(o.getVehicule().getMarque())
                    .modele(o.getVehicule().getModele())
                    .kilometrage(o.getVehicule().getKilometrage() != null ? o.getVehicule().getKilometrage().intValue() : null)
                    .client(clientDto)
                    .build();
        }

        // Diagnostic
        OrdreReparationResponseDTO.DiagnosticDto diagnosticDto = null;
        if (o.getDiagnostic() != null) {
            diagnosticDto = OrdreReparationResponseDTO.DiagnosticDto.builder()
                    .id(o.getDiagnostic().getId())
                    .build();
        }

        // Techniciens (diagnostic)
        List<OrdreReparationResponseDTO.TechnicienDto> techniciens = null;
        if (o.getDiagnostic() != null && o.getDiagnostic().getTechnicien() != null) {
            techniciens = List.of(OrdreReparationResponseDTO.TechnicienDto.builder()
                    .id(o.getDiagnostic().getTechnicien().getId())
                    .firstName(o.getDiagnostic().getTechnicien().getFirstName())
                    .lastName(o.getDiagnostic().getTechnicien().getLastName())
                    .specialite(o.getDiagnostic().getTechnicien().getSpecialite() != null ? o.getDiagnostic().getTechnicien().getSpecialite().name() : null)
                    .build());
        }

        // Techniciens réparation
        List<OrdreReparationResponseDTO.TechnicienDto> techniciensReparation = null;
        if (o.getTechniciensReparation() != null) {
            techniciensReparation = o.getTechniciensReparation().stream()
                    .map(t -> OrdreReparationResponseDTO.TechnicienDto.builder()
                            .id(t.getId())
                            .firstName(t.getFirstName())
                            .lastName(t.getLastName())
                            .specialite(t.getSpecialite() != null ? t.getSpecialite().name() : null)
                            .build())
                    .collect(Collectors.toList());
        }

        // Bon de sortie
        OrdreReparationResponseDTO.BonDeSortieDto bonDeSortieDto = null;
        if (o.getBonDeSortie() != null) {
            bonDeSortieDto = OrdreReparationResponseDTO.BonDeSortieDto.builder()
                    .id(o.getBonDeSortie().getId())
                    .reference(o.getBonDeSortie().getReference())
                    .statut(o.getBonDeSortie().getStatut() != null ? o.getBonDeSortie().getStatut().name() : null)
                    .build();
        }

        // Lignes pièces
        List<OrdreReparationResponseDTO.LigneOrdreReparationPieceDto> lignesPieces = null;
        if (o.getLignesOrdreReparationPieces() != null) {
            lignesPieces = o.getLignesOrdreReparationPieces().stream().map(lp -> {
                OrdreReparationResponseDTO.PieceDto pieceDto = null;
                if (lp.getPiece() != null) {
                    pieceDto = OrdreReparationResponseDTO.PieceDto.builder()
                            .id(lp.getPiece().getId())
                            .reference(lp.getPiece().getReference())
                            .designation(lp.getPiece().getDesignation())
                            .prix(lp.getPiece().getPrixUnitaire() != null ? lp.getPiece().getPrixUnitaire() : null)
                            .type(lp.getPiece().getClass().getSimpleName())
                            .build();
                }
                return OrdreReparationResponseDTO.LigneOrdreReparationPieceDto.builder()
                        .id(lp.getId())
                        .piece(pieceDto)
                        .isCustom(lp.getIsCustom())
                        .designationPds(lp.getDesignationPds())
                        .quantite(lp.getQuantite())
                        .prix(lp.getPrix() != null ? lp.getPrix().doubleValue() : null)
                        .build();
            }).collect(Collectors.toList());
        }

        // Lignes main d'oeuvre
        List<OrdreReparationResponseDTO.LigneOrdreReparationMainDoeuvreDto> lignesMo = null;
        if (o.getLignesOrdreReparationMainDoeuvres() != null) {
            lignesMo = o.getLignesOrdreReparationMainDoeuvres().stream().map(lm -> {
                OrdreReparationResponseDTO.MainDoeuvreDto moDto = null;
                if (lm.getMainDoeuvre() != null) {
                    OrdreReparationResponseDTO.CategorieDto catDto = null;
                    if (lm.getMainDoeuvre().getCategorie() != null) {
                        catDto = OrdreReparationResponseDTO.CategorieDto.builder()
                                .nom(lm.getMainDoeuvre().getCategorie().getNom())
                                .build();
                    }
                    moDto = OrdreReparationResponseDTO.MainDoeuvreDto.builder()
                            .id(lm.getMainDoeuvre().getId())
                            .prix(lm.getMainDoeuvre().getPrix() != null ? lm.getMainDoeuvre().getPrix().doubleValue() : null)
                            .nbreHeure(lm.getMainDoeuvre().getNbreHeure())
                            .description(lm.getMainDoeuvre().getDescription())
                            .categorie(catDto)
                            .build();
                }
                return OrdreReparationResponseDTO.LigneOrdreReparationMainDoeuvreDto.builder()
                        .id(lm.getId())
                        .mainDoeuvre(moDto)
                        .nbreHeure(lm.getNbreHeure())
                        .prix(lm.getPrix() != null ? lm.getPrix().doubleValue() : null)
                        .build();
            }).collect(Collectors.toList());
        }

        return OrdreReparationResponseDTO.builder()
                .id(o.getId())
                .numero(o.getNumero())
                .descriptionTravaux(o.getDescriptionTravaux())
                .lignesTravaux(o.getLignesTravaux())
                .lignesReception(o.getLignesReception())
                .listeDefauts(o.getListeDefauts())
                .dateCreation(o.getDateCreation())
                .updatedAt(o.getUpdatedAt())
                .dateSortie(o.getDateSortie())
                .dateRestitution(o.getDateRestitution())
                .garantieMois(o.getGarantieMois())
                .ficheAtelierId(o.getFicheAtelier() == null ? null : o.getFicheAtelier().getId())
                .statut(o.getStatut())
                .vehicule(vehiculeDto)
                .diagnostic(diagnosticDto)
                .techniciens(techniciens)
                .techniciensReparation(techniciensReparation)
                .bonDeSortie(bonDeSortieDto)
                .lignesOrdreReparationPieces(lignesPieces)
                .lignesOrdreReparationMainDoeuvres(lignesMo)
                .build();
    }

    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public OrdreReparation updateOrdreReparation(Long id, OrdreReparationRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));

        if (request.getNumero() != null)
            ordreReparation.setNumero(request.getNumero());
        if (request.getDescriptionTravaux() != null)
            ordreReparation.setDescriptionTravaux(request.getDescriptionTravaux());
        if (request.getLignesTravaux() != null)
            ordreReparation.setLignesTravaux(request.getLignesTravaux());
        if (request.getLignesReception() != null)
            ordreReparation.setLignesReception(request.getLignesReception());
        if (request.getListeDefauts() != null)
            ordreReparation.setListeDefauts(request.getListeDefauts());
        if (request.getStatut() == StatutOrdreReparation.LIVRE)
            throw new IllegalArgumentException("La livraison doit passer par la restitution du véhicule.");
        if (ordreReparation.getStatut() == StatutOrdreReparation.LIVRE
                && request.getStatut() != null && request.getStatut() != StatutOrdreReparation.LIVRE)
            throw new IllegalArgumentException("Un ordre livré ne peut pas revenir à une étape précédente.");
        if (request.getStatut() != null)
            ordreReparation.setStatut(request.getStatut());

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(request.getVehiculeId())
                    .orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            ordreReparation.setVehicule(vehicule);
        }

        if (request.getLignesPieces() != null) {
            ordreReparation.getLignesOrdreReparationPieces().clear();
            for (LigneOrdreReparationPieceRequest ligneReq : request.getLignesPieces()) {
                PieceDetache piece = null;
                Integer prix = ligneReq.prix();
                
                if (Boolean.TRUE.equals(ligneReq.isCustom())) {
                    // Custom piece, pas de pièce en stock
                    if (prix == null) prix = 0;
                } else {
                    piece = pieceDetacheRepository.findById(ligneReq.pieceId())
                            .orElseThrow(() -> new RuntimeException("Pièce non trouvée"));
                    piece = (PieceDetache) org.hibernate.Hibernate.unproxy(piece);
                    if (prix == null) prix = (piece.getPrixUnitaire() != null ? piece.getPrixUnitaire().intValue() : 0);
                }

                ordreReparation.getLignesOrdreReparationPieces().add(LigneOrdreReparationPiece.builder()
                        .ordreReparation(ordreReparation)
                        .piece(piece)
                        .isCustom(Boolean.TRUE.equals(ligneReq.isCustom()))
                        .designationPds(ligneReq.designationPds())
                        .quantite(ligneReq.quantite())
                        .prix(prix)
                        .build());
            }
        }

        if (request.getLignesMainDoeuvres() != null) {
            ordreReparation.getLignesOrdreReparationMainDoeuvres().clear();
            for (LigneOrdreReparationMainDoeuvreRequest ligneReq : request.getLignesMainDoeuvres()) {
                MainDoeuvre md = mainDoeuvreRepository.findById(ligneReq.mainDoeuvreId())
                        .orElseThrow(() -> new RuntimeException("Main d'œuvre non trouvée"));
                ordreReparation.getLignesOrdreReparationMainDoeuvres().add(LigneOrdreReparationMainDoeuvre.builder()
                        .ordreReparation(ordreReparation)
                        .mainDoeuvre(md)
                        .nbreHeure(ligneReq.nbreHeure())
                        .prix(ligneReq.prix() != null ? ligneReq.prix()
                                : (md.getPrix() != null ? md.getPrix().intValue() : 0))
                        .build());
            }
        }

        ordreReparation = ordreReparationRepository.save(ordreReparation);

        // Auto-create proforma if pieces or MO are added and it doesn't exist yet
        if ((request.getLignesPieces() != null && !request.getLignesPieces().isEmpty()) ||
                (request.getLignesMainDoeuvres() != null && !request.getLignesMainDoeuvres().isEmpty())) {

            if (proformaRepository.findByOrdreReparationId(ordreReparation.getId()).isEmpty()) {
                ProformaCreateRequest pcr = new ProformaCreateRequest();
                pcr.setOrdreReparationId(ordreReparation.getId());
                pcr.setClientId(
                        ordreReparation.getVehicule().getClient() != null ? ordreReparation.getVehicule().getClient().getId()
                                : null);
                pcr.setVehiculeId(ordreReparation.getVehicule().getId());
                pcr.setKilometrage(ordreReparation.getVehicule().getKilometrage() != null
                        ? ordreReparation.getVehicule().getKilometrage()
                        : 0.0);

                if (ordreReparation.getLignesOrdreReparationPieces() != null) {
                    pcr.setLignesPieces(ordreReparation.getLignesOrdreReparationPieces().stream().map(lp -> {
                        LigneFacturationPieceRequest lr = new LigneFacturationPieceRequest();
                        lr.setPieceId(lp.getPiece() != null ? lp.getPiece().getId() : null);
                        lr.setQuantite(lp.getQuantite());
                        lr.setPrix(lp.getPrix());
                        lr.setIsCustom(lp.getIsCustom());
                        lr.setDesignationPds(lp.getDesignationPds());
                        return lr;
                    }).collect(Collectors.toList()));
                }

                if (ordreReparation.getLignesOrdreReparationMainDoeuvres() != null) {
                    pcr.setLignesMainDoeuvres(ordreReparation.getLignesOrdreReparationMainDoeuvres().stream().map(lm -> {
                        LigneFacturationMainDoeuvreRequest lmr = new LigneFacturationMainDoeuvreRequest();
                        lmr.setMainDoeuvreId(lm.getMainDoeuvre() != null ? lm.getMainDoeuvre().getId() : null);
                        lmr.setNbreHeure(lm.getNbreHeure());
                        lmr.setTarifHoraire(lm.getPrix());
                        return lmr;
                    }).collect(Collectors.toList()));
                }

                proformaService.create(pcr);
                // proformaService.create already sets OrdreReparation status to
                // EN_ATTENTE_PROFORMA
            }
        }

        return ordreReparation;
    }

    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void deleteOrdreReparation(Long id) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        ordreReparationRepository.delete(ordreReparation);
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void assignTechnicien(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        Diagnostic diag = fiche.getDiagnostic();
        if (diag == null) {
            diag = Diagnostic.builder()
                    .ordreReparation(fiche)
                    .garage(fiche.getGarage())
                    .technicien(technicien)
                    .statut(StatutDiagnostic.EN_ATTENTE)
                    .build();
            fiche.setDiagnostic(diag);
        } else {
            diag.setTechnicien(technicien);
        }
        fiche.setUpdatedAt(LocalDateTime.now());
        ordreReparationRepository.save(fiche);
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void removeTechnicien(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        if (fiche.getDiagnostic() != null && fiche.getDiagnostic().getTechnicien() != null
                && fiche.getDiagnostic().getTechnicien().getId().equals(technicienId)) {
            fiche.getDiagnostic().setTechnicien(null);
            fiche.setUpdatedAt(LocalDateTime.now());
            ordreReparationRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void assignTechnicienReparation(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        if (!fiche.getTechniciensReparation().contains(technicien)) {
            fiche.getTechniciensReparation().add(technicien);
            fiche.setUpdatedAt(LocalDateTime.now());
            ordreReparationRepository.save(fiche);
        }
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public void removeTechnicienReparation(Long ficheId, Long technicienId) {
        OrdreReparation fiche = ordreReparationRepository.findById(ficheId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        Technicien technicien = technicienRepository.findById(technicienId)
                .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));

        fiche.getTechniciensReparation().remove(technicien);
        fiche.setUpdatedAt(LocalDateTime.now());
        ordreReparationRepository.save(fiche);
    }

    @Transactional
    @Override
    /*@Caching(evict = {
            @CacheEvict(value = "dashboard_super_agent", allEntries = true),
            @CacheEvict(value = "dashboard_chef_atelier", allEntries = true),
            @CacheEvict(value = "dashboard_agent", allEntries = true)
    })*/
    public OrdreReparation updateStatut(Long id, String statut) {
        OrdreReparation fiche = ordreReparationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StatutOrdreReparation newStatut;
        try {
            newStatut = StatutOrdreReparation.valueOf(statut);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Statut invalide : " + statut);
        }

        if (newStatut == StatutOrdreReparation.LIVRE) {
            throw new IllegalArgumentException("La restitution avec signature et garantie doit être validée avant la livraison.");
        }
        if (fiche.getStatut() == StatutOrdreReparation.LIVRE) {
            throw new IllegalArgumentException("Un ordre livré ne peut pas revenir à une étape précédente.");
        }

        // Un technicien doit être affecté au diagnostic avant de pouvoir démarrer le diagnostic.
        /*
        if (newStatut == StatutOrdreReparation.DIAGNOSTIC
                && (fiche.getDiagnostic() == null || fiche.getDiagnostic().getTechnicien() == null)) {
            throw new RuntimeException("Veuillez affecter au moins un technicien au diagnostic avant de démarrer le diagnostic.");
        }
        */

        // Si la réparation commence (REPARATION), on déduit les pièces
        // du proforma du stock de l'atelier
        if (newStatut == StatutOrdreReparation.REPARATION && fiche.getStatut() != StatutOrdreReparation.REPARATION) {
            proformaRepository.findByOrdreReparationId(id).ifPresent(proforma -> {
                for (LigneFacturationPiece lp : proforma
                        .getLignesFacturationPieces()) {
                    if (lp.getPiece() == null) {
                        continue;
                    }
                    PieceDetache piece = pieceDetacheRepository
                            .findById(lp.getPiece().getId()).orElse(null);
                    if (piece != null && piece instanceof PDP pdp) {
                        double prixUnitaire = (pdp.getPrixUnitaire() != null) ? pdp.getPrixUnitaire() : 0.0;
                        double montantPiece = prixUnitaire * lp.getQuantite();
                        Double currentAtelier = pdp.getStockAtelier() != null ? pdp.getStockAtelier() : 0.0;
                        double quantiteUtilisee = lp.getQuantite();
                        pdp.setStockAtelier(Math.max(0.0, currentAtelier - quantiteUtilisee));
                        pdp.setQteReelle(
                                (pdp.getStockMagasin() != null ? pdp.getStockMagasin() : 0) + pdp.getStockAtelier());
                        pieceDetacheRepository.save(pdp);
                    }
                }
            });
        }

        fiche.setStatut(newStatut);
        fiche.setUpdatedAt(LocalDateTime.now());
        OrdreReparation savedFiche = ordreReparationRepository.save(fiche);

        if (newStatut == StatutOrdreReparation.BON_DE_COMMANDE || newStatut == StatutOrdreReparation.BON_DE_SORTIE) {
            agentNotificationService.notifyRole(Role.AGENT_MAGASIN,
                    "Pièces en attente pour " + savedFiche.getNumero(),
                    "La fiche " + savedFiche.getNumero() + " est passée en " + newStatut + ".");
        } else if (newStatut == StatutOrdreReparation.PRET_A_LIVRER) {
            if (savedFiche.getVehicule() != null && savedFiche.getVehicule().getClient() != null) {
                String clientEmail = savedFiche.getVehicule().getClient().getEmail();
                if (clientEmail != null && !clientEmail.isEmpty()) {
                    emailService.sendHtmlEmail(
                            clientEmail,
                            "Votre véhicule est prêt",
                            "<p>Bonjour,</p><p>Toutes les réparations sont terminées. Vous pouvez venir récupérer votre véhicule " + savedFiche.getVehicule().getImmatriculation() + ".</p><p>Cordialement.</p>"
                    );
                }
            }
        }

        return savedFiche;
    }

    @Override
    @Transactional
    public OrdreReparation restituerVehicule(Long id, String signature, Integer garantieMois) {
        OrdreReparation ordre = ordreReparationRepository.findByIdForRestitution(id)
                .orElseThrow(() -> new sn.oas.facturation.shared.exception.ResourceNotFoundException("Ordre de réparation introuvable"));
        if (ordre.getStatut() != StatutOrdreReparation.PRET_A_LIVRER) {
            throw new IllegalArgumentException("Le véhicule doit être prêt à livrer avant sa restitution.");
        }
        FicheAtelier fiche = ordre.getFicheAtelier();
        if (ordre.getDateRestitution() != null) {
            throw new IllegalArgumentException("La restitution a déjà été enregistrée.");
        }
        if (signature == null || signature.length() > 1_500_000
                || !signature.matches("^data:image/png;base64,[A-Za-z0-9+/=]+$")) {
            throw new IllegalArgumentException("Une signature PNG valide est obligatoire.");
        }
        int duree = garantieMois == null ? 1 : garantieMois;
        if (duree < 1 || duree > 120) {
            throw new IllegalArgumentException("La garantie doit durer entre 1 et 120 mois.");
        }
        LocalDateTime maintenant = LocalDateTime.now();
        if (fiche != null) {
            fiche.setSignatureSortieBase64(signature);
            fiche.setGarantieMois(duree);
            fiche.setGarantie(duree + " mois après restitution");
            ficheAtelierRepository.save(fiche);
        }
        ordre.setSignatureRestitutionBase64(signature);
        ordre.setDateRestitution(maintenant);
        ordre.setGarantieMois(duree);
        ordre.setDateSortie(maintenant);
        ordre.setStatut(StatutOrdreReparation.LIVRE);
        ordre.setUpdatedAt(maintenant);
        return ordreReparationRepository.save(ordre);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByVehiculeIdAndStatutNotIn(Long vehiculeId, List<StatutOrdreReparation> statuts) {
        return ordreReparationRepository.existsByVehiculeIdAndStatutNotIn(vehiculeId, statuts);
    }

    // ─── Pièces jointes de diagnostic ──────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<PieceJointeDiagnosticResponse> getPiecesJointesDiagnostic(Long ordreReparationId, TypePieceJointe type) {
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        List<PieceJointeDiagnostic> pieces = type != null
                ? pieceJointeDiagnosticRepository.findByDiagnosticOrdreReparationIdAndTypeOrderByCreatedAtDesc(ordreReparationId, type)
                : pieceJointeDiagnosticRepository.findByDiagnosticOrdreReparationIdOrderByCreatedAtDesc(ordreReparationId);
        return pieces.stream().map(this::toPieceJointeResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PieceJointeDiagnosticResponse addPieceJointeDiagnostic(Long ordreReparationId, PieceJointeDiagnosticRequest request) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            throw new RuntimeException("L'URL de la pièce jointe est obligatoire");
        }
        if (request.getType() == null) {
            throw new RuntimeException("Le type de la pièce jointe est obligatoire");
        }

        Diagnostic diag = diagnosticRepository.findByOrdreReparationId(ordreReparationId).orElse(null);
        if (diag == null) {
            diag = ordreReparation.getDiagnostic();
        }
        if (diag == null) {
            diag = Diagnostic.builder()
                    .ordreReparation(ordreReparation)
                    .garage(ordreReparation.getGarage())
                    .statut(StatutDiagnostic.EN_COURS)
                    .build();
            diag = diagnosticRepository.save(diag);
            ordreReparation.setDiagnostic(diag);
            ordreReparationRepository.save(ordreReparation);
        }

        OrdreReparation orRef = diag.getOrdreReparation() != null ? diag.getOrdreReparation() : ordreReparation;
        PieceJointeDiagnostic pieceJointe = PieceJointeDiagnostic.builder()
                .diagnostic(diag)
                .ordreReparation(orRef)
                .url(request.getUrl())
                .type(request.getType())
                .remarque(request.getRemarque())
                .build();

        return toPieceJointeResponse(pieceJointeDiagnosticRepository.save(pieceJointe));
    }

    @Override
    @Transactional
    public void deletePieceJointeDiagnostic(Long ordreReparationId, Long pieceJointeId) {
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        PieceJointeDiagnostic pieceJointe = pieceJointeDiagnosticRepository.findById(pieceJointeId)
                .orElseThrow(() -> new RuntimeException("Pièce jointe non trouvée"));
        if (pieceJointe.getDiagnostic() == null || pieceJointe.getDiagnostic().getOrdreReparation() == null
                || !pieceJointe.getDiagnostic().getOrdreReparation().getId().equals(ordreReparationId)) {
            throw new RuntimeException("Cette pièce jointe n'appartient pas à cet ordre de réparation");
        }
        pieceJointeDiagnosticRepository.delete(pieceJointe);
    }

    private PieceJointeDiagnosticResponse toPieceJointeResponse(PieceJointeDiagnostic p) {
        String techNom = null;
        if (p.getTechnicien() != null) {
            String prenom = p.getTechnicien().getFirstName() != null ? p.getTechnicien().getFirstName() : "";
            String nom = p.getTechnicien().getLastName() != null ? p.getTechnicien().getLastName() : "";
            techNom = (prenom + " " + nom).trim();
            if (techNom.isEmpty()) techNom = p.getTechnicien().getUsername();
        }
        Long orId = (p.getDiagnostic() != null && p.getDiagnostic().getOrdreReparation() != null)
                ? p.getDiagnostic().getOrdreReparation().getId()
                : (p.getOrdreReparation() != null ? p.getOrdreReparation().getId() : null);
        return PieceJointeDiagnosticResponse.builder()
                .id(p.getId())
                .ordreReparationId(orId)
                .url(p.getUrl())
                .type(p.getType())
                .remarque(p.getRemarque())
                .technicienNom(techNom)
                .createdAt(p.getCreatedAt())
                .build();
    }

    // ─── Remarques de diagnostic ────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<RemarqueDiagnosticResponse> getRemarquesDiagnostic(Long ordreReparationId) {
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        return remarqueDiagnosticRepository.findByOrdreReparationIdOrderByCreatedAtDesc(ordreReparationId)
                .stream().map(this::toRemarqueResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RemarqueDiagnosticResponse addRemarqueDiagnostic(Long ordreReparationId, Technicien technicien, String contenu) {
        OrdreReparation ordreReparation = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        if (contenu == null || contenu.trim().isEmpty()) {
            throw new RuntimeException("Le contenu de la remarque ne peut pas être vide");
        }

        Diagnostic diag = diagnosticRepository.findByOrdreReparationId(ordreReparationId).orElse(null);
        if (diag == null) {
            diag = ordreReparation.getDiagnostic();
        }
        if (diag == null) {
            diag = Diagnostic.builder()
                    .ordreReparation(ordreReparation)
                    .garage(ordreReparation.getGarage())
                    .technicien(technicien)
                    .statut(StatutDiagnostic.EN_COURS)
                    .build();
            diag = diagnosticRepository.save(diag);
            ordreReparation.setDiagnostic(diag);
            ordreReparationRepository.save(ordreReparation);
        }

        OrdreReparation orRef = diag.getOrdreReparation() != null ? diag.getOrdreReparation() : ordreReparation;
        RemarqueDiagnostic remarque = RemarqueDiagnostic.builder()
                .diagnostic(diag)
                .ordreReparation(orRef)
                .technicien(technicien)
                .contenu(contenu.trim())
                .build();
        return toRemarqueResponse(remarqueDiagnosticRepository.save(remarque));
    }

    @Override
    @Transactional
    public void deleteRemarqueDiagnostic(Long ordreReparationId, Long remarqueId) {
        ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        RemarqueDiagnostic r = remarqueDiagnosticRepository.findById(remarqueId)
                .orElseThrow(() -> new RuntimeException("Remarque non trouvée"));
        boolean matchOr = (r.getOrdreReparation() != null && r.getOrdreReparation().getId().equals(ordreReparationId))
                || (r.getDiagnostic() != null && r.getDiagnostic().getOrdreReparation() != null
                && r.getDiagnostic().getOrdreReparation().getId().equals(ordreReparationId));
        if (!matchOr) {
            throw new RuntimeException("Cette remarque n'appartient pas à cet ordre de réparation");
        }
        remarqueDiagnosticRepository.delete(r);
    }

    private RemarqueDiagnosticResponse toRemarqueResponse(RemarqueDiagnostic r) {
        String techNom = null;
        if (r.getTechnicien() != null) {
            String prenom = r.getTechnicien().getFirstName() != null ? r.getTechnicien().getFirstName() : "";
            String nom = r.getTechnicien().getLastName() != null ? r.getTechnicien().getLastName() : "";
            techNom = (prenom + " " + nom).trim();
            if (techNom.isEmpty()) techNom = r.getTechnicien().getUsername();
        }
        Long orId = (r.getDiagnostic() != null && r.getDiagnostic().getOrdreReparation() != null)
                ? r.getDiagnostic().getOrdreReparation().getId()
                : null;
        return RemarqueDiagnosticResponse.builder()
                .id(r.getId())
                .ordreReparationId(orId)
                .technicienNom(techNom)
                .contenu(r.getContenu())
                .createdAt(r.getCreatedAt())
                .build();
    }

    // ─── Lien Fiche Atelier → Ordre de réparation ──────────────────────

    @Override
    @Transactional
    public OrdreReparation createFromFicheAtelier(Long ficheAtelierId) {
        FicheAtelier ficheAtelier = ficheAtelierRepository.findById(ficheAtelierId)
                .orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));

        Optional<OrdreReparation> existingExact = ordreReparationRepository.findFirstByFicheAtelierId(ficheAtelierId);
        if (existingExact.isPresent()) {
            OrdreReparation existing = existingExact.get();
            // Les deux entités portent actuellement une référence OneToOne. Maintenir
            // également le côté FicheAtelier, utilisé par les DTOs pour exposer le statut.
            if (ficheAtelier.getOrdreReparation() == null) {
                ficheAtelier.setOrdreReparation(existing);
                ficheAtelierRepository.save(ficheAtelier);
            }
            return existing;
        }
        
        // Le devis prévisionnel sur la fiche atelier n'est plus obligatoire
        List<DevisPrevisionnel> devisList = devisPrevisionnelRepository.findByFicheAtelierIdOrderByDateCreationDesc(ficheAtelierId);

        if (ficheAtelier.getVehicule() == null) {
            throw new RuntimeException("La fiche atelier n'a pas de véhicule associé");
        }
        
        Optional<OrdreReparation> activeOr = ordreReparationRepository.findFirstByVehiculeIdAndStatutNotIn(
                ficheAtelier.getVehicule().getId(), 
                List.of(StatutOrdreReparation.LIVRE, StatutOrdreReparation.PRET_A_LIVRER)
        );
        if (activeOr.isPresent()) {
            OrdreReparation existing = activeOr.get();
            if (existing.getFicheAtelier() != null
                    && !existing.getFicheAtelier().getId().equals(ficheAtelierId)) {
                throw new IllegalStateException(
                        "Un ordre de réparation actif existe déjà pour ce véhicule (" + existing.getNumero()
                                + ") et est lié à une autre fiche atelier.");
            }
            // Réutiliser l'ordre actif non lié (créé par un ancien flux) et réparer
            // les deux références afin que la fiche reflète correctement l'association.
            existing.setFicheAtelier(ficheAtelier);
            ficheAtelier.setOrdreReparation(existing);
            ordreReparationRepository.save(existing);
            ficheAtelierRepository.save(ficheAtelier);
            return existing;
        }

        String numero = documentNumberGeneratorService.generateNextNumber(DocumentType.OR);
        String travauxDemandes = ficheAtelier.getDesignationTravaux();

        OrdreReparation ordreReparation = OrdreReparation.builder()
                .numero(numero)
                .descriptionTravaux(travauxDemandes != null ? travauxDemandes : "")
                .lignesTravaux(syntheseTravaux(travauxDemandes))
                .lignesReception(syntheseReception(ficheAtelier))
                .vehicule(ficheAtelier.getVehicule())
                .client(ficheAtelier.getVehicule().getClient())
                .ficheAtelier(ficheAtelier)
                .statut(StatutOrdreReparation.RECEPTION)
                .build();

        OrdreReparation savedOrdre = ordreReparationRepository.save(ordreReparation);
        ficheAtelier.setOrdreReparation(savedOrdre);
        ficheAtelierRepository.save(ficheAtelier);
        
        ficheAtelier.setOrdreReparation(savedOrdre);
        ficheAtelierRepository.save(ficheAtelier);
        
        if (savedOrdre.getVehicule() != null && savedOrdre.getVehicule().getClient() != null) {
            String clientEmail = savedOrdre.getVehicule().getClient().getEmail();
            if (clientEmail != null && !clientEmail.isEmpty()) {
                emailService.sendHtmlEmail(
                        clientEmail,
                        "Création de votre Ordre de Réparation",
                        "<p>Bonjour,</p><p>Nous vous informons de la création de l'ordre de réparation <b>" + savedOrdre.getNumero() + "</b> pour votre véhicule " + savedOrdre.getVehicule().getImmatriculation() + ".</p><p>Cordialement.</p>"
                );
            }
        }

        // Lier les éventuels devis existants de la fiche atelier à ce nouvel ordre de réparation
        if (devisList != null) {
            for (DevisPrevisionnel d : devisList) {
                if (d.getOrdreReparation() == null) {
                    d.setOrdreReparation(savedOrdre);
                    devisPrevisionnelRepository.save(d);
                }
            }
        }

        agentNotificationService.notifyRole(Role.CHEF_ATELIER,
                "Nouvel Ordre de Réparation",
                "L'ordre de réparation (" + savedOrdre.getNumero() + ") a été généré depuis une fiche atelier.");

        return savedOrdre;
    }

    /**
     * Reprend la désignation des travaux de la fiche atelier d'origine comme unique ligne
     * verrouillée de la section "Travaux demandés" (le chef d'atelier peut ensuite en ajouter
     * d'autres, non verrouillées, mais pas modifier/supprimer celle-ci).
     */
    private List<LigneTravailOrdre> syntheseTravaux(String designationTravaux) {
        ArrayList<LigneTravailOrdre> lignes = new ArrayList<>();
        if (designationTravaux != null && !designationTravaux.isBlank()) {
            lignes.add(LigneTravailOrdre.builder().nom(designationTravaux).verrouille(true).build());
        }
        return lignes;
    }

    /**
     * Reprend les lignes de réception de la fiche atelier d'origine, verrouillées (le chef
     * d'atelier ne peut pas modifier leur désignation ni les supprimer côté UI — il peut
     * seulement ajouter de nouvelles lignes, non verrouillées, via le formulaire de l'ordre
     * de réparation).
     */
    private List<LigneReceptionOrdre> syntheseReception(FicheAtelier ficheAtelier) {
        List<LigneReception> lignesReception = ficheAtelier.getLignesReception();
        if (lignesReception == null || lignesReception.isEmpty()) {
            return new java.util.ArrayList<>();
        }
        return lignesReception.stream()
                .map(l -> LigneReceptionOrdre.builder()
                        .nom(l.getNom())
                        .etat(l.getEtat())
                        .verrouille(true)
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByFicheAtelierId(Long ficheAtelierId) {
        if (ordreReparationRepository.existsByFicheAtelierId(ficheAtelierId)) {
            return true;
        }
        FicheAtelier fiche = ficheAtelierRepository.findById(ficheAtelierId).orElse(null);
        if (fiche != null && fiche.getVehicule() != null) {
            return ordreReparationRepository.existsByVehiculeIdAndStatutNotIn(
                fiche.getVehicule().getId(), 
                List.of(StatutOrdreReparation.LIVRE, StatutOrdreReparation.PRET_A_LIVRER)
            );
        }
        return false;
    }

    private void updateBaseStepFields(OrdreReparation ordre, BaseStepDto dto) {
        if (dto.getStatut() == StatutOrdreReparation.LIVRE) {
            throw new IllegalArgumentException("La livraison doit passer par la restitution du véhicule.");
        }
        if (ordre.getStatut() == StatutOrdreReparation.LIVRE && dto.getStatut() != null) {
            throw new IllegalArgumentException("Un ordre livré ne peut pas revenir à une étape précédente.");
        }
        if (dto.getNumero() != null) ordre.setNumero(dto.getNumero());
        if (dto.getDescriptionTravaux() != null) ordre.setDescriptionTravaux(dto.getDescriptionTravaux());
        if (dto.getStatut() != null) ordre.setStatut(dto.getStatut());
        if (dto.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeRepository.findById(dto.getVehiculeId()).orElseThrow(() -> new RuntimeException("Véhicule non trouvé"));
            ordre.setVehicule(vehicule);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public StepReceptionResponseDto getStepReception(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        StepReceptionResponseDto dto = new StepReceptionResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        dto.setDescriptionTravaux(ordre.getDescriptionTravaux());
        dto.setLignesTravaux(ordre.getLignesTravaux());
        dto.setLignesReception(ordre.getLignesReception());
        dto.setListeDefauts(ordre.getListeDefauts());

        if (ordre.getVehicule() != null) {
            dto.setVehiculeId(ordre.getVehicule().getId());
            VehiculeHeaderDto vDto = new VehiculeHeaderDto();
            vDto.setId(ordre.getVehicule().getId());
            vDto.setImmatriculation(ordre.getVehicule().getImmatriculation());
            vDto.setMarque(ordre.getVehicule().getMarque());
            vDto.setModele(ordre.getVehicule().getModele());
            vDto.setKilometrage(ordre.getVehicule().getKilometrage() != null ? ordre.getVehicule().getKilometrage().intValue() : null);
            if (ordre.getVehicule().getClient() != null) {
                ClientHeaderDto cDto = new ClientHeaderDto();
                cDto.setId(ordre.getVehicule().getClient().getId());
                cDto.setFirstName(ordre.getVehicule().getClient().getFirstName());
                cDto.setLastName(ordre.getVehicule().getClient().getLastName());
                cDto.setPhone(ordre.getVehicule().getClient().getPhone());
                vDto.setClient(cDto);
            }
            dto.setVehicule(vDto);
        }

        if (ordre.getFicheAtelier() != null) {
            dto.setFicheAtelierId(ordre.getFicheAtelier().getId());
            dto.setFicheAtelier(FicheAtelierDetailsResponse.from(ordre.getFicheAtelier()));
            dto.setLignesDefauts(ordre.getFicheAtelier().getLignesDefauts());
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepDiagnosticResponseDto getStepDiagnostic(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepDiagnosticResponseDto dto = new StepDiagnosticResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        dto.setListeDefauts(ordre.getListeDefauts());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepPiecesMoResponseDto getStepPiecesMo(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepPiecesMoResponseDto dto = new StepPiecesMoResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        
        dto.setLignesOrdreReparationPieces(ordre.getLignesOrdreReparationPieces().stream().map(ligne -> {
            LignePieceOrdreDto ligneDto = new LignePieceOrdreDto();
            ligneDto.setId(ligne.getId());
            ligneDto.setQuantite(ligne.getQuantite());
            ligneDto.setPrix(ligne.getPrix());
            ligneDto.setIsCustom(ligne.getIsCustom());
            ligneDto.setDesignationPds(ligne.getDesignationPds());
            if (ligne.getPiece() != null) {
                ligneDto.setPieceId(ligne.getPiece().getId());
                PieceSummaryDto pieceDto = new PieceSummaryDto();
                pieceDto.setId(ligne.getPiece().getId());
                pieceDto.setReference(ligne.getPiece().getReference());
                pieceDto.setDesignation(ligne.getPiece().getDesignation());
                pieceDto.setType(ligne.getPiece().getType() != null ? ligne.getPiece().getType().name() : null);
                pieceDto.setStockMagasin(ligne.getPiece().getStockMagasin());
                ligneDto.setPiece(pieceDto);
            }
            return ligneDto;
        }).collect(Collectors.toList()));

        dto.setLignesOrdreReparationMainDoeuvres(ordre.getLignesOrdreReparationMainDoeuvres().stream().map(ligne -> {
            LigneMoOrdreDto ligneDto = new LigneMoOrdreDto();
            ligneDto.setId(ligne.getId());
            ligneDto.setHeures(ligne.getNbreHeure());
            ligneDto.setPrix(ligne.getPrix());
            if (ligne.getMainDoeuvre() != null) {
                ligneDto.setDescription(ligne.getMainDoeuvre().getDescription());
            }
            return ligneDto;
        }).collect(Collectors.toList()));

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepProformaResponseDto getStepProforma(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepProformaResponseDto dto = new StepProformaResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        dto.setUpdatedAt(ordre.getUpdatedAt());
        dto.setDateCreation(ordre.getDateCreation());
        if (ordre.getVehicule() != null) {
            dto.setVehiculeId(ordre.getVehicule().getId());
            VehiculeHeaderDto vDto = new VehiculeHeaderDto();
            vDto.setId(ordre.getVehicule().getId());
            vDto.setImmatriculation(ordre.getVehicule().getImmatriculation());
            vDto.setMarque(ordre.getVehicule().getMarque());
            vDto.setModele(ordre.getVehicule().getModele());
            vDto.setKilometrage(ordre.getVehicule().getKilometrage() != null ? ordre.getVehicule().getKilometrage().intValue() : 0);
            if (ordre.getVehicule().getClient() != null) {
                ClientHeaderDto cDto = new ClientHeaderDto();
                cDto.setId(ordre.getVehicule().getClient().getId());
                cDto.setFirstName(ordre.getVehicule().getClient().getFirstName());
                cDto.setLastName(ordre.getVehicule().getClient().getLastName());
                cDto.setPhone(ordre.getVehicule().getClient().getPhone());
                vDto.setClient(cDto);
            }
            dto.setVehicule(vDto);
        }
        if (ordre.getDiagnostic() != null) {
            StepProformaResponseDto.DiagnosticDto dDto = new StepProformaResponseDto.DiagnosticDto();
            dDto.setKilometrage(ordre.getDiagnostic().getKilometrage());
            dto.setDiagnostic(dDto);
        }
        if (ordre.getProforma() != null){
            ProformaSummaryDto proformaDto = new ProformaSummaryDto();
            proformaDto.setId(ordre.getProforma().getId());
            proformaDto.setNumero(ordre.getProforma().getNumero());
            proformaDto.setVisibleClient(Boolean.TRUE.equals(ordre.getProforma().getVisibleClient()));
            proformaDto.setDateCreation(ordre.getProforma().getDateCreation());
            proformaDto.setStatut(ordre.getProforma().getStatut() != null ? ordre.getProforma().getStatut().name() : null);
            proformaDto.setMontantHT(ordre.getProforma().getMontantHT() != null ? ordre.getProforma().getMontantHT().doubleValue() : 0.0);
            proformaDto.setMontantTVA(ordre.getProforma().getMontantTVA() != null ? ordre.getProforma().getMontantTVA().doubleValue() : 0.0);
            proformaDto.setMontantTTC(ordre.getProforma().getMontantTTC() != null ? ordre.getProforma().getMontantTTC().doubleValue() : 0.0);
            
            proformaDto.setLignesPieces(ordre.getProforma().getLignesFacturationPieces().stream().map(ligne -> {
                LigneProformaPieceDto ligneDto = new LigneProformaPieceDto();
                ligneDto.setId(ligne.getId());
                ligneDto.setNom(ligne.getIsCustom() != null && ligne.getIsCustom() ? ligne.getDesignationPds() : (ligne.getPiece() != null ? ligne.getPiece().getDesignation() : "Pièce"));
                ligneDto.setQuantite(ligne.getQuantite());
                ligneDto.setPrix(ligne.getPrix() != null ? ligne.getPrix().doubleValue() : 0.0);
                ligneDto.setMontantTotal(ligne.getPrix() != null && ligne.getQuantite() != null ? ligne.getPrix().doubleValue() * ligne.getQuantite() : 0.0);
                ligneDto.setIsCustom(ligne.getIsCustom());
                ligneDto.setType(ligne.getPiece() != null && ligne.getPiece().getType() != null ? ligne.getPiece().getType().name() : null);
                return ligneDto;
            }).collect(Collectors.toList()));
            
            proformaDto.setLignesMainDoeuvres(ordre.getProforma().getLignesFacturationMainDoeuvres().stream().map(ligne -> {
                LigneProformaMoDto ligneDto = new LigneProformaMoDto();
                ligneDto.setId(ligne.getId());
                ligneDto.setNom(ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getDescription() : "Main d'œuvre");
                ligneDto.setNbreHeure(ligne.getNbreHeure());
                ligneDto.setTarifHoraire(ligne.getTarifHoraire() != null ? ligne.getTarifHoraire().doubleValue() : 0.0);
                ligneDto.setMontantTotal(ligne.getTarifHoraire() != null && ligne.getNbreHeure() != null ? ligne.getTarifHoraire().doubleValue() * ligne.getNbreHeure() : 0.0);
                return ligneDto;
            }).collect(Collectors.toList()));
            
            dto.setProforma(proformaDto);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepApprovisionnementResponseDto getStepApprovisionnement(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepApprovisionnementResponseDto dto = new StepApprovisionnementResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());

        Proforma proforma = ordre.getProforma();
        if (proforma == null) {
            proforma = proformaRepository.findByOrdreReparationId(id).orElse(null);
        }

        // Récupération des bons de commande liés
        Long vehiculeId = ordre.getVehicule() != null ? ordre.getVehicule().getId() : null;
        // 1. Récupérer uniquement les bons de commande liés directement à cet Ordre de Réparation
        List<BonDeCommande> bdcList = bonDeCommandeRepository.findByOrdreReparationId(id);

        // 2. Ajouter le BDC du proforma s'il existe et n'est pas déjà dans la liste
        if (proforma != null && proforma.getBonDeCommande() != null) {
            BonDeCommande proformaBdc = proforma.getBonDeCommande();
            if (bdcList.stream().noneMatch(b -> b.getId().equals(proformaBdc.getId()))) {
        bdcList.add(proformaBdc);
    }
}
        List<StepApprovisionnementResponseDto.BonCommandeSummaryDto> bdcDtos = new ArrayList<>();
        if (bdcList != null) {
            for (BonDeCommande bc : bdcList) {
                String fournisseurNom = null;
                if (bc.getFournisseur() != null) {
                    Fournisseur f = bc.getFournisseur();
                    if (f.getNomEntreprise() != null && !f.getNomEntreprise().isBlank()) {
                        fournisseurNom = f.getNomEntreprise();
                    } else {
                        String prenom = f.getPrenom() != null ? f.getPrenom() : "";
                        String nom = f.getNom() != null ? f.getNom() : "";
                        fournisseurNom = (prenom + " " + nom).trim();
                    }
                }
                double montant = bc.getMontantTTC() != null ? bc.getMontantTTC().doubleValue()
                        : (bc.getMontantHT() != null ? bc.getMontantHT().doubleValue() : 0.0);

                List<StepApprovisionnementResponseDto.PieceBonCommandeDto> piecesBdc = new ArrayList<>();
                int totalQuantitePieces = 0;
                if (bc.getLignes() != null) {
                    for (LigneBonDeCommandePiece l : bc.getLignes()) {
                        int qte = l.getQuantite() != null ? l.getQuantite() : 0;
                        totalQuantitePieces += qte;
                        String ref = l.getPieceDetachee() != null ? l.getPieceDetachee().getReference() : l.getReferencePds();
                        String des = l.getPieceDetachee() != null ? l.getPieceDetachee().getDesignation() : l.getDesignationPds();
                        Long pId = l.getPieceDetachee() != null ? l.getPieceDetachee().getId() : null;
                        double pu = l.getPrixUnitaire() != null ? l.getPrixUnitaire().doubleValue() : 0.0;
                        double mt = l.getMontant() != null ? l.getMontant().doubleValue() : (qte * pu);

                        piecesBdc.add(StepApprovisionnementResponseDto.PieceBonCommandeDto.builder()
                                .ligneId(l.getId())
                                .pieceId(pId)
                                .reference(ref)
                                .designation(des != null ? des : "Pièce")
                                .quantite(qte)
                                .quantiteRecue(l.getQuantiteRecue() != null ? l.getQuantiteRecue() : 0)
                                .prixUnitaire(pu)
                                .montantTotal(mt)
                                .build());
                    }
                }

                bdcDtos.add(StepApprovisionnementResponseDto.BonCommandeSummaryDto.builder()
                        .id(bc.getId())
                        .numero(bc.getNumero())
                        .reference(bc.getNumero())
                        .fournisseurNom(fournisseurNom)
                        .montantTotal(montant)
                        .statut(bc.getStatut() != null ? bc.getStatut().name() : null)
                        .dateCommande(bc.getDateCommande())
                        .nombrePieces(totalQuantitePieces)
                        .pieces(piecesBdc)
                        .build());
            }
        }

        dto.setBonsDeCommande(bdcDtos);
        dto.setHasBonDeCommande(!bdcDtos.isEmpty());

        if (proforma != null) {
            dto.setProformaId(proforma.getId());
            dto.setProformaNumero(proforma.getNumero());

            List<StepApprovisionnementResponseDto.PieceApprovisionnementDto> piecesDto = new ArrayList<>();
            if (proforma.getLignesFacturationPieces() != null) {
                for (LigneFacturationPiece lp : proforma.getLignesFacturationPieces()) {
                    boolean isCustom = Boolean.TRUE.equals(lp.getIsCustom()) || lp.getPiece() == null;
                    if (isCustom || lp.getPiece() == null) {
                        // On ignore les pièces custom (PDS) ou hors catalogue
                        continue;
                    }

                    PieceDetache piece = lp.getPiece();
                    String type = piece.getType() != null ? piece.getType().name() : (piece instanceof PDP ? "PDP" : "");
                    if (!"PDP".equalsIgnoreCase(type) && !(piece instanceof PDP)) {
                        // On ne traite que les PDP (Pièces Détachées Principales / Magasin)
                        continue;
                    }

                    int qteDemandee = lp.getQuantite() != null ? lp.getQuantite() : 0;
                    double stockMagasin = piece.getStockMagasin() != null ? piece.getStockMagasin() : 0.0;
                    double stockAtelier = piece.getStockAtelier() != null ? piece.getStockAtelier() : 0.0;

                    // Uniquement ce qui est manquant en stock magasin
                    if (stockMagasin >= qteDemandee) {
                        continue;
                    }

                    double manque = qteDemandee - stockMagasin;
                    double diffStock = stockMagasin - qteDemandee;
                    double prix = lp.getPrix() != null ? lp.getPrix().doubleValue() : 0.0;
                    if (prix == 0.0 && piece.getPrixUnitaire() != null) {
                        prix = piece.getPrixUnitaire();
                    }

                    // Calcul de la quantité commandée et reçue à partir des bons de commande réels
                    int totalQteCommandee = 0;
                    int totalQteRecue = 0;
                    if (bdcList != null) {
                        for (BonDeCommande bc : bdcList) {
                            if (bc.getStatut() == StatutBonCommande.ANNULE) {
                                continue;
                            }
                            if (bc.getLignes() != null) {
                                for (LigneBonDeCommandePiece l : bc.getLignes()) {
                                    boolean match = false;
                                    if (l.getPieceDetachee() != null && piece.getId() != null && piece.getId().equals(l.getPieceDetachee().getId())) {
                                        match = true;
                                    } else if (piece.getReference() != null && !piece.getReference().isBlank()) {
                                        String bdcRef = l.getPieceDetachee() != null ? l.getPieceDetachee().getReference() : l.getReferencePds();
                                        if (piece.getReference().equalsIgnoreCase(bdcRef)) {
                                            match = true;
                                        }
                                    }
                                    if (match) {
                                        totalQteCommandee += (l.getQuantite() != null ? l.getQuantite() : 0);
                                        totalQteRecue += (l.getQuantiteRecue() != null ? l.getQuantiteRecue() : 0);
                                    }
                                }
                            }
                        }
                    }

                    piecesDto.add(StepApprovisionnementResponseDto.PieceApprovisionnementDto.builder()
                            .ligneId(lp.getId())
                            .pieceId(piece.getId())
                            .reference(piece.getReference())
                            .designation(piece.getDesignation() != null ? piece.getDesignation() : "Pièce")
                            .type("PDP")
                            .isCustom(false)
                            .quantiteDemandee(qteDemandee)
                            .stockMagasin(stockMagasin)
                            .stockAtelier(stockAtelier)
                            .quantiteCommande(totalQteCommandee)
                            .quantiteRecue(totalQteRecue)
                            .quantiteManquante(manque)
                            .differenceStock(diffStock)
                            .isManquant(true)
                            .prixUnitaire(prix)
                            .montantTotal(qteDemandee * prix)
                            .build());
                }
            }
            dto.setPiecesManquantesProforma(piecesDto);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepBonSortieResponseDto getStepBonSortie(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepBonSortieResponseDto dto = new StepBonSortieResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepAssignationResponseDto getStepAssignation(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepAssignationResponseDto dto = new StepAssignationResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepReparationResponseDto getStepReparation(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepReparationResponseDto dto = new StepReparationResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepPaiementResponseDto getStepPaiement(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepPaiementResponseDto dto = new StepPaiementResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepPretALivrerResponseDto getStepPretALivrer(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepPretALivrerResponseDto dto = new StepPretALivrerResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepLivraisonResponseDto getStepLivraison(Long id) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        StepLivraisonResponseDto dto = new StepLivraisonResponseDto();
        dto.setId(ordre.getId());
        dto.setNumero(ordre.getNumero());
        dto.setStatut(ordre.getStatut() != null ? ordre.getStatut().name() : null);
        if (ordre.getVehicule() != null) dto.setVehiculeId(ordre.getVehicule().getId());
        return dto;
    }

    @Override
    @Transactional
    public StepReceptionDto updateStepReception(Long id, StepReceptionDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé"));
        updateBaseStepFields(ordre, dto);
        if (dto.getListeDefauts() != null) ordre.setListeDefauts(dto.getListeDefauts());
        if (dto.getLignesTravaux() != null) ordre.setLignesTravaux(dto.getLignesTravaux());
        if (dto.getLignesReception() != null) ordre.setLignesReception(dto.getLignesReception());
        if (dto.getLignesDefauts() != null && ordre.getFicheAtelier() != null) {
            ordre.getFicheAtelier().setLignesDefauts(dto.getLignesDefauts());
        }
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepDiagnosticDto updateStepDiagnostic(Long id, StepDiagnosticDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        if (dto.getTechnicienId() != null) {
            Technicien technicien = technicienRepository.findById(dto.getTechnicienId()).orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
            Diagnostic diag = ordre.getDiagnostic();
            if (diag == null) {
                diag = Diagnostic.builder().ordreReparation(ordre).technicien(technicien).build();
                ordre.setDiagnostic(diag);
            } else {
                diag.setTechnicien(technicien);
            }
        }
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepPiecesMoDto updateStepPiecesMo(Long id, StepPiecesMoDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);

        OrdreReparationRequest req = new OrdreReparationRequest();
        if (dto.getLignesPieces() != null) {
            req.setLignesPieces(dto.getLignesPieces().stream().map(l -> 
                new LigneOrdreReparationPieceRequest(l.getPieceId(), l.getQuantite(), (l.getPrix() != null ? l.getPrix().intValue() : 0), l.getIsCustom(), l.getDesignationPds())
            ).collect(Collectors.toList()));
        }
        if (dto.getLignesMainDoeuvres() != null) {
            req.setLignesMainDoeuvres(dto.getLignesMainDoeuvres().stream().map(l -> 
                new LigneOrdreReparationMainDoeuvreRequest(l.getMainDoeuvreId(), l.getNbreHeure(), (l.getPrix() != null ? l.getPrix().intValue() : 0))
            ).collect(Collectors.toList()));
        }
        boolean proformaExistedBefore = proformaRepository.findByOrdreReparationId(id).isPresent();
        updateOrdreReparation(id, req);

        if (proformaExistedBefore) {
            Proforma proforma = proformaService.getByOrdreReparationId(id);
            if (proforma != null) {
                ProformaUpdateRequest proformaReq = new ProformaUpdateRequest();
                if (dto.getLignesPieces() != null) {
                    proformaReq.setLignesPieces(dto.getLignesPieces().stream().map(l -> {
                        LigneFacturationPieceRequest pReq = new LigneFacturationPieceRequest();
                        pReq.setPieceId(l.getPieceId());
                        pReq.setQuantite(l.getQuantite());
                        pReq.setPrix(l.getPrix() != null ? l.getPrix().intValue() : 0);
                        pReq.setIsCustom(l.getIsCustom());
                        pReq.setDesignationPds(l.getDesignationPds());
                        return pReq;
                    }).collect(Collectors.toList()));
                }
                if (dto.getLignesMainDoeuvres() != null) {
                    proformaReq.setLignesMainDoeuvres(dto.getLignesMainDoeuvres().stream().map(l -> {
                        LigneFacturationMainDoeuvreRequest mReq = new LigneFacturationMainDoeuvreRequest();
                        mReq.setMainDoeuvreId(l.getMainDoeuvreId());
                        mReq.setNbreHeure(l.getNbreHeure());
                        mReq.setTarifHoraire(l.getPrix() != null ? l.getPrix().intValue() : 0);
                        return mReq;
                    }).collect(Collectors.toList()));
                }
                proformaService.update(proforma.getId(), proformaReq);
            }
        }

        return dto;
    }

    @Override
    @Transactional
    public StepProformaDto updateStepProforma(Long id, StepProformaDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepApprovisionnementDto updateStepApprovisionnement(Long id, StepApprovisionnementDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepBonSortieDto updateStepBonSortie(Long id, StepBonSortieDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepAssignationDto updateStepAssignation(Long id, StepAssignationDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        if (dto.getTechniciensIds() != null) {
            ordre.getTechniciensReparation().clear();
            for (Long tId : dto.getTechniciensIds()) {
                Technicien t = technicienRepository.findById(tId).orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
                ordre.getTechniciensReparation().add(t);
            }
        }
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepReparationDto updateStepReparation(Long id, StepReparationDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepPaiementDto updateStepPaiement(Long id, StepPaiementDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepPretALivrerDto updateStepPretALivrer(Long id, StepPretALivrerDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);
        return dto;
    }

    @Override
    @Transactional
    public StepLivraisonDto updateStepLivraison(Long id, StepLivraisonDto dto) {
        OrdreReparation ordre = ordreReparationRepository.findById(id).orElseThrow(() -> new RuntimeException("Fiche Atelier non trouvée"));
        updateBaseStepFields(ordre, dto);
        ordreReparationRepository.save(ordre);
        return dto;
    }
}
