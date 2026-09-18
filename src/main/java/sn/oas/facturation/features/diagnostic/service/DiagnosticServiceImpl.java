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
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosticServiceImpl implements DiagnosticService {

    private final DiagnosticRepository diagnosticRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final TechnicienRepository technicienRepository;
    private final PieceJointeDiagnosticRepository pieceJointeDiagnosticRepository;
    private final RemarqueDiagnosticRepository remarqueDiagnosticRepository;

    @Override
    @Transactional
    public DiagnosticResponse create(DiagnosticRequest request) {
        if (request.getOrdreReparationId() == null) {
            throw new IllegalArgumentException("L'ID de l'ordre de réparation est obligatoire");
        }

        OrdreReparation ordreReparation = ordreReparationRepository.findById(request.getOrdreReparationId())
                .orElseThrow(() -> new RuntimeException("Ordre de réparation non trouvé avec l'id : " + request.getOrdreReparationId()));

        if (diagnosticRepository.existsByOrdreReparationId(ordreReparation.getId())) {
            throw new IllegalStateException("Un diagnostic existe déjà pour cet ordre de réparation");
        }

        Technicien technicien = null;
        if (request.getTechnicienId() != null) {
            technicien = technicienRepository.findById(request.getTechnicienId())
                    .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
        } else if (request.getTechnicienIds() != null && !request.getTechnicienIds().isEmpty()) {
            technicien = technicienRepository.findById(request.getTechnicienIds().get(0)).orElse(null);
        }

        Diagnostic diagnostic = Diagnostic.builder()
                .ordreReparation(ordreReparation)
                .garage(ordreReparation.getGarage())
                .technicien(technicien)
                .observations(request.getObservations())
                .pannesDetectees(request.getPannesDetectees())
                .recommandations(request.getRecommandations())
                .kilometrage(request.getKilometrage())
                .statut(request.getStatut() != null ? request.getStatut() : StatutDiagnostic.EN_COURS)
                .dateDebut(request.getDateDebut() != null ? request.getDateDebut() : LocalDateTime.now())
                .dateFin(request.getDateFin())
                .build();

        if (request.getPannesDetectees() != null) {
            ordreReparation.setListeDefauts(request.getPannesDetectees());
            ordreReparationRepository.save(ordreReparation);
        }

        Diagnostic saved = diagnosticRepository.save(diagnostic);

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

        return toDiagnosticResponse(saved);
    }

    @Override
    @Transactional
    public DiagnosticResponse update(Long id, DiagnosticRequest request) {
        Diagnostic diagnostic = diagnosticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + id));

        if (request.getTechnicienId() != null) {
            Technicien technicien = technicienRepository.findById(request.getTechnicienId())
                    .orElseThrow(() -> new RuntimeException("Technicien non trouvé"));
            diagnostic.setTechnicien(technicien);
        } else if (request.getTechnicienIds() != null && !request.getTechnicienIds().isEmpty()) {
            Technicien technicien = technicienRepository.findById(request.getTechnicienIds().get(0)).orElse(null);
            diagnostic.setTechnicien(technicien);
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
        diagnostic.setStatut(statut);
        if (statut == StatutDiagnostic.TERMINE && diagnostic.getDateFin() == null) {
            diagnostic.setDateFin(LocalDateTime.now());
        }
        return toDiagnosticResponse(diagnosticRepository.save(diagnostic));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Diagnostic diagnostic = diagnosticRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + id));
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

        if (stepDto.getListeDefauts() != null) {
            ordreReparation.setListeDefauts(stepDto.getListeDefauts());
            ordreReparationRepository.save(ordreReparation);
        }

        Technicien technicien = null;
        if (stepDto.getTechnicienIds() != null && !stepDto.getTechnicienIds().isEmpty()) {
            technicien = technicienRepository.findById(stepDto.getTechnicienIds().get(0)).orElse(null);
        }

        Diagnostic diagnostic = diagnosticRepository.findByOrdreReparationId(ordreReparation.getId())
                .orElseGet(() -> Diagnostic.builder()
                        .ordreReparation(ordreReparation)
                        .garage(ordreReparation.getGarage())
                        .statut(StatutDiagnostic.EN_COURS)
                        .dateDebut(LocalDateTime.now())
                        .build());

        if (technicien != null) {
            diagnostic.setTechnicien(technicien);
        }
        if (stepDto.getListeDefauts() != null) {
            diagnostic.setPannesDetectees(stepDto.getListeDefauts());
            diagnostic.setObservations(stepDto.getListeDefauts());
        }

        Diagnostic saved = diagnosticRepository.save(diagnostic);

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

        return toDiagnosticResponse(saved);
    }

    @Override
    @Transactional
    public PieceJointeDiagnosticResponse addPieceJointe(Long diagnosticId, PieceJointeDiagnosticRequest request) {
        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + diagnosticId));

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
        pieceJointeDiagnosticRepository.delete(pj);
    }

    @Override
    @Transactional
    public RemarqueDiagnosticResponse addRemarque(Long diagnosticId, String contenu, Long technicienId) {
        Diagnostic diagnostic = diagnosticRepository.findById(diagnosticId)
                .orElseThrow(() -> new RuntimeException("Diagnostic non trouvé avec l'id : " + diagnosticId));

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
        remarqueDiagnosticRepository.delete(rem);
    }

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

        if (d.getTechnicien() != null) {
            Technicien t = d.getTechnicien();
            techId = t.getId();
            techNom = formatTechnicienNom(t);
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

        if (d.getTechnicien() != null) {
            Technicien t = d.getTechnicien();
            techId = t.getId();
            techIds.add(techId);
            techNom = formatTechnicienNom(t);
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
                .technicienIds(techIds)
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
