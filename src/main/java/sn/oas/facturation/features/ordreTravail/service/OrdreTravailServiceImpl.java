package sn.oas.facturation.features.ordreTravail.service;

import com.cloudinary.Cloudinary;
import org.springframework.web.multipart.MultipartFile;
import sn.oas.facturation.features.ordreTravail.data.entity.DocumentOrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.TypeDocumentOt;
import sn.oas.facturation.features.ordreTravail.dto.DocumentOrdreTravailRequestDto;
import sn.oas.facturation.features.ordreTravail.dto.responses.DocumentOrdreTravailResponseDto;
import sn.oas.facturation.features.ordreTravail.repository.DocumentOrdreTravailRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.auth.service.AuthService;
import sn.oas.facturation.features.bonDeCommande.data.entity.BonDeCommande;
import sn.oas.facturation.features.bonDeCommande.data.entity.LigneBonDeCommandePiece;
import sn.oas.facturation.features.bonDeCommande.data.enums.StatutBonCommande;
import sn.oas.facturation.features.bonDeCommande.repository.BonDeCommandeRepository;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortiePiece;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.data.enums.TypeLigneBonDeSortie;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto.PieceApprovisionnementDto;
import sn.oas.facturation.features.client.data.entity.Client;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.ordreReparation.data.entity.OrdreReparation;
import sn.oas.facturation.features.ordreReparation.repository.OrdreReparationRepository;
import sn.oas.facturation.features.ordreTravail.data.entity.LigneTechnicienOrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.entity.OrdreTravail;
import sn.oas.facturation.features.ordreTravail.data.enums.StatutOrdreTravail;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailListResponse;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailRequest;
import sn.oas.facturation.features.ordreTravail.dto.OrdreTravailResponseDto;
import sn.oas.facturation.features.ordreTravail.repository.OrdreTravailRepository;
import sn.oas.facturation.features.proforma.data.entity.Proforma;
import sn.oas.facturation.features.proforma.repository.ProformaRepository;
import sn.oas.facturation.features.technicien.data.entity.Technicien;
import sn.oas.facturation.features.technicien.repository.TechnicienRepository;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.features.user.service.UserService;
import sn.oas.facturation.features.vehicule.data.entity.Vehicule;
import sn.oas.facturation.features.vehicule.service.VehiculeService;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import sn.oas.facturation.features.facture.data.entity.Facture;
import sn.oas.facturation.features.facture.repository.FactureRepository;
import sn.oas.facturation.features.ordreReparation.dto.responses.AdditionalStubs;
import sn.oas.facturation.features.ordreReparation.dto.responses.BonDeSortieSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.ClientHeaderDto;
import sn.oas.facturation.features.ordreTravail.dto.responses.FactureSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.ProformaSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.VehiculeHeaderDto;
import sn.oas.facturation.features.ordreReparation.service.OrdreReparationService;
import sn.oas.facturation.features.ordreTravail.dto.responses.*;
import sn.oas.facturation.features.ordreTravail.dto.steps.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdreTravailServiceImpl implements OrdreTravailService {

    private final OrdreTravailRepository ordreTravailRepository;
    private final OrdreReparationRepository ordreReparationRepository;
    private final OrdreReparationService ordreReparationService;
    private final FactureRepository factureRepository;
    private final ProformaRepository proformaRepository;
    private final BonDeCommandeRepository bonDeCommandeRepository;
    private final BonDeSortieRepository bonDeSortieRepository;
    private final TechnicienRepository technicienRepository;
    private final UserService userService;
    private final VehiculeService vehiculeService;
    private final AuthService authService;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final DocumentOrdreTravailRepository documentOrdreTravailRepository;
    private final Cloudinary cloudinary;

    @Override
    @Transactional
    public OrdreTravailResponseDto creer(OrdreTravailRequest request) {
        if (request == null) {
            throw new BadRequestException("Les données de l'ordre de travail sont obligatoires");
        }

        Agent agent = null;
        try {
            agent = authService.getAgentConnecte();
        } catch (Exception ignored) {
        }

        OrdreReparation ordreReparation = null;
        if (request.getOrdreReparationId() != null) {
            ordreReparation = ordreReparationRepository.findById(request.getOrdreReparationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable : " + request.getOrdreReparationId()));
        }

        Proforma proforma = null;
        if (request.getProformaId() != null) {
            proforma = proformaRepository.findById(request.getProformaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proforma introuvable : " + request.getProformaId()));
        }

        Client client = null;
        if (request.getClientId() != null) {
            client = userService.getClientById(request.getClientId());
        } else if (ordreReparation != null && ordreReparation.getClient() != null) {
            client = ordreReparation.getClient();
        } else if (proforma != null && proforma.getClient() != null) {
            client = proforma.getClient();
        }

        Vehicule vehicule = null;
        if (request.getVehiculeId() != null) {
            vehicule = vehiculeService.getVehiculeById(request.getVehiculeId());
        } else if (ordreReparation != null && ordreReparation.getVehicule() != null) {
            vehicule = ordreReparation.getVehicule();
        } else if (proforma != null && proforma.getVehicule() != null) {
            vehicule = proforma.getVehicule();
        }

        Garage garage = (agent != null && agent.getGarage() != null)
                ? agent.getGarage()
                : (ordreReparation != null && ordreReparation.getGarage() != null
                    ? ordreReparation.getGarage()
                    : (proforma != null && proforma.getGarage() != null
                        ? proforma.getGarage()
                        : documentNumberGeneratorService.getCurrentGarage()));

        String numero = documentNumberGeneratorService.generateNextNumber(garage, DocumentType.OT);

        OrdreTravail ordreTravail = OrdreTravail.builder()
                .numero(numero)
                .garage(garage)
                .client(client)
                .vehicule(vehicule)
                .proforma(proforma)
                .ordreReparation(ordreReparation)
                .statut(request.getStatut() != null ? request.getStatut() : StatutOrdreTravail.RECAPITULATIF)
                .bonsDeCommande(new ArrayList<>())
                .bonsDeSortie(new ArrayList<>())
                .lignesTechniciens(new ArrayList<>())
                .build();

        // Ajout des techniciens
        if (request.getTechnicienIds() != null && !request.getTechnicienIds().isEmpty()) {
            synchroniserTechniciens(ordreTravail, request.getTechnicienIds());
        } else if (ordreReparation != null && ordreReparation.getDiagnostic() != null && ordreReparation.getDiagnostic().getTechnicien() != null) {
            Technicien tech = ordreReparation.getDiagnostic().getTechnicien();
            LigneTechnicienOrdreTravail ligne = LigneTechnicienOrdreTravail.builder()
                    .ordreTravail(ordreTravail)
                    .technicien(tech)
                    .build();
            ordreTravail.getLignesTechniciens().add(ligne);
        }

        // Ajout des bons de commande
        if (request.getBonDeCommandeIds() != null && !request.getBonDeCommandeIds().isEmpty()) {
            List<BonDeCommande> bcs = bonDeCommandeRepository.findAllById(request.getBonDeCommandeIds());
            ordreTravail.getBonsDeCommande().addAll(bcs);
        }

        // Ajout des bons de sortie
        if (request.getBonDeSortieIds() != null && !request.getBonDeSortieIds().isEmpty()) {
            List<BonDeSortie> bss = bonDeSortieRepository.findAllById(request.getBonDeSortieIds());
            for (BonDeSortie bs : bss) {
                ordreTravail.addBonDeSortie(bs);
            }
        } else if (request.getBonDeSortieId() != null) {
            bonDeSortieRepository.findById(request.getBonDeSortieId()).ifPresent(ordreTravail::addBonDeSortie);

        }

        OrdreTravail saved = ordreTravailRepository.save(ordreTravail);
        return OrdreTravailResponseDto.from(saved);
    }

    @Override
    @Transactional
    public OrdreTravailResponseDto creerDepuisOrdreReparation(Long ordreReparationId) {
        if (ordreReparationId == null) {
            throw new BadRequestException("L'identifiant de l'ordre de réparation est obligatoire");
        }

        // Serialize creation attempts for the same OR. The second request then sees
        // and returns the OT created by the first one instead of creating a duplicate.
        OrdreReparation or = ordreReparationRepository.findByIdForOrdreTravailCreation(ordreReparationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable : " + ordreReparationId));

        // Vérifier si un ordre de travail existe déjà pour cet ordre de réparation
        Optional<OrdreTravail> existing = ordreTravailRepository.findByOrdreReparationId(ordreReparationId);
        if (existing.isPresent()) {
            return OrdreTravailResponseDto.from(existing.get());
        }

        List<Long> techIds = new ArrayList<>();
        if (or.getDiagnostic() != null && or.getDiagnostic().getTechnicien() != null) {
            techIds.add(or.getDiagnostic().getTechnicien().getId());
        }
        if (or.getTechniciensReparation() != null) {
            for (Technicien t : or.getTechniciensReparation()) {
                if (!techIds.contains(t.getId())) {
                    techIds.add(t.getId());
                }
            }
        }

        OrdreTravailRequest request = OrdreTravailRequest.builder()
                .ordreReparationId(or.getId())
                .proformaId(or.getProforma() != null ? or.getProforma().getId() : null)
                .clientId(or.getClient() != null ? or.getClient().getId() : (or.getVehicule() != null && or.getVehicule().getClient() != null ? or.getVehicule().getClient().getId() : null))
                .vehiculeId(or.getVehicule() != null ? or.getVehicule().getId() : null)
                .bonDeCommandeIds(or.getBonDeCommandes() != null ? or.getBonDeCommandes().stream().map(BonDeCommande::getId).toList() : new ArrayList<>())
                .bonDeSortieId(or.getBonDeSortie() != null ? or.getBonDeSortie().getId() : null)
                .bonDeSortieIds(or.getBonsDeSortie() != null ? or.getBonsDeSortie().stream().map(BonDeSortie::getId).toList() : new ArrayList<>())
                .technicienIds(techIds)

                .build();

        return creer(request);
    }

    @Override
    @Transactional
    public OrdreTravailResponseDto creerDepuisProforma(Long proformaId) {
        if (proformaId == null) {
            throw new BadRequestException("L'identifiant du proforma est obligatoire");
        }

        Proforma proforma = proformaRepository.findById(proformaId)
                .orElseThrow(() -> new ResourceNotFoundException("Proforma introuvable : " + proformaId));

        if (proforma.getOrdreReparation() == null) {
            throw new BadRequestException("Le proforma #" + proforma.getNumero() + " n'est pas lié à un ordre de réparation");
        }

        // Vérifier si un ordre de travail existe déjà pour ce proforma
        Optional<OrdreTravail> existing = ordreTravailRepository.findByProformaId(proformaId);
        if (existing.isPresent()) {
            return OrdreTravailResponseDto.from(existing.get());
        }

        return creerDepuisOrdreReparation(proforma.getOrdreReparation().getId());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdreTravailResponseDto> getByOrdreReparationId(Long ordreReparationId) {
        return ordreTravailRepository.findByOrdreReparationId(ordreReparationId)
                .map(OrdreTravailResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdreTravailResponseDto> getByProformaId(Long proformaId) {
        return ordreTravailRepository.findByProformaId(proformaId)
                .map(OrdreTravailResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdreTravailSummaryDto getSummary(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));
        return buildSummaryDto(ot);
    }

    @Override
    @Transactional(readOnly = true)
    public OrdreTravailSummaryDto getSummaryByOrdreReparationId(Long ordreReparationId) {
        OrdreTravail ot = ordreTravailRepository.findByOrdreReparationId(ordreReparationId)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun ordre de travail associé à l'ordre de réparation : " + ordreReparationId));
        return buildSummaryDto(ot);
    }

    private OrdreTravailSummaryDto buildSummaryDto(OrdreTravail ot) {
        OrdreReparation ordre = ot.getOrdreReparation();
        Proforma proforma = ot.getProforma();
        if (proforma == null && ordre != null) {
            proforma = ordre.getProforma();
            if (proforma == null) {
                proforma = proformaRepository.findByOrdreReparationId(ordre.getId()).orElse(null);
            }
        }

        Vehicule vehicule = ot.getVehicule();
        if (vehicule == null && ordre != null) {
            vehicule = ordre.getVehicule();
        }

        Client client = ot.getClient();
        if (client == null && vehicule != null) {
            client = vehicule.getClient();
        }
        if (client == null && ordre != null) {
            client = ordre.getClient();
        }

        VehiculeHeaderDto vehiculeDto = null;
        if (vehicule != null) {
            vehiculeDto = new VehiculeHeaderDto();
            vehiculeDto.setId(vehicule.getId());
            vehiculeDto.setImmatriculation(vehicule.getImmatriculation());
            vehiculeDto.setMarque(vehicule.getMarque());
            vehiculeDto.setModele(vehicule.getModele());
            vehiculeDto.setKilometrage(vehicule.getKilometrage() != null ? vehicule.getKilometrage().intValue() : null);

            Client owner = vehicule.getClient() != null ? vehicule.getClient() : client;
            if (owner != null) {
                ClientHeaderDto cDto = new ClientHeaderDto();
                cDto.setId(owner.getId());
                cDto.setFirstName(owner.getFirstName());
                cDto.setLastName(owner.getLastName());
                cDto.setPhone(owner.getPhone());
                vehiculeDto.setClient(cDto);
            }
        }

        ClientHeaderDto clientDto = null;
        if (client != null) {
            clientDto = new ClientHeaderDto();
            clientDto.setId(client.getId());
            clientDto.setFirstName(client.getFirstName());
            clientDto.setLastName(client.getLastName());
            clientDto.setPhone(client.getPhone());
        }

        return OrdreTravailSummaryDto.builder()
                .id(ot.getId())
                .numero(ot.getNumero())
                .statut(ot.getStatut() != null ? ot.getStatut().name() : null)
                .statutLabel(ot.getStatut() != null ? ot.getStatut().getLabel() : null)
                .etape(ot.getStatut() != null ? ot.getStatut().getEtape() : null)
                .ordreReparationId(ordre != null ? ordre.getId() : null)
                .ordreReparationNumero(ordre != null ? ordre.getNumero() : null)
                .proformaId(proforma != null ? proforma.getId() : null)
                .proformaNumero(proforma != null ? proforma.getNumero() : null)
                .vehicule(vehiculeDto)
                .client(clientDto)
                .nombreTechniciens(ot.getLignesTechniciens() != null ? ot.getLignesTechniciens().size() : 0)
                .nombreBonsCommande(ot.getBonsDeCommande() != null ? ot.getBonsDeCommande().size() : 0)
                .nombreBonsSortie(ot.getBonsDeSortie() != null ? ot.getBonsDeSortie().size() : 0)
                .createdAt(ot.getCreatedAt())
                .updatedAt(ot.getUpdatedAt() != null ? ot.getUpdatedAt() : ot.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public OrdreTravailResponseDto modifier(Long id, OrdreTravailRequest request) {
        OrdreTravail ordreTravail = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail introuvable avec l'id : " + id));

        if (request.getClientId() != null) {
            Client client = userService.getClientById(request.getClientId());
            ordreTravail.setClient(client);
        }

        if (request.getVehiculeId() != null) {
            Vehicule vehicule = vehiculeService.getVehiculeById(request.getVehiculeId());
            ordreTravail.setVehicule(vehicule);
        }

        if (request.getProformaId() != null) {
            Proforma proforma = proformaRepository.findById(request.getProformaId())
                    .orElseThrow(() -> new ResourceNotFoundException("Proforma introuvable : " + request.getProformaId()));
            ordreTravail.setProforma(proforma);
        }

        if (request.getOrdreReparationId() != null) {
            OrdreReparation ordreReparation = ordreReparationRepository.findById(request.getOrdreReparationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable : " + request.getOrdreReparationId()));
            ordreTravail.setOrdreReparation(ordreReparation);
        }

        if (request.getTechnicienIds() != null) {
            synchroniserTechniciens(ordreTravail, request.getTechnicienIds());
        }

        if (request.getBonDeCommandeIds() != null) {
            ordreTravail.getBonsDeCommande().clear();
            List<BonDeCommande> bcs = bonDeCommandeRepository.findAllById(request.getBonDeCommandeIds());
            ordreTravail.getBonsDeCommande().addAll(bcs);
        }

        if (request.getStatut() != null) {
            ordreTravail.setStatut(request.getStatut());
        }

        if (request.getBonDeSortieIds() != null) {
            for (BonDeSortie oldBs : new ArrayList<>(ordreTravail.getBonsDeSortie())) {
                ordreTravail.removeBonDeSortie(oldBs);

            }
            if (!request.getBonDeSortieIds().isEmpty()) {
                List<BonDeSortie> bss = bonDeSortieRepository.findAllById(request.getBonDeSortieIds());
                for (BonDeSortie bs : bss) {
                    ordreTravail.addBonDeSortie(bs);
                }
            }
        } else if (request.getBonDeSortieId() != null) {
            for (BonDeSortie oldBs : new ArrayList<>(ordreTravail.getBonsDeSortie())) {
                ordreTravail.removeBonDeSortie(oldBs);
            }
            bonDeSortieRepository.findById(request.getBonDeSortieId()).ifPresent(ordreTravail::addBonDeSortie);
        }

        OrdreTravail updated = ordreTravailRepository.save(ordreTravail);
        return OrdreTravailResponseDto.from(updated);
    }

    @Override
    @Transactional
    public OrdreTravailResponseDto changerStatut(Long id, StatutOrdreTravail statut) {
        if (statut == null) {
            throw new BadRequestException("Le nouveau statut est obligatoire");
        }
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail introuvable avec l'id : " + id));
        ot.setStatut(statut);
        if (statut == StatutOrdreTravail.TERMINE && ot.getDateFinReparation() == null) {
            ot.setDateFinReparation(LocalDateTime.now());
        }
        return OrdreTravailResponseDto.from(ordreTravailRepository.save(ot));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdreTravailResponseDto> getById(Long id) {
        return getEntityById(id).map(OrdreTravailResponseDto::from);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<OrdreTravail> getEntityById(Long id) {
        return ordreTravailRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrdreTravailListResponse> lister(String search, Pageable pageable) {
        Pageable effectivePageable = pageable;
        if (effectivePageable == null || effectivePageable.getSort().isUnsorted() || effectivePageable.getSort().getOrderFor("createdAt") != null) {
            int pageNumber = effectivePageable != null ? effectivePageable.getPageNumber() : 0;
            int pageSize = effectivePageable != null ? effectivePageable.getPageSize() : 10;
            effectivePageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "updatedAt"));
        }
        return ordreTravailRepository.search(search, effectivePageable)
                .map(OrdreTravailListResponse::from);
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail introuvable avec l'id : " + id));
        ordreTravailRepository.delete(ot);
    }

    @Override
    @Transactional
    public OrdreTravailResponseDto assignerTechniciens(Long id, List<Long> technicienIds) {
        OrdreTravail ordreTravail = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail introuvable avec l'id : " + id));

        if (technicienIds != null) {
            for (Long techId : new LinkedHashSet<>(technicienIds)) {
                boolean exists = ordreTravail.getLignesTechniciens().stream()
                        .anyMatch(l -> l.getTechnicien() != null && techId.equals(l.getTechnicien().getId()));
                if (!exists) {
                    Technicien tech = technicienRepository.findById(techId)
                            .orElseThrow(() -> new ResourceNotFoundException("Technicien introuvable : " + techId));
                    LigneTechnicienOrdreTravail ligne = LigneTechnicienOrdreTravail.builder()
                            .ordreTravail(ordreTravail)
                            .technicien(tech)
                            .build();
                    ordreTravail.getLignesTechniciens().add(ligne);
                }
            }
        }

        return OrdreTravailResponseDto.from(ordreTravailRepository.save(ordreTravail));
    }

    @Override
    @Transactional
    public OrdreTravailResponseDto retirerTechnicien(Long id, Long technicienId) {
        OrdreTravail ordreTravail = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail introuvable avec l'id : " + id));
        ordreTravail.getLignesTechniciens().removeIf(l -> l.getTechnicien() != null && technicienId.equals(l.getTechnicien().getId()));
        return OrdreTravailResponseDto.from(ordreTravailRepository.save(ordreTravail));
    }

    // ==================== STEPS WORKFLOW ====================

    @Override
    @Transactional(readOnly = true)
    public StepRecapitulatifResponseDto getStepRecapitulatif(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepRecapitulatifResponseDto dto = new StepRecapitulatifResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        dto.setVehiculeId(ot.getVehicule() != null ? ot.getVehicule().getId() : null);
        dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        dto.setOrdreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null);
        dto.setOrdreReparationNumero(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getNumero() : null);
        dto.setCreatedAt(ot.getCreatedAt());
        dto.setUpdatedAt(ot.getUpdatedAt());

        if (ot.getVehicule() != null) {
            VehiculeHeaderDto vDto = new VehiculeHeaderDto();
            vDto.setId(ot.getVehicule().getId());
            vDto.setImmatriculation(ot.getVehicule().getImmatriculation());
            vDto.setMarque(ot.getVehicule().getMarque());
            vDto.setModele(ot.getVehicule().getModele());
            vDto.setKilometrage(ot.getVehicule().getKilometrage() != null ? ot.getVehicule().getKilometrage().intValue() : 0);
            dto.setVehicule(vDto);
        }

        if (ot.getClient() != null) {
            ClientHeaderDto cDto = new ClientHeaderDto();
            cDto.setId(ot.getClient().getId());
            cDto.setFirstName(ot.getClient().getFirstName());
            cDto.setLastName(ot.getClient().getLastName());
            cDto.setPhone(ot.getClient().getPhone());
            dto.setClient(cDto);
        }

        OrdreReparation ordre = ot.getOrdreReparation();
        Proforma proforma = ot.getProforma();
        if (proforma == null && ordre != null) {
            proforma = ordre.getProforma();
            if (proforma == null) {
                proforma = proformaRepository.findByOrdreReparationId(ordre.getId()).orElse(null);
            }
        }

        List<BonDeCommande> bdcList = getLinkedBonsDeCommande(ot, ordre, proforma);

        if (proforma != null) {
            dto.setProformaId(proforma.getId());
            ProformaSummaryDto proformaDto = new ProformaSummaryDto();
            proformaDto.setId(proforma.getId());
            proformaDto.setNumero(proforma.getNumero());
            proformaDto.setVisibleClient(Boolean.TRUE.equals(proforma.getVisibleClient()));
            proformaDto.setDateCreation(proforma.getDateCreation());
            proformaDto.setStatut(proforma.getStatut() != null ? proforma.getStatut().name() : null);
            proformaDto.setMontantHT(proforma.getMontantHT() != null ? proforma.getMontantHT().doubleValue() : 0.0);
            proformaDto.setMontantTVA(proforma.getMontantTVA() != null ? proforma.getMontantTVA().doubleValue() : 0.0);
            proformaDto.setMontantTTC(proforma.calculerMontantTtcAvecTimbre().doubleValue());
            proformaDto.setMontantTimbre(proforma.getMontantTimbre() != null ? proforma.getMontantTimbre().doubleValue() : 0.0);
            proformaDto.setMontantTotal(proforma.getMontantTotal() != null ? proforma.getMontantTotal().doubleValue() : proformaDto.getMontantTTC());

            if (proforma.getLignesFacturationPieces() != null) {
                proformaDto.setLignesPieces(proforma.getLignesFacturationPieces().stream().map(ligne -> {
                    AdditionalStubs.LigneProformaPieceDto lDto = new AdditionalStubs.LigneProformaPieceDto();
                    lDto.setId(ligne.getId());
                    lDto.setNom(ligne.getIsCustom() != null && ligne.getIsCustom() ? ligne.getDesignationPds() : (ligne.getPiece() != null ? ligne.getPiece().getDesignation() : "Pièce"));
                    lDto.setQuantite(ligne.getQuantite());
                    lDto.setPrix(ligne.getPrix() != null ? ligne.getPrix().doubleValue() : 0.0);
                    lDto.setTauxRemise(ligne.getTauxRemise() != null ? ligne.getTauxRemise().doubleValue() : 0.0);
                    double brut = ligne.getPrix() != null && ligne.getQuantite() != null ? ligne.getPrix().doubleValue() * ligne.getQuantite() : 0.0;
                    lDto.setMontantTotal(Math.round(brut * (1 - lDto.getTauxRemise() / 100.0) * 100.0) / 100.0);
                    lDto.setIsCustom(ligne.getIsCustom());
                    lDto.setType(ligne.getPiece() != null && ligne.getPiece().getType() != null ? ligne.getPiece().getType().name() : null);
                    return lDto;
                }).collect(Collectors.toList()));
            }

            if (proforma.getLignesFacturationMainDoeuvres() != null) {
                proformaDto.setLignesMainDoeuvres(proforma.getLignesFacturationMainDoeuvres().stream().map(ligne -> {
                    AdditionalStubs.LigneProformaMoDto lDto = new AdditionalStubs.LigneProformaMoDto();
                    lDto.setId(ligne.getId());
                    lDto.setNom(ligne.getMainDoeuvre() != null ? ligne.getMainDoeuvre().getDescription() : "Main d'œuvre");
                    lDto.setNbreHeure(ligne.getNbreHeure());
                    lDto.setTarifHoraire(ligne.getTarifHoraire() != null ? ligne.getTarifHoraire().doubleValue() : 0.0);
                    lDto.setTauxRemise(ligne.getTauxRemise() != null ? ligne.getTauxRemise().doubleValue() : 0.0);
                    double brut = ligne.getTarifHoraire() != null && ligne.getNbreHeure() != null ? ligne.getTarifHoraire().doubleValue() * ligne.getNbreHeure() : 0.0;
                    lDto.setMontantTotal(Math.round(brut * (1 - lDto.getTauxRemise() / 100.0) * 100.0) / 100.0);
                    return lDto;
                }).collect(Collectors.toList()));
            }

            dto.setProforma(proformaDto);
            dto.setPiecesManquantesProforma(extractPiecesManquantesProforma(proforma, bdcList));
        }

        return dto;
    }

    private List<BonDeCommande> getLinkedBonsDeCommande(OrdreTravail ot, OrdreReparation ordre, Proforma proforma) {
        List<BonDeCommande> bdcList = new ArrayList<>();
        if (ot != null && ot.getBonsDeCommande() != null) {
            bdcList.addAll(ot.getBonsDeCommande());
        }

        // 1. Ajouter les bons de commande liés à l'ordre de réparation s'il existe
        if (ordre != null) {
            List<BonDeCommande> orBdcList = bonDeCommandeRepository.findByOrdreReparationId(ordre.getId());
            if (orBdcList != null) {
                for (BonDeCommande bc : orBdcList) {
                    if (bdcList.stream().noneMatch(b -> b.getId().equals(bc.getId()))) {
                        bdcList.add(bc);
                    }
                }
            }
        }

        // 2. Ajouter le BDC du proforma s'il existe et n'est pas déjà dans la liste
        if (proforma != null && proforma.getBonDeCommande() != null) {
            BonDeCommande proformaBdc = proforma.getBonDeCommande();
            if (bdcList.stream().noneMatch(b -> b.getId().equals(proformaBdc.getId()))) {
                bdcList.add(proformaBdc);
            }
        }

        return bdcList;
    }

    private List<PieceApprovisionnementDto> extractPiecesManquantesProforma(Proforma proforma, List<BonDeCommande> bdcList) {
        List<PieceApprovisionnementDto> piecesDto = new ArrayList<>();
        if (proforma == null || proforma.getLignesFacturationPieces() == null) {
            return piecesDto;
        }

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

            piecesDto.add(PieceApprovisionnementDto.builder()
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

        return piecesDto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepBonSortieResponseDto getStepBonSortie(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepBonSortieResponseDto dto = new StepBonSortieResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        dto.setVehiculeId(ot.getVehicule() != null ? ot.getVehicule().getId() : null);
        dto.setOrdreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null);
        dto.setClientId(ot.getClient() != null ? ot.getClient().getId() : (ot.getVehicule() != null && ot.getVehicule().getClient() != null ? ot.getVehicule().getClient().getId() : null));
        dto.setOrdreReparationStatut(ot.getOrdreReparation() != null && ot.getOrdreReparation().getStatut() != null ? ot.getOrdreReparation().getStatut().name() : null);

        OrdreReparation ordre = ot.getOrdreReparation();
        Proforma proforma = ot.getProforma();
        if (proforma == null && ordre != null) {
            proforma = ordre.getProforma();
            if (proforma == null) {
                proforma = proformaRepository.findByOrdreReparationId(ordre.getId()).orElse(null);
            }
        }

        List<BonDeSortie> bsList = new ArrayList<>();
        if (ot.getBonsDeSortie() != null) {
            for (BonDeSortie bs : ot.getBonsDeSortie()) {
                if (bsList.stream().noneMatch(b -> b.getId().equals(bs.getId()))) {
                    bsList.add(bs);
                }
            }
        }
        if (ordre != null && ordre.getBonsDeSortie() != null) {
            for (BonDeSortie bs : ordre.getBonsDeSortie()) {
                if (bsList.stream().noneMatch(b -> b.getId().equals(bs.getId()))) {
                    bsList.add(bs);
                }

            }
        }
        if (proforma != null) {
            if (proforma.getBs() != null) {
                BonDeSortie bs = proforma.getBs();
                if (bsList.stream().noneMatch(b -> b.getId().equals(bs.getId()))) {
                    bsList.add(bs);
                }
            }
            if (proforma.getId() != null) {
                for (BonDeSortie bs : bonDeSortieRepository.findByProformaId(proforma.getId())) {
                    if (bsList.stream().noneMatch(b -> b.getId().equals(bs.getId()))) {
                        bsList.add(bs);
                    }
                }
            }
        }

        List<BonDeSortieSummaryDto> bssDtos = new ArrayList<>();
        for (BonDeSortie bs : bsList) {
            BonDeSortieSummaryDto bsDto = new BonDeSortieSummaryDto();
            bsDto.setId(bs.getId());
            bsDto.setStatut(bs.getStatut() != null ? bs.getStatut().name() : null);
            bsDto.setDateSortiePrevue(bs.getDate() != null ? bs.getDate().toLocalDate() : null);
            bssDtos.add(bsDto);
        }
        dto.setBonsDeSortie(bsList.stream().map(sn.oas.facturation.features.bonDeSortie.dto.BonDeSortieListResponse::from).toList());
        int quantiteTotalePieces = 0;
        double montantTotalBons = 0.0;
        for (BonDeSortie bs : bsList) {
            montantTotalBons += bs.getMontantTotal() != null ? bs.getMontantTotal().doubleValue() : 0.0;
            if (bs.getLignesBonDeSortiePieces() != null) {
                quantiteTotalePieces += bs.getLignesBonDeSortiePieces().stream()
                        .filter(l -> l.getType() != sn.oas.facturation.features.bonDeSortie.data.enums.TypeLigneBonDeSortie.MAIN_DOEUVRE)
                        .mapToInt(l -> l.getQuantite() != null ? l.getQuantite() : 0).sum();
            }
        }
        dto.setQuantiteTotalePieces(quantiteTotalePieces);
        dto.setMontantTotalBons(montantTotalBons);
        dto.setHasBonDeSortie(!bssDtos.isEmpty());

        if (proforma != null) {
            dto.setProformaId(proforma.getId());
            dto.setProformaNumero(proforma.getNumero());

            List<StepBonSortieResponseDto.PieceBonDeSortieDto> piecesDto = new ArrayList<>();
            if (proforma.getLignesFacturationPieces() != null) {
                for (LigneFacturationPiece lp : proforma.getLignesFacturationPieces()) {
                    boolean isCustom = Boolean.TRUE.equals(lp.getIsCustom()) || lp.getPiece() == null;
                    if (isCustom || lp.getPiece() == null) {
                        continue;
                    }

                    PieceDetache piece = lp.getPiece();
                    String type = piece.getType() != null ? piece.getType().name() : (piece instanceof PDP ? "PDP" : "");
                    if (!"PDP".equalsIgnoreCase(type) && !(piece instanceof PDP)) {
                        continue;
                    }

                    int qteDemandee = lp.getQuantite() != null ? lp.getQuantite() : 0;
                    double stockMagasin = piece.getStockMagasin() != null ? piece.getStockMagasin() : 0.0;
                    double stockAtelier = piece.getStockAtelier() != null ? piece.getStockAtelier() : 0.0;

                    // Calcul de la quantité déjà sortie via les bons de sortie
                    double qteSortie = 0.0;
                    if (!bsList.isEmpty()) {
                        for (BonDeSortie bs : bsList) {
                            if (bs.getLignesBonDeSortiePieces() != null) {
                                for (LigneBonDeSortiePiece lbs : bs.getLignesBonDeSortiePieces()) {
                                    boolean match = false;
                                    if (lbs.getPiece() != null && piece.getId() != null && piece.getId().equals(lbs.getPiece().getId())) {
                                        match = true;
                                    } else if (piece.getReference() != null && lbs.getPiece() != null && piece.getReference().equalsIgnoreCase(lbs.getPiece().getReference())) {
                                        match = true;
                                    }
                                    if (match && lbs.getQuantite() != null) {
                                        qteSortie += lbs.getQuantite().doubleValue();
                                    }
                                }
                            }
                        }
                    }

                    double quantiteASortir = Math.max(0.0, (double) qteDemandee - qteSortie);

                    Double prixUnitaire = lp.getPrix() != null ? lp.getPrix().doubleValue()
                            : (piece.getPrixUnitaire() != null ? piece.getPrixUnitaire() : 0.0);
                    Double tauxRemise = lp.getTauxRemise() != null ? lp.getTauxRemise().doubleValue() : 0.0;

                    piecesDto.add(StepBonSortieResponseDto.PieceBonDeSortieDto.builder()
                            .ligneId(lp.getId())
                            .pieceId(piece.getId())
                            .reference(piece.getReference())
                            .designation(piece.getDesignation() != null ? piece.getDesignation() : "Pièce")
                            .quantiteDemandee(qteDemandee)
                            .stockMagasin(stockMagasin)
                            .stockAtelier(stockAtelier)
                            .quantiteASortir(quantiteASortir)
                            .quantiteSortie(qteSortie)
                            .prixUnitaire(prixUnitaire)
                            .tauxRemise(tauxRemise)
                            .type(type)
                            .build());
                }
            }
            dto.setPiecesManquantesProforma(piecesDto);

            if (proforma.getLignesFacturationMainDoeuvres() != null) {
                List<StepBonSortieResponseDto.MainDoeuvreBonDeSortieDto> moDtos = new ArrayList<>();
                for (var lm : proforma.getLignesFacturationMainDoeuvres()) {
                    if (lm.getMainDoeuvre() == null) continue;
                    var mo = lm.getMainDoeuvre();
                    moDtos.add(StepBonSortieResponseDto.MainDoeuvreBonDeSortieDto.builder()
                            .ligneId(lm.getId())
                            .mainDoeuvreId(mo.getId())
                            .code("MO-" + mo.getId())
                            .designation(mo.getDescription() != null ? mo.getDescription() : "Main d'œuvre")
                            .nbreHeure(lm.getNbreHeure() != null ? lm.getNbreHeure() : 1)
                            .tarifHoraire(lm.getTarifHoraire() != null ? lm.getTarifHoraire().doubleValue()
                                    : (mo.getPrix() != null ? mo.getPrix() : 0.0))
                            .tauxRemise(lm.getTauxRemise() != null ? lm.getTauxRemise().doubleValue() : 0.0)
                            .build());
                }
                dto.setMainDoeuvresProforma(moDtos);
            }
        } else {
            dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepAssignationResponseDto getStepAssignation(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepAssignationResponseDto dto = new StepAssignationResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        dto.setVehiculeId(ot.getVehicule() != null ? ot.getVehicule().getId() : null);
        dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        dto.setOrdreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null);

        List<TechnicienSummaryDto> techDtos = new ArrayList<>();
        if (ot.getLignesTechniciens() != null) {
            for (LigneTechnicienOrdreTravail ligne : ot.getLignesTechniciens()) {
                if (ligne.getTechnicien() != null) {
                    TechnicienSummaryDto tDto = TechnicienSummaryDto.builder()
                            .id(ligne.getTechnicien().getId())
                            .nom(ligne.getTechnicien().getLastName())
                            .prenom(ligne.getTechnicien().getFirstName())
                            .specialite(ligne.getTechnicien().getSpecialite() != null ? ligne.getTechnicien().getSpecialite().name() : null)
                            .telephone(ligne.getTechnicien().getPhone())
                            .build();
                    techDtos.add(tDto);
                }
            }
        }
        if (techDtos.isEmpty() && ot.getOrdreReparation() != null && ot.getOrdreReparation().getDiagnostic() != null && ot.getOrdreReparation().getDiagnostic().getTechnicien() != null) {
            Technicien tech = ot.getOrdreReparation().getDiagnostic().getTechnicien();
            techDtos.add(TechnicienSummaryDto.builder()
                    .id(tech.getId())
                    .nom(tech.getLastName())
                    .prenom(tech.getFirstName())
                    .specialite(tech.getSpecialite() != null ? tech.getSpecialite().name() : null)
                    .telephone(tech.getPhone())
                    .build());
        }
        dto.setTechniciens(techDtos);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepReparationResponseDto getStepReparation(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepReparationResponseDto dto = new StepReparationResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        dto.setVehiculeId(ot.getVehicule() != null ? ot.getVehicule().getId() : null);
        dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        dto.setOrdreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null);
        dto.setRapportReparation(ot.getRapportReparation());
        dto.setDateFinReparation(ot.getDateFinReparation() != null ? ot.getDateFinReparation().toString() : null);
        dto.setAvertissementsBonsDeSortie(calculerAvertissementsCouvertureBonsDeSortie(ot, StatutOrdreTravail.TERMINE));

        List<TechnicienSummaryDto> techDtos = new ArrayList<>();
        if (ot.getLignesTechniciens() != null) {
            for (LigneTechnicienOrdreTravail ligne : ot.getLignesTechniciens()) {
                if (ligne.getTechnicien() != null) {
                    TechnicienSummaryDto tDto = TechnicienSummaryDto.builder()
                            .id(ligne.getTechnicien().getId())
                            .nom(ligne.getTechnicien().getLastName())
                            .prenom(ligne.getTechnicien().getFirstName())
                            .specialite(ligne.getTechnicien().getSpecialite() != null ? ligne.getTechnicien().getSpecialite().name() : null)
                            .telephone(ligne.getTechnicien().getPhone())
                            .build();
                    techDtos.add(tDto);
                }
            }
        }
        if (techDtos.isEmpty() && ot.getOrdreReparation() != null && ot.getOrdreReparation().getDiagnostic() != null && ot.getOrdreReparation().getDiagnostic().getTechnicien() != null) {
            Technicien tech = ot.getOrdreReparation().getDiagnostic().getTechnicien();
            techDtos.add(TechnicienSummaryDto.builder()
                    .id(tech.getId())
                    .nom(tech.getLastName())
                    .prenom(tech.getFirstName())
                    .specialite(tech.getSpecialite() != null ? tech.getSpecialite().name() : null)
                    .telephone(tech.getPhone())
                    .build());
        }
        dto.setTechniciensReparation(techDtos);
        List<DocumentOrdreTravail> docs = documentOrdreTravailRepository.findByOrdreTravailIdOrderByCreatedAtDesc(ot.getId());
        dto.setDocuments(docs.stream().map(DocumentOrdreTravailResponseDto::from).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional
    public StepAssignationDto updateStepAssignation(Long id, StepAssignationDto dto) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        if (dto.getTechniciensIds() != null) {
            synchroniserTechniciens(ot, dto.getTechniciensIds());
        }

        if (dto.getStatut() != null) {
            ot.setStatut(dto.getStatut());
        }
        ot.setUpdatedAt(LocalDateTime.now());
        ordreTravailRepository.save(ot);
        return dto;
    }

    private void synchroniserTechniciens(OrdreTravail ordreTravail, List<Long> technicienIds) {
        if (ordreTravail.getLignesTechniciens() == null) {
            ordreTravail.setLignesTechniciens(new ArrayList<>());
        }

        if (technicienIds == null) {
            return;
        }

        Set<Long> targetIds = new LinkedHashSet<>(technicienIds);

        // 1. Supprimer les lignes des techniciens qui ne sont plus retenus
        ordreTravail.getLignesTechniciens().removeIf(ligne ->
                ligne.getTechnicien() == null || !targetIds.contains(ligne.getTechnicien().getId())
        );

        // 2. Déterminer les IDs des techniciens déjà présents
        Set<Long> existingIds = ordreTravail.getLignesTechniciens().stream()
                .filter(ligne -> ligne.getTechnicien() != null)
                .map(ligne -> ligne.getTechnicien().getId())
                .collect(Collectors.toSet());

        // 3. Ajouter uniquement les nouveaux techniciens manquants
        for (Long techId : targetIds) {
            if (!existingIds.contains(techId)) {
                Technicien tech = technicienRepository.findById(techId)
                        .orElseThrow(() -> new ResourceNotFoundException("Technicien introuvable : " + techId));
                LigneTechnicienOrdreTravail ligne = LigneTechnicienOrdreTravail.builder()
                        .ordreTravail(ordreTravail)
                        .technicien(tech)
                        .build();
                ordreTravail.getLignesTechniciens().add(ligne);
            }
        }
    }

    @Override
    @Transactional
    public StepReparationDto updateStepReparation(Long id, StepReparationDto dto) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        if (dto.getRapportReparation() != null) {
            ot.setRapportReparation(dto.getRapportReparation());
        }
        if (dto.getDateFinReparation() != null) {
            try {
                ot.setDateFinReparation(LocalDateTime.parse(dto.getDateFinReparation()));
            } catch (Exception e) {
                ot.setDateFinReparation(LocalDateTime.now());
            }
        }
        List<DocumentOrdreTravailRequestDto> docsToSave = new ArrayList<>();
        if (dto.getDocuments() != null && !dto.getDocuments().isEmpty()) {
            docsToSave.addAll(dto.getDocuments());
        }
        if (dto.getPiecesJointes() != null && !dto.getPiecesJointes().isEmpty()) {
            docsToSave.addAll(dto.getPiecesJointes());
        }
        for (DocumentOrdreTravailRequestDto docReq : docsToSave) {
            if (docReq != null && docReq.getUrl() != null && !docReq.getUrl().isBlank()) {
                addDocumentToOt(ot, docReq);
            }
        }
        dto.setAvertissementsBonsDeSortie(calculerAvertissementsCouvertureBonsDeSortie(
                ot, dto.getStatut() != null ? dto.getStatut() : StatutOrdreTravail.TERMINE));
        if (dto.getStatut() != null) {
            ot.setStatut(dto.getStatut());
        }
        ot.setUpdatedAt(LocalDateTime.now());
        ordreTravailRepository.save(ot);

        List<DocumentOrdreTravail> updatedDocs = documentOrdreTravailRepository.findByOrdreTravailIdOrderByCreatedAtDesc(ot.getId());
        List<DocumentOrdreTravailRequestDto> updatedReqs = updatedDocs.stream().map(d -> DocumentOrdreTravailRequestDto.builder()
                .nomFichier(d.getNomFichier())
                .url(d.getUrl())
                .type(d.getType())
                .taille(d.getTaille())
                .remarque(d.getRemarque())
                .publicId(d.getPublicId())
                .etape(d.getEtape())
                .technicienId(d.getTechnicien() != null ? d.getTechnicien().getId() : null)
                .build()).collect(Collectors.toList());
        dto.setDocuments(updatedReqs);
        dto.setPiecesJointes(updatedReqs);
        return dto;
    }

    /**
     * Signale les lignes demandées qui ne sont pas couvertes par des bons de sortie validés.
     * Cet écart est informatif et ne bloque pas la fin de réparation.
     * Les quantités sont agrégées en base au lieu de charger les collections d'entités.
     */
    private List<String> calculerAvertissementsCouvertureBonsDeSortie(OrdreTravail ordreTravail, StatutOrdreTravail nouveauStatut) {
        if (nouveauStatut != StatutOrdreTravail.TERMINE
                || ordreTravail.getOrdreReparation() == null
                || ordreTravail.getOrdreReparation().getId() == null) {
            return List.of();
        }

        Long ordreReparationId = ordreTravail.getOrdreReparation().getId();
        Map<String, Long> quantitesDemandees = new LinkedHashMap<>();
        Map<String, String> libelles = new HashMap<>();

        for (var ligne : ordreReparationRepository.sommerQuantitesPiecesDemandees(ordreReparationId)) {
            String cle = clePiece(ligne.getPieceId(), ligne.getDesignation());
            quantitesDemandees.merge(cle, quantite(ligne.getQuantiteTotale()), Long::sum);
            libelles.putIfAbsent(cle, "Pièce — " + libelle(ligne.getDesignation(), "ligne personnalisée"));
        }
        for (var ligne : ordreReparationRepository.sommerQuantitesMainDoeuvreDemandees(ordreReparationId)) {
            String cle = cleMainDoeuvre(ligne.getMainDoeuvreId());
            quantitesDemandees.merge(cle, quantite(ligne.getQuantiteTotale()), Long::sum);
            libelles.putIfAbsent(cle, "Main-d'œuvre — " + libelle(ligne.getDesignation(), "ligne sans désignation"));
        }

        if (quantitesDemandees.isEmpty()) {
            return List.of();
        }

        Map<String, Long> quantitesValidees = new HashMap<>();
        for (var ligne : bonDeSortieRepository.sommerQuantitesPiecesValideesParOrdreReparation(ordreReparationId,
                StatutBon.VALIDE, TypeLigneBonDeSortie.PIECE)) {
            quantitesValidees.merge(clePiece(ligne.getPieceId(), ligne.getDesignation()),
                    quantite(ligne.getQuantiteTotale()), Long::sum);
        }
        for (var ligne : bonDeSortieRepository.sommerQuantitesMainDoeuvreValideesParOrdreReparation(ordreReparationId,
                StatutBon.VALIDE)) {
            quantitesValidees.merge(cleMainDoeuvre(ligne.getMainDoeuvreId()),
                    quantite(ligne.getQuantiteTotale()), Long::sum);
        }
        for (var ligne : bonDeSortieRepository.sommerQuantitesMainDoeuvreLegacyValideesParOrdreReparation(ordreReparationId,
                StatutBon.VALIDE, TypeLigneBonDeSortie.MAIN_DOEUVRE)) {
            quantitesValidees.merge(cleMainDoeuvre(ligne.getMainDoeuvreId()),
                    quantite(ligne.getQuantiteTotale()), Long::sum);
        }

        List<String> manquants = new ArrayList<>();
        quantitesDemandees.forEach((cle, demandee) -> {
            long validee = quantitesValidees.getOrDefault(cle, 0L);
            if (validee < demandee) {
                manquants.add(libelles.get(cle) + " : demandé " + demandee + ", sur les bons validés " + validee
                        + ", reste " + (demandee - validee));
            }
        });

        return manquants;
    }

    private String clePiece(Long pieceId, String designation) {
        if (pieceId != null) {
            return "PIECE:" + pieceId;
        }
        if (designation != null && !designation.isBlank()) {
            return "PDS:" + designation.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
        }
        return "PDS_SANS_DESIGNATION";
    }

    private String cleMainDoeuvre(Long mainDoeuvreId) {
        return "MO:" + mainDoeuvreId;
    }

    private long quantite(Long quantite) {
        return quantite != null ? quantite : 0L;
    }

    private String libelle(String valeur, String fallback) {
        return valeur == null || valeur.isBlank() ? fallback : valeur.trim();
    }

    // ==================== DOCUMENTS / PIECES JOINTES ====================

    @Override
    @Transactional
    public DocumentOrdreTravailResponseDto addDocument(Long ordreTravailId, DocumentOrdreTravailRequestDto request) {
        OrdreTravail ot = getEntityById(ordreTravailId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + ordreTravailId));

        if (request == null || request.getUrl() == null || request.getUrl().isBlank()) {
            throw new BadRequestException("L'URL du document est obligatoire");
        }

        DocumentOrdreTravail saved = addDocumentToOt(ot, request);
        return DocumentOrdreTravailResponseDto.from(saved);
    }

    private DocumentOrdreTravail addDocumentToOt(OrdreTravail ot, DocumentOrdreTravailRequestDto req) {
        TypeDocumentOt docType = req.getType();
        if (docType == null) {
            docType = detectTypeFromUrlOrName(req.getUrl(), req.getNomFichier());
        }

        Technicien tech = null;
        if (req.getTechnicienId() != null) {
            tech = technicienRepository.findById(req.getTechnicienId()).orElse(null);
        }

        DocumentOrdreTravail doc = DocumentOrdreTravail.builder()
                .ordreTravail(ot)
                .nomFichier(req.getNomFichier())
                .url(req.getUrl().trim())
                .type(docType)
                .taille(req.getTaille())
                .remarque(req.getRemarque())
                .publicId(req.getPublicId())
                .etape(req.getEtape() != null ? req.getEtape() : 4)
                .technicien(tech)
                .build();

        return documentOrdreTravailRepository.save(doc);
    }

    @Override
    @Transactional
    public DocumentOrdreTravailResponseDto uploadDocument(Long ordreTravailId, MultipartFile file, TypeDocumentOt type, String remarque) {
        OrdreTravail ot = getEntityById(ordreTravailId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + ordreTravailId));

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Le fichier à uploader est obligatoire");
        }

        try {
            String originalFilename = file.getOriginalFilename();
            TypeDocumentOt resolvedType = type != null ? type : detectTypeFromUrlOrName(originalFilename, originalFilename);

            String folder = "oas/ordres-travail/reparation";
            Map<String, Object> uploadParams = new HashMap<>();
            uploadParams.put("folder", folder);
            uploadParams.put("resource_type", "auto");

            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadParams);
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            DocumentOrdreTravail doc = DocumentOrdreTravail.builder()
                    .ordreTravail(ot)
                    .nomFichier(originalFilename)
                    .url(secureUrl)
                    .type(resolvedType)
                    .taille(file.getSize())
                    .remarque(remarque)
                    .publicId(publicId)
                    .etape(4)
                    .build();

            DocumentOrdreTravail saved = documentOrdreTravailRepository.save(doc);
            return DocumentOrdreTravailResponseDto.from(saved);
        } catch (Exception e) {
            log.error("Erreur lors de l'upload du document pour l'OT {}: {}", ordreTravailId, e.getMessage(), e);
            throw new RuntimeException("Échec de l'upload du document : " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentOrdreTravailResponseDto> getDocuments(Long ordreTravailId, TypeDocumentOt type, Integer etape) {
        if (!ordreTravailRepository.existsById(ordreTravailId)) {
            throw new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + ordreTravailId);
        }

        List<DocumentOrdreTravail> docs;
        if (etape != null && type != null) {
            docs = documentOrdreTravailRepository.findByOrdreTravailIdAndEtapeAndTypeOrderByCreatedAtDesc(ordreTravailId, etape, type);
        } else if (etape != null) {
            docs = documentOrdreTravailRepository.findByOrdreTravailIdAndEtapeOrderByCreatedAtDesc(ordreTravailId, etape);
        } else if (type != null) {
            docs = documentOrdreTravailRepository.findByOrdreTravailIdAndTypeOrderByCreatedAtDesc(ordreTravailId, type);
        } else {
            docs = documentOrdreTravailRepository.findByOrdreTravailIdOrderByCreatedAtDesc(ordreTravailId);
        }

        return docs.stream().map(DocumentOrdreTravailResponseDto::from).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteDocument(Long documentId) {
        DocumentOrdreTravail doc = documentOrdreTravailRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document non trouvé avec l'id : " + documentId));

        if (doc.getPublicId() != null && !doc.getPublicId().isBlank()) {
            try {
                cloudinary.uploader().destroy(doc.getPublicId(), com.cloudinary.utils.ObjectUtils.emptyMap());
            } catch (Exception e) {
                log.warn("Impossible de supprimer le fichier Cloudinary publicId {}: {}", doc.getPublicId(), e.getMessage());
            }
        }

        documentOrdreTravailRepository.delete(doc);
    }

    private TypeDocumentOt detectTypeFromUrlOrName(String url, String filename) {
        String target = filename != null && !filename.isBlank() ? filename : url;
        if (target == null) return TypeDocumentOt.DOCUMENT;
        String lower = target.toLowerCase(Locale.ROOT);
        if (lower.matches(".*\\.(jpg|jpeg|png|gif|webp|bmp|svg)(\\?.*)?$")) {
            return TypeDocumentOt.PHOTO;
        }
        if (lower.matches(".*\\.(pdf)(\\?.*)?$")) {
            return TypeDocumentOt.PDF;
        }
        if (lower.matches(".*\\.(doc|docx|odt|rtf|txt|xls|xlsx|csv)(\\?.*)?$")) {
            return TypeDocumentOt.DOCUMENT;
        }
        return TypeDocumentOt.AUTRE;
    }
}
