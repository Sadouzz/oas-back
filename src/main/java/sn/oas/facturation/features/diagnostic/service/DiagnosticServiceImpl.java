package sn.oas.facturation.features.diagnostic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.diagnostic.data.entity.Diagnostic;
import sn.oas.facturation.features.diagnostic.data.entity.PieceJointeDiagnostic;
import sn.oas.facturation.features.diagnostic.data.entity.RemarqueDiagnostic;
import sn.oas.facturation.features.diagnostic.data.enums.StatutDiagnostic;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticListResponse;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.DiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticRequest;
import sn.oas.facturation.features.diagnostic.dto.PieceJointeDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.dto.RemarqueDiagnosticResponse;
import sn.oas.facturation.features.diagnostic.repository.DiagnosticRepository;
import sn.oas.facturation.features.diagnostic.repository.PieceJointeDiagnosticRepository;
import sn.oas.facturation.features.diagnostic.repository.RemarqueDiagnosticRepository;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.dto.steps.DiagnosticStepDto;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.technicien.repository.TechnicienRepository;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.io.ByteArrayOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;

import com.lowagie.text.pdf.PdfWriter;
import sn.oas.facturation.features.pdfGenerator.service.OasBrandedPdfPageEvent;
import sn.oas.facturation.features.notification.service.EmailService;
import sn.oas.facturation.features.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;


@Service
@RequiredArgsConstructor
@Slf4j
public class DiagnosticServiceImpl implements DiagnosticService {

    private final DiagnosticRepository diagnosticRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final TechnicienRepository technicienRepository;
    private final PieceJointeDiagnosticRepository pieceJointeDiagnosticRepository;
    private final RemarqueDiagnosticRepository remarqueDiagnosticRepository;
    private final sn.oas.facturation.features.pdfGenerator.service.HtmlToPdfService htmlToPdfService;
    private final EmailService emailService;
    private final sn.oas.facturation.features.notification.service.ClientEmailTemplateService clientEmailTemplateService;
    private final sn.oas.facturation.features.notification.service.MailDeliveryAsyncService mailDeliveryAsyncService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public DiagnosticResponse create(DiagnosticRequest request) {
        if (request.getOrdreReparationId() == null) {
            throw new IllegalArgumentException("L'ID de l'ordre de réparation est obligatoire");
        }

        OrdreReparation ordreReparation = ordreReparationRepository.findById(request.getOrdreReparationId())
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé avec l'id : " + request.getOrdreReparationId()));

        verifierDiagnosticModifiable(ordreReparation);

        if (diagnosticRepository.existsByOrdreReparationId(ordreReparation.getId())) {
            throw new IllegalStateException("Un diagnostic existe déjà pour cet ordre de réparation");
        }

        List<Technicien> techniciens = resolveTechniciens(
                request.getTechnicienIds(), request.getTechnicienId(), ordreReparation);
        if (request.getStatut() == StatutDiagnostic.TERMINE || request.getStatut() == StatutDiagnostic.VALIDE
                || (request.getStatut() == StatutDiagnostic.EN_COURS && techniciens.isEmpty())) {
            throw new IllegalArgumentException("Le diagnostic passe en cours avec une affectation et se termine au passage à l'étape suivante.");
        }
        Technicien technicien = techniciens.stream().findFirst().orElse(null);

        Diagnostic diagnostic = Diagnostic.builder()
                .ordreReparation(ordreReparation)
                .garage(ordreReparation.getGarage())
                .technicien(technicien)
                .observations(request.getObservations())
                .pannesDetectees(request.getPannesDetectees())
                .recommandations(request.getRecommandations())
                .kilometrage(request.getKilometrage())
                .techniciens(new LinkedHashSet<>(techniciens))
                .statut(techniciens.isEmpty() ? StatutDiagnostic.EN_ATTENTE : StatutDiagnostic.EN_COURS)
                .dateDebut(request.getDateDebut() != null ? request.getDateDebut()
                        : (techniciens.isEmpty() ? null : LocalDateTime.now()))
                .dateFin(request.getDateFin())
                .build();

        if (request.getPannesDetectees() != null) {
            ordreReparation.setListeDefauts(request.getPannesDetectees());
            ordreReparationRepository.save(ordreReparation);
        }

        Diagnostic saved = diagnosticRepository.save(diagnostic);
        if (!techniciens.isEmpty() && (ordreReparation.getStatut() == null
                || ordreReparation.getStatut() == sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.RECEPTION)) {
            ordreReparation.setStatut(sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.DIAGNOSTIC);
            ordreReparationRepository.save(ordreReparation);
        }

        // Pièces jointes initiales
        if (request.getPiecesJointes() != null) {
            for (PieceJointeDiagnosticRequest pjReq : request.getPiecesJointes()) {
                if (pjReq.getUrl() != null && pjReq.getType() != null) {
                    PieceJointeDiagnostic pj = PieceJointeDiagnostic.builder()
                            .diagnostic(saved)
                            .ordreReparation(saved.getOrdreReparation())
                            .url(pjReq.getUrl())
                            .type(pjReq.getType())
                            .remarque(pjReq.getRemarque())
                            .technicien(technicien)
                            .build();
                    pieceJointeDiagnosticRepository.save(pj);
                }
            }
        }

        // Remarques initiales
        if (request.getRemarques() != null) {
            for (DiagnosticRequest.RemarqueRequest remReq : request.getRemarques()) {
                if (remReq.getContenu() != null && !remReq.getContenu().isBlank()) {
                    Technicien remTech = technicien;
                    if (remReq.getTechnicienId() != null) {
                        remTech = technicienRepository.findById(remReq.getTechnicienId()).orElse(technicien);
                    }
                    RemarqueDiagnostic rem = RemarqueDiagnostic.builder()
                            .diagnostic(saved)
                            .ordreReparation(saved.getOrdreReparation())
                            .technicien(remTech)
                            .contenu(remReq.getContenu())
                            .build();
                    remarqueDiagnosticRepository.save(rem);
                }
            }
        }

        if (ordreReparation.getVehicule() != null && ordreReparation.getVehicule().getClient() != null) {
            var client = ordreReparation.getVehicule().getClient();
            String clientEmail = client.getEmail();
            String immat = ordreReparation.getVehicule().getImmatriculation();
            Long diagId = saved.getId();
            mailDeliveryAsyncService.sendEmailWithPdfAttachmentAsync(
                    clientEmail,
                    "Diagnostic de votre véhicule · " + (immat != null ? immat : "Dossier OAS"),
                    clientEmailTemplateService.diagnostic(client.getFirstName(), immat),
                    "Diagnostic_" + diagId + ".pdf",
                    () -> generatePdf(diagId),
                    true
            );
        }

        return toDiagnosticResponse(saved);
    }

    @Override
    @Transactional
    public DiagnosticResponse update(Long id, DiagnosticRequest request) {
        Diagnostic diagnostic = diagnosticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + id));

        verifierDiagnosticModifiable(diagnostic.getOrdreReparation());
        if (request.getStatut() == StatutDiagnostic.TERMINE || request.getStatut() == StatutDiagnostic.VALIDE) {
            throw new IllegalArgumentException("Le diagnostic se termine en passant à l'étape suivante de l'ordre de réparation.");
        }

        if (request.getTechnicienId() != null || request.getTechnicienIds() != null) {
            diagnostic.remplacerTechniciens(resolveTechniciens(
                    request.getTechnicienIds(), request.getTechnicienId(), diagnostic.getOrdreReparation()));
            if (!diagnostic.getTechniciens().isEmpty()
                    && (diagnostic.getStatut() == null || diagnostic.getStatut() == StatutDiagnostic.EN_ATTENTE)) {
                diagnostic.setStatut(StatutDiagnostic.EN_COURS);
                if (diagnostic.getDateDebut() == null) diagnostic.setDateDebut(LocalDateTime.now());
            } else if (diagnostic.getTechniciens().isEmpty() && diagnostic.getStatut() == StatutDiagnostic.EN_COURS) {
                diagnostic.setStatut(StatutDiagnostic.EN_ATTENTE);
            }
            OrdreReparation ordre = diagnostic.getOrdreReparation();
            if (!techniciensAffectes(diagnostic).isEmpty() && ordre != null
                    && (ordre.getStatut() == null
                    || ordre.getStatut() == sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.RECEPTION)) {
                ordre.setStatut(sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.DIAGNOSTIC);
            }
        }

        if (request.getObservations() != null) {
            diagnostic.setObservations(request.getObservations());
        }
        if (request.getPannesDetectees() != null) {
            diagnostic.setPannesDetectees(request.getPannesDetectees());
            if (diagnostic.getOrdreReparation() != null) {
                diagnostic.getOrdreReparation().setListeDefauts(request.getPannesDetectees());
                ordreReparationRepository.save(diagnostic.getOrdreReparation());
            }
        }
        if (request.getRecommandations() != null) {
            diagnostic.setRecommandations(request.getRecommandations());
        }
        if (request.getKilometrage() != null) {
            diagnostic.setKilometrage(request.getKilometrage());
        }
        if (request.getStatut() != null) {
            if (request.getStatut() == StatutDiagnostic.EN_COURS && techniciensAffectes(diagnostic).isEmpty()) {
                throw new IllegalArgumentException("Affectez au moins un technicien avant de démarrer le diagnostic.");
            }
            if (request.getStatut() == StatutDiagnostic.EN_ATTENTE && !techniciensAffectes(diagnostic).isEmpty()) {
                throw new IllegalArgumentException("Retirez tous les techniciens avant de remettre le diagnostic en attente.");
            }
            diagnostic.setStatut(request.getStatut());
        }
        if (request.getDateDebut() != null) {
            diagnostic.setDateDebut(request.getDateDebut());
        }
        if (request.getDateFin() != null) {
            diagnostic.setDateFin(request.getDateFin());
        }

        return toDiagnosticResponse(diagnosticRepository.save(diagnostic));
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticResponse getById(Long id) {
        Diagnostic diagnostic = diagnosticRepository.findByIdWithDetails(id)
                .orElseGet(() -> diagnosticRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + id)));
        return toDiagnosticResponse(diagnostic);
    }

    @Override
    @Transactional(readOnly = true)
    public DiagnosticResponse getByOrdreReparationId(Long ordreReparationId) {
        Diagnostic diagnostic = diagnosticRepository.findByOrdreReparationId(ordreReparationId)
                .orElseThrow(() -> new RuntimeException("Aucun diagnostic trouvé pour l'ordre de réparation id : " + ordreReparationId));
        return toDiagnosticResponse(diagnostic);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiagnosticListResponse> getAll(Pageable pageable, String search, StatutDiagnostic statut) {
        if (search != null && !search.trim().isEmpty()) {
            return diagnosticRepository.searchDiagnostics(search.trim(), pageable).map(this::toDiagnosticListResponse);
        }
        if (statut != null) {
            return diagnosticRepository.findByStatutWithDetails(statut, pageable).map(this::toDiagnosticListResponse);
        }
        return diagnosticRepository.findAllWithDetails(pageable).map(this::toDiagnosticListResponse);
    }

    @Override
    @Transactional
    public DiagnosticResponse updateStatut(Long id, StatutDiagnostic statut) {
        Diagnostic diagnostic = diagnosticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + id));
        if (diagnostic.getOrdreReparation() != null && diagnostic.getOrdreReparation().getStatut() != null
                && diagnostic.getOrdreReparation().getStatut().getEtape()
                >= sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.PIECES_MO.getEtape()) {
            throw new IllegalArgumentException("Le diagnostic est clôturé et son statut ne peut plus être modifié.");
        }
        if (statut == StatutDiagnostic.TERMINE || statut == StatutDiagnostic.VALIDE) {
            throw new IllegalArgumentException("Le diagnostic se termine en passant à l'étape suivante de l'ordre de réparation.");
        }
        boolean hasTechnicians = !techniciensAffectes(diagnostic).isEmpty();
        if (statut == StatutDiagnostic.EN_COURS && !hasTechnicians) {
            throw new IllegalArgumentException("Affectez au moins un technicien avant de démarrer le diagnostic.");
        }
        if (statut == StatutDiagnostic.EN_ATTENTE && hasTechnicians) {
            throw new IllegalArgumentException("Retirez tous les techniciens avant de remettre le diagnostic en attente.");
        }
        diagnostic.setStatut(statut);
        Diagnostic saved = diagnosticRepository.save(diagnostic);
        return toDiagnosticResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Diagnostic diagnostic = diagnosticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + id));
        verifierDiagnosticModifiable(diagnostic.getOrdreReparation());
        diagnosticRepository.delete(diagnostic);
    }

    @Override
    @Transactional
    public DiagnosticResponse saveFromStep(DiagnosticStepDto stepDto) {
        if (stepDto.getOrdreReparationId() == null) {
            throw new IllegalArgumentException("L'ordreReparationId est requis");
        }

        OrdreReparation ordreReparation = ordreReparationRepository.findById(stepDto.getOrdreReparationId())
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé : " + stepDto.getOrdreReparationId()));

        verifierDiagnosticModifiable(ordreReparation);

        if (stepDto.getListeDefauts() != null) {
            ordreReparation.setListeDefauts(stepDto.getListeDefauts());
            ordreReparationRepository.save(ordreReparation);
        }

        List<Technicien> techniciens = stepDto.getTechnicienIds() == null ? null
                : resolveTechniciens(stepDto.getTechnicienIds(), null, ordreReparation);
        Technicien technicien = techniciens == null ? null : techniciens.stream().findFirst().orElse(null);

        Diagnostic diagnostic = diagnosticRepository.findByOrdreReparationId(ordreReparation.getId())
                .orElseGet(() -> Diagnostic.builder()
                        .ordreReparation(ordreReparation)
                        .garage(ordreReparation.getGarage())
                        .statut(techniciens != null && !techniciens.isEmpty() ? StatutDiagnostic.EN_COURS : StatutDiagnostic.EN_ATTENTE)
                        .dateDebut(techniciens != null && !techniciens.isEmpty() ? LocalDateTime.now() : null)
                        .build());

        if (techniciens != null) {
            diagnostic.remplacerTechniciens(techniciens);
            if (!techniciens.isEmpty()) {
                if (diagnostic.getStatut() == null || diagnostic.getStatut() == StatutDiagnostic.EN_ATTENTE) {
                    diagnostic.setStatut(StatutDiagnostic.EN_COURS);
                }
                if (diagnostic.getDateDebut() == null) diagnostic.setDateDebut(LocalDateTime.now());
            } else if (diagnostic.getStatut() == StatutDiagnostic.EN_COURS) {
                diagnostic.setStatut(StatutDiagnostic.EN_ATTENTE);
            }
        }
        if (stepDto.getListeDefauts() != null) {
            diagnostic.setPannesDetectees(stepDto.getListeDefauts());
            diagnostic.setObservations(stepDto.getListeDefauts());
        }

        Diagnostic saved = diagnosticRepository.save(diagnostic);

        if (techniciens != null && !techniciens.isEmpty()
                && (ordreReparation.getStatut() == null
                || ordreReparation.getStatut() == sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.RECEPTION)) {
            ordreReparation.setStatut(sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.DIAGNOSTIC);
            ordreReparationRepository.save(ordreReparation);
        }

        // Ajout des pièces jointes fournies
        if (stepDto.getPiecesJointes() != null) {
            for (DiagnosticStepDto.PieceJointeDiagnosticDto pjDto : stepDto.getPiecesJointes()) {
                if (pjDto.getUrl() != null && pjDto.getType() != null) {
                    PieceJointeDiagnostic pj = PieceJointeDiagnostic.builder()
                            .diagnostic(saved)
                            .url(pjDto.getUrl())
                            .type(pjDto.getType())
                            .remarque(pjDto.getRemarque())
                            .technicien(technicien)
                            .build();
                    pieceJointeDiagnosticRepository.save(pj);
                }
            }
        }

        // Ajout des remarques fournies
        if (stepDto.getRemarques() != null) {
            for (DiagnosticStepDto.RemarqueDiagnosticDto remDto : stepDto.getRemarques()) {
                if (remDto.getContenu() != null && !remDto.getContenu().isBlank()) {
                    Technicien remTech = technicien;
                    if (remDto.getTechnicienId() != null) {
                        remTech = technicienRepository.findById(remDto.getTechnicienId()).orElse(technicien);
                    }
                    RemarqueDiagnostic rem = RemarqueDiagnostic.builder()
                            .diagnostic(saved)
                            .technicien(remTech)
                            .contenu(remDto.getContenu())
                            .build();
                    remarqueDiagnosticRepository.save(rem);
                }
            }
        }

        if (ordreReparation.getVehicule() != null && ordreReparation.getVehicule().getClient() != null) {
            var client = ordreReparation.getVehicule().getClient();
            String clientEmail = client.getEmail();
            String immat = ordreReparation.getVehicule().getImmatriculation();
            Long diagId = saved.getId();
            mailDeliveryAsyncService.sendEmailWithPdfAttachmentAsync(
                    clientEmail,
                    "Diagnostic de votre véhicule · " + (immat != null ? immat : "Dossier OAS"),
                    clientEmailTemplateService.diagnostic(client.getFirstName(), immat),
                    "Diagnostic_" + diagId + ".pdf",
                    () -> generatePdf(diagId),
                    true
            );
        }

        return toDiagnosticResponse(saved);
    }

    @Override
    @Transactional
    public DiagnosticResponse terminerDepuisEtape(DiagnosticStepDto stepDto) {
        if (stepDto == null || stepDto.getOrdreReparationId() == null) {
            throw new IllegalArgumentException("L'ordreReparationId est requis");
        }
        OrdreReparation ordre = ordreReparationRepository.findById(stepDto.getOrdreReparationId())
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé : " + stepDto.getOrdreReparationId()));
        Diagnostic existing = diagnosticRepository.findByOrdreReparationId(ordre.getId()).orElse(null);
        if (ordre.getStatut() != null
                && ordre.getStatut().getEtape() >= sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.PIECES_MO.getEtape()) {
            if (ordre.getStatut() == sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.PIECES_MO
                    && existing != null && existing.getStatut() == StatutDiagnostic.TERMINE) {
                return toDiagnosticResponse(existing);
            }
            throw new IllegalArgumentException("L'ordre a déjà dépassé l'étape du diagnostic.");
        }
        if (ordre.getStatut() != sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.DIAGNOSTIC) {
            throw new IllegalArgumentException("L'ordre doit être à l'étape Diagnostic pour pouvoir la terminer.");
        }
        List<Technicien> techniciensDejaAffectes = existing != null ? techniciensAffectes(existing) : List.of();
        if (techniciensDejaAffectes.isEmpty()) {
            throw new IllegalArgumentException("Affectez au moins un technicien avant de terminer le diagnostic.");
        }
        List<Long> idsAffectes = techniciensDejaAffectes.stream().map(Technicien::getId).toList();
        if (stepDto.getTechnicienIds() != null) {
            Set<Long> idsDemandes = stepDto.getTechnicienIds().stream().filter(Objects::nonNull).collect(Collectors.toSet());
            if (!idsDemandes.equals(new LinkedHashSet<>(idsAffectes))) {
                throw new IllegalArgumentException("L'équipe du diagnostic a changé. Rechargez l'ordre avant de le terminer.");
            }
        }
        // La clôture consomme l'équipe persistée côté serveur ; elle ne peut pas affecter ou retirer des techniciens.
        stepDto.setTechnicienIds(idsAffectes);

        DiagnosticResponse savedResponse = saveFromStep(stepDto);
        Diagnostic diagnostic = diagnosticRepository.findById(savedResponse.getId())
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé après enregistrement"));
        if (techniciensAffectes(diagnostic).isEmpty()) {
            throw new IllegalArgumentException("Affectez au moins un technicien avant de terminer le diagnostic.");
        }

        diagnostic.setStatut(StatutDiagnostic.TERMINE);
        diagnostic.setDateFin(LocalDateTime.now());
        ordre.setStatut(sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.PIECES_MO);
        ordre.setUpdatedAt(LocalDateTime.now());
        diagnosticRepository.save(diagnostic);
        ordreReparationRepository.save(ordre);
        if (ordre.getVehicule() != null && ordre.getVehicule().getClient() != null) {
            notificationService.sendInAppNotification(ordre.getVehicule().getClient(), "Diagnostic terminé",
                    "Le diagnostic de votre véhicule " + ordre.getVehicule().getImmatriculation()
                            + " est terminé. Consultez votre espace client pour le compte rendu.");
        }
        return toDiagnosticResponse(diagnostic);
    }

    @Override
    @Transactional
    public PieceJointeDiagnosticResponse addPieceJointe(Long diagnosticId, PieceJointeDiagnosticRequest request) {
        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + diagnosticId));

        verifierDiagnosticModifiable(diagnostic.getOrdreReparation());

        if (request.getUrl() == null || request.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("L'URL de la pièce jointe est obligatoire");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("Le type de la pièce jointe est obligatoire");
        }

        PieceJointeDiagnostic pj = PieceJointeDiagnostic.builder()
                .diagnostic(diagnostic)
                .ordreReparation(diagnostic.getOrdreReparation())
                .url(request.getUrl())
                .type(request.getType())
                .remarque(request.getRemarque())
                .technicien(diagnostic.getTechnicien())
                .build();

        return toPieceJointeResponse(pieceJointeDiagnosticRepository.save(pj));
    }

    @Override
    @Transactional
    public void deletePieceJointe(Long pieceJointeId) {
        PieceJointeDiagnostic pj = pieceJointeDiagnosticRepository.findById(pieceJointeId)
                .orElseThrow(() -> new RuntimeException("Pièce jointe non trouvée avec l'id : " + pieceJointeId));
        verifierDiagnosticModifiable(pj.getDiagnostic() != null ? pj.getDiagnostic().getOrdreReparation() : null);
        pieceJointeDiagnosticRepository.delete(pj);
    }

    @Override
    @Transactional
    public RemarqueDiagnosticResponse addRemarque(Long diagnosticId, String contenu, Long technicienId) {
        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + diagnosticId));

        verifierDiagnosticModifiable(diagnostic.getOrdreReparation());

        if (contenu == null || contenu.isBlank()) {
            throw new IllegalArgumentException("Le contenu de la remarque est obligatoire");
        }

        Technicien tech = null;
        if (technicienId != null) {
            tech = technicienRepository.findById(technicienId).orElse(null);
        } else {
            tech = diagnostic.getTechnicien();
        }

        RemarqueDiagnostic rem = RemarqueDiagnostic.builder()
                .diagnostic(diagnostic)
                .ordreReparation(diagnostic.getOrdreReparation())
                .technicien(tech)
                .contenu(contenu)
                .build();

        return toRemarqueResponse(remarqueDiagnosticRepository.save(rem));
    }

    @Override
    @Transactional
    public void deleteRemarque(Long remarqueId) {
        RemarqueDiagnostic rem = remarqueDiagnosticRepository.findById(remarqueId)
                .orElseThrow(() -> new RuntimeException("Remarque non trouvée avec l'id : " + remarqueId));
        verifierDiagnosticModifiable(rem.getDiagnostic() != null ? rem.getDiagnostic().getOrdreReparation() : null);
        remarqueDiagnosticRepository.delete(rem);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generatePdf(Long id) {
        Diagnostic diagnostic = diagnosticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + id));

        var vehicle = diagnostic.getOrdreReparation() == null ? null : diagnostic.getOrdreReparation().getVehicule();
        byte[] configured = htmlToPdfService.genererTemplatePdfSiConfigure("DIAGNOSTIC", java.util.Map.of(
                "numero", templateValue(diagnostic.getOrdreReparation() == null ? null : diagnostic.getOrdreReparation().getNumero()),
                "date", templateValue(diagnostic.getDateDebut()), "statut", templateValue(diagnostic.getStatut()),
                "immatriculation", vehicle == null ? "" : templateValue(vehicle.getImmatriculation()),
                "marque", vehicle == null ? "" : templateValue(vehicle.getMarque()), "modele", vehicle == null ? "" : templateValue(vehicle.getModele()),
                "observations", templateValue(diagnostic.getObservations()), "pannes", templateValue(diagnostic.getPannesDetectees()),
                "recommandations", templateValue(diagnostic.getRecommandations())));
        if (configured != null) return configured;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new OasBrandedPdfPageEvent());
            document.open();

            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font fontTexte = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph titre = new Paragraph("FICHE DE DIAGNOSTIC", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingAfter(20);
            document.add(titre);

            if (diagnostic.getOrdreReparation() != null && diagnostic.getOrdreReparation().getVehicule() != null) {
                Vehicule v = diagnostic.getOrdreReparation().getVehicule();
                document.add(new Paragraph("Véhicule : " + v.getMarque() + " " + v.getModele() + " (" + v.getImmatriculation() + ")", fontSousTitre));
            }
            
            document.add(new Paragraph("Kilométrage : " + (diagnostic.getKilometrage() != null ? diagnostic.getKilometrage() : "Non renseigné"), fontTexte));
            document.add(new Paragraph("Date : " + diagnostic.getDateDebut(), fontTexte));
            
            if (!techniciensAffectes(diagnostic).isEmpty()) {
                String noms = techniciensAffectes(diagnostic).stream()
                        .map(this::formatTechnicienNom).collect(Collectors.joining(", "));
                document.add(new Paragraph("Techniciens : " + noms, fontTexte));
            }

            document.add(new Paragraph(" "));
            
            document.add(new Paragraph("Pannes Détectées :", fontSousTitre));
            document.add(new Paragraph(diagnostic.getPannesDetectees() != null ? diagnostic.getPannesDetectees() : "Aucune", fontTexte));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Observations :", fontSousTitre));
            document.add(new Paragraph(diagnostic.getObservations() != null ? diagnostic.getObservations() : "Aucune", fontTexte));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Recommandations :", fontSousTitre));
            document.add(new Paragraph(diagnostic.getRecommandations() != null ? diagnostic.getRecommandations() : "Aucune", fontTexte));
            document.add(new Paragraph(" "));

        } catch (Exception e) {
            log.error("Erreur lors de la génération du PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private String templateValue(Object value) { return value == null ? "" : value.toString(); }

    public DiagnosticListResponse toDiagnosticListResponse(Diagnostic d) {
        OrdreReparation or = d.getOrdreReparation();
        Long orId = or != null ? or.getId() : null;
        String orNum = or != null ? or.getNumero() : null;

        DiagnosticListResponse.VehiculeSummary vehiculeSummary = null;
        if (or != null && or.getVehicule() != null) {
            Vehicule v = or.getVehicule();
            DiagnosticListResponse.ClientSummary clientSummary = null;
            if (v.getClient() != null) {
                Client c = v.getClient();
                clientSummary = DiagnosticListResponse.ClientSummary.builder()
                        .id(c.getId())
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .phone(c.getPhone())
                        .email(c.getEmail())
                        .build();
            }
            vehiculeSummary = DiagnosticListResponse.VehiculeSummary.builder()
                    .id(v.getId())
                    .immatriculation(v.getImmatriculation())
                    .marque(v.getMarque())
                    .modele(v.getModele())
                    .client(clientSummary)
                    .build();
        }

        Long techId = null;
        String techNom = null;
        List<DiagnosticListResponse.TechnicienSummary> techniciensList = new ArrayList<>();

        for (Technicien t : techniciensAffectes(d)) {
            if (techId == null) {
                techId = t.getId();
                techNom = formatTechnicienNom(t);
            }
            techniciensList.add(DiagnosticListResponse.TechnicienSummary.builder()
                    .id(t.getId())
                    .firstName(t.getFirstName())
                    .lastName(t.getLastName())
                    .phone(t.getPhone())
                    .specialite(t.getSpecialite() != null ? t.getSpecialite().name() : null)
                    .build());
        }

        if (or != null && or.getTechniciensReparation() != null) {
            for (Technicien t : or.getTechniciensReparation()) {
                if (techId == null || !t.getId().equals(techId)) {
                    techniciensList.add(DiagnosticListResponse.TechnicienSummary.builder()
                            .id(t.getId())
                            .firstName(t.getFirstName())
                            .lastName(t.getLastName())
                            .phone(t.getPhone())
                            .specialite(t.getSpecialite() != null ? t.getSpecialite().name() : null)
                            .build());
                }
            }
        }

        return DiagnosticListResponse.builder()
                .id(d.getId())
                .ordreReparationId(orId)
                .ordreReparationNumero(orNum)
                .vehicule(vehiculeSummary)
                .technicienId(techId)
                .technicienNom(techNom)
                .techniciens(techniciensList)
                .observations(d.getObservations())
                .pannesDetectees(d.getPannesDetectees())
                .recommandations(d.getRecommandations())
                .kilometrage(d.getKilometrage())
                .statut(d.getStatut())
                .dateDebut(d.getDateDebut())
                .dateFin(d.getDateFin())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private DiagnosticResponse toDiagnosticResponse(Diagnostic d) {
        List<PieceJointeDiagnosticResponse> pjs = pieceJointeDiagnosticRepository
                .findByDiagnosticIdOrderByCreatedAtDesc(d.getId())
                .stream().map(this::toPieceJointeResponse).collect(Collectors.toList());

        List<RemarqueDiagnosticResponse> rems = remarqueDiagnosticRepository
                .findByDiagnosticIdOrderByCreatedAtDesc(d.getId())
                .stream().map(this::toRemarqueResponse).collect(Collectors.toList());

        OrdreReparation or = d.getOrdreReparation();
        Long orId = or != null ? or.getId() : null;
        String orNum = or != null ? or.getNumero() : null;

        DiagnosticListResponse.VehiculeSummary vehiculeSummary = null;
        if (or != null && or.getVehicule() != null) {
            Vehicule v = or.getVehicule();
            DiagnosticListResponse.ClientSummary clientSummary = null;
            if (v.getClient() != null) {
                Client c = v.getClient();
                clientSummary = DiagnosticListResponse.ClientSummary.builder()
                        .id(c.getId())
                        .firstName(c.getFirstName())
                        .lastName(c.getLastName())
                        .phone(c.getPhone())
                        .email(c.getEmail())
                        .build();
            }
            vehiculeSummary = DiagnosticListResponse.VehiculeSummary.builder()
                    .id(v.getId())
                    .immatriculation(v.getImmatriculation())
                    .marque(v.getMarque())
                    .modele(v.getModele())
                    .client(clientSummary)
                    .build();
        }

        Long techId = null;
        String techNom = null;
        List<Long> techIds = new ArrayList<>();
        List<DiagnosticListResponse.TechnicienSummary> techniciensList = new ArrayList<>();

        for (Technicien t : techniciensAffectes(d)) {
            if (techId == null) {
                techId = t.getId();
                techNom = formatTechnicienNom(t);
            }
            techIds.add(t.getId());
            techniciensList.add(DiagnosticListResponse.TechnicienSummary.builder()
                    .id(t.getId())
                    .firstName(t.getFirstName())
                    .lastName(t.getLastName())
                    .phone(t.getPhone())
                    .specialite(t.getSpecialite() != null ? t.getSpecialite().name() : null)
                    .build());
        }

        List<Long> technicienIdsDiagnostic = new ArrayList<>(techIds);

        if (or != null && or.getTechniciensReparation() != null) {
            for (Technicien t : or.getTechniciensReparation()) {
                if (!techIds.contains(t.getId())) {
                    techIds.add(t.getId());
                    techniciensList.add(DiagnosticListResponse.TechnicienSummary.builder()
                            .id(t.getId())
                            .firstName(t.getFirstName())
                            .lastName(t.getLastName())
                            .phone(t.getPhone())
                            .specialite(t.getSpecialite() != null ? t.getSpecialite().name() : null)
                            .build());
                }
            }
        }

        return DiagnosticResponse.builder()
                .id(d.getId())
                .ordreReparationId(orId)
                .ordreReparationNumero(orNum)
                .vehicule(vehiculeSummary)
                .technicienId(techId)
                .technicienNom(techNom)
                .technicienIds(technicienIdsDiagnostic)
                .techniciens(techniciensList)
                .observations(d.getObservations())
                .pannesDetectees(d.getPannesDetectees())
                .recommandations(d.getRecommandations())
                .kilometrage(d.getKilometrage())
                .statut(d.getStatut())
                .dateDebut(d.getDateDebut())
                .dateFin(d.getDateFin())
                .piecesJointes(pjs)
                .remarques(rems)
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    private String formatTechnicienNom(Technicien t) {
        String prenom = t.getFirstName() != null ? t.getFirstName() : "";
        String nom = t.getLastName() != null ? t.getLastName() : "";
        String full = (prenom + " " + nom).trim();
        return full.isEmpty() ? t.getUsername() : full;
    }

    private List<Technicien> techniciensAffectes(Diagnostic diagnostic) {
        if (diagnostic.getTechniciens() != null && !diagnostic.getTechniciens().isEmpty()) {
            return new ArrayList<>(diagnostic.getTechniciens());
        }
        return diagnostic.getTechnicien() != null ? List.of(diagnostic.getTechnicien()) : List.of();
    }

    private void verifierDiagnosticModifiable(OrdreReparation ordre) {
        if (ordre != null && ordre.getStatut() != null
                && ordre.getStatut().getEtape()
                >= sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation.PIECES_MO.getEtape()) {
            throw new IllegalArgumentException("Le diagnostic est clôturé et ne peut plus être modifié.");
        }
    }

    private List<Technicien> resolveTechniciens(List<Long> ids, Long legacyId, OrdreReparation ordre) {
        LinkedHashSet<Long> distinctIds = new LinkedHashSet<>();
        if (ids != null) ids.stream().filter(Objects::nonNull).forEach(distinctIds::add);
        if (legacyId != null) distinctIds.add(legacyId);

        List<Technicien> techniciens = new ArrayList<>();
        for (Long id : distinctIds) {
            Technicien technicien = technicienRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Technicien non trouvé : " + id));
            Long garageTechnicienId = technicien.getGarage() != null ? technicien.getGarage().getId() : null;
            Long garageOrdreId = ordre.getGarage() != null ? ordre.getGarage().getId() : null;
            if (!Objects.equals(garageTechnicienId, garageOrdreId)) {
                throw new IllegalArgumentException("Tous les techniciens doivent appartenir au même garage que l'ordre.");
            }
            techniciens.add(technicien);
        }
        return techniciens;
    }

    private PieceJointeDiagnosticResponse toPieceJointeResponse(PieceJointeDiagnostic p) {
        String techNom = p.getTechnicien() != null ? formatTechnicienNom(p.getTechnicien()) : null;
        Long orId = (p.getDiagnostic() != null && p.getDiagnostic().getOrdreReparation() != null)
                ? p.getDiagnostic().getOrdreReparation().getId()
                : null;

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

    private RemarqueDiagnosticResponse toRemarqueResponse(RemarqueDiagnostic r) {
        String techNom = r.getTechnicien() != null ? formatTechnicienNom(r.getTechnicien()) : null;
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
}
