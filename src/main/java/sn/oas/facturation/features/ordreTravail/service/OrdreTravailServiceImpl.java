package sn.oas.facturation.features.ordreTravail.service;

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
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.fournisseur.data.entity.Fournisseur;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto.BonCommandeSummaryDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto.PieceApprovisionnementDto;
import sn.oas.facturation.features.ordreReparation.dto.responses.StepApprovisionnementResponseDto.PieceBonCommandeDto;
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
import java.util.List;
import java.util.Optional;
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
                .bonDeSortie(null)
                .lignesTechniciens(new ArrayList<>())
                .build();

        // Ajout des techniciens
        if (request.getTechnicienIds() != null && !request.getTechnicienIds().isEmpty()) {
            for (Long techId : request.getTechnicienIds()) {
                Technicien tech = technicienRepository.findById(techId)
                        .orElseThrow(() -> new ResourceNotFoundException("Technicien introuvable : " + techId));
                LigneTechnicienOrdreTravail ligne = LigneTechnicienOrdreTravail.builder()
                        .ordreTravail(ordreTravail)
                        .technicien(tech)
                        .build();
                ordreTravail.getLignesTechniciens().add(ligne);
            }
        }

        // Ajout des bons de commande
        if (request.getBonDeCommandeIds() != null && !request.getBonDeCommandeIds().isEmpty()) {
            List<BonDeCommande> bcs = bonDeCommandeRepository.findAllById(request.getBonDeCommandeIds());
            ordreTravail.getBonsDeCommande().addAll(bcs);
        }

        // Ajout du bon de sortie
        if (request.getBonDeSortieId() != null) {
            bonDeSortieRepository.findById(request.getBonDeSortieId()).ifPresent(ordreTravail::setBonDeSortie);
        } else if (request.getBonDeSortieIds() != null && !request.getBonDeSortieIds().isEmpty()) {
            bonDeSortieRepository.findById(request.getBonDeSortieIds().get(0)).ifPresent(ordreTravail::setBonDeSortie);
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

        OrdreReparation or = ordreReparationRepository.findById(ordreReparationId)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de réparation introuvable : " + ordreReparationId));

        // Vérifier si un ordre de travail existe déjà pour cet ordre de réparation
        Optional<OrdreTravail> existing = ordreTravailRepository.findByOrdreReparationId(ordreReparationId);
        if (existing.isPresent()) {
            return OrdreTravailResponseDto.from(existing.get());
        }

        OrdreTravailRequest request = OrdreTravailRequest.builder()
                .ordreReparationId(or.getId())
                .proformaId(or.getProforma() != null ? or.getProforma().getId() : null)
                .clientId(or.getClient() != null ? or.getClient().getId() : (or.getVehicule() != null && or.getVehicule().getClient() != null ? or.getVehicule().getClient().getId() : null))
                .vehiculeId(or.getVehicule() != null ? or.getVehicule().getId() : null)
                .bonDeCommandeIds(or.getBonDeCommandes() != null ? or.getBonDeCommandes().stream().map(BonDeCommande::getId).toList() : new ArrayList<>())
                .bonDeSortieId(or.getBonDeSortie() != null ? or.getBonDeSortie().getId() : null)
                .bonDeSortieIds(or.getBonDeSortie() != null ? List.of(or.getBonDeSortie().getId()) : new ArrayList<>())
                .technicienIds(or.getTechniciensReparation() != null ? or.getTechniciensReparation().stream().map(Technicien::getId).toList() : new ArrayList<>())
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
                .nombreBonsSortie(ot.getBonDeSortie() != null ? 1 : 0)
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
            ordreTravail.getLignesTechniciens().clear();
            for (Long techId : request.getTechnicienIds()) {
                Technicien tech = technicienRepository.findById(techId)
                        .orElseThrow(() -> new ResourceNotFoundException("Technicien introuvable : " + techId));
                LigneTechnicienOrdreTravail ligne = LigneTechnicienOrdreTravail.builder()
                        .ordreTravail(ordreTravail)
                        .technicien(tech)
                        .build();
                ordreTravail.getLignesTechniciens().add(ligne);
            }
        }

        if (request.getBonDeCommandeIds() != null) {
            ordreTravail.getBonsDeCommande().clear();
            List<BonDeCommande> bcs = bonDeCommandeRepository.findAllById(request.getBonDeCommandeIds());
            ordreTravail.getBonsDeCommande().addAll(bcs);
        }

        if (request.getStatut() != null) {
            ordreTravail.setStatut(request.getStatut());
        }

        if (request.getBonDeSortieId() != null) {
            ordreTravail.setBonDeSortie(bonDeSortieRepository.findById(request.getBonDeSortieId()).orElse(null));
        } else if (request.getBonDeSortieIds() != null) {
            if (request.getBonDeSortieIds().isEmpty()) {
                ordreTravail.setBonDeSortie(null);
            } else {
                bonDeSortieRepository.findById(request.getBonDeSortieIds().get(0)).ifPresent(ordreTravail::setBonDeSortie);
            }
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
            for (Long techId : technicienIds) {
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

        Proforma proforma = ot.getProforma();
        if (proforma == null && ot.getOrdreReparation() != null) {
            proforma = ot.getOrdreReparation().getProforma();
        }

        if (proforma != null) {
            ProformaSummaryDto proformaDto = new ProformaSummaryDto();
            proformaDto.setId(proforma.getId());
            proformaDto.setNumero(proforma.getNumero());
            proformaDto.setVisibleClient(Boolean.TRUE.equals(proforma.getVisibleClient()));
            proformaDto.setDateCreation(proforma.getDateCreation());
            proformaDto.setStatut(proforma.getStatut() != null ? proforma.getStatut().name() : null);
            proformaDto.setMontantHT(proforma.getMontantHT() != null ? proforma.getMontantHT().doubleValue() : 0.0);
            proformaDto.setMontantTVA(proforma.getMontantTVA() != null ? proforma.getMontantTVA().doubleValue() : 0.0);
            proformaDto.setMontantTTC(proforma.getMontantTTC() != null ? proforma.getMontantTTC().doubleValue() : 0.0);

            if (proforma.getLignesFacturationPieces() != null) {
                proformaDto.setLignesPieces(proforma.getLignesFacturationPieces().stream().map(ligne -> {
                    AdditionalStubs.LigneProformaPieceDto lDto = new AdditionalStubs.LigneProformaPieceDto();
                    lDto.setId(ligne.getId());
                    lDto.setNom(ligne.getIsCustom() != null && ligne.getIsCustom() ? ligne.getDesignationPds() : (ligne.getPiece() != null ? ligne.getPiece().getDesignation() : "Pièce"));
                    lDto.setQuantite(ligne.getQuantite());
                    lDto.setPrix(ligne.getPrix() != null ? ligne.getPrix().doubleValue() : 0.0);
                    lDto.setMontantTotal(ligne.getPrix() != null && ligne.getQuantite() != null ? ligne.getPrix().doubleValue() * ligne.getQuantite() : 0.0);
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
                    lDto.setMontantTotal(ligne.getTarifHoraire() != null && ligne.getNbreHeure() != null ? ligne.getTarifHoraire().doubleValue() * ligne.getNbreHeure() : 0.0);
                    return lDto;
                }).collect(Collectors.toList()));
            }

            dto.setProforma(proformaDto);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepApprovisionnementResponseDto getStepApprovisionnement(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepApprovisionnementResponseDto dto = new StepApprovisionnementResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        if (ot.getVehicule() != null) {
            dto.setVehiculeId(ot.getVehicule().getId());
        }

        OrdreReparation ordre = ot.getOrdreReparation();
        if (ordre != null) {
            dto.setOrdreReparationId(ordre.getId());
        }

        Proforma proforma = ot.getProforma();
        if (proforma == null && ordre != null) {
            proforma = ordre.getProforma();
            if (proforma == null) {
                proforma = proformaRepository.findByOrdreReparationId(ordre.getId()).orElse(null);
            }
        }

        // Récupération des bons de commande liés
        List<BonDeCommande> bdcList = new ArrayList<>();
        if (ot.getBonsDeCommande() != null) {
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

        List<BonCommandeSummaryDto> bdcDtos = new ArrayList<>();
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

                List<PieceBonCommandeDto> piecesBdc = new ArrayList<>();
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

                        piecesBdc.add(PieceBonCommandeDto.builder()
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

                bdcDtos.add(BonCommandeSummaryDto.builder()
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

            List<PieceApprovisionnementDto> piecesDto = new ArrayList<>();
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
            }
            dto.setPiecesManquantesProforma(piecesDto);
        }

        return dto;
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

        OrdreReparation ordre = ot.getOrdreReparation();
        Proforma proforma = ot.getProforma();
        if (proforma == null && ordre != null) {
            proforma = ordre.getProforma();
            if (proforma == null) {
                proforma = proformaRepository.findByOrdreReparationId(ordre.getId()).orElse(null);
            }
        }

        List<BonDeSortie> bsList = new ArrayList<>();
        if (ot.getBonDeSortie() != null) {
            bsList.add(ot.getBonDeSortie());
        }
        if (ordre != null && ordre.getBonDeSortie() != null) {
            BonDeSortie bs = ordre.getBonDeSortie();
            if (bsList.stream().noneMatch(b -> b.getId().equals(bs.getId()))) {
                bsList.add(bs);
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
                bonDeSortieRepository.findByProformaId(proforma.getId()).ifPresent(bs -> {
                    if (bsList.stream().noneMatch(b -> b.getId().equals(bs.getId()))) {
                        bsList.add(bs);
                    }
                });
            }
        }

        if (!bsList.isEmpty()) {
            BonDeSortie bs = bsList.get(0);
            BonDeSortieSummaryDto bsDto = new BonDeSortieSummaryDto();
            bsDto.setId(bs.getId());
            bsDto.setStatut(bs.getStatut() != null ? bs.getStatut().name() : null);
            bsDto.setDateSortiePrevue(bs.getDate() != null ? bs.getDate().toLocalDate() : null);
            dto.setBonDeSortie(bsDto);
            dto.setHasBonDeSortie(true);
        } else {
            dto.setHasBonDeSortie(false);
        }

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

                    // Prix unitaire issu du proforma associé
                    Double prixUnitaire = lp.getPrix() != null ? lp.getPrix().doubleValue()
                            : (piece.getPrixUnitaire() != null ? piece.getPrixUnitaire() : 0.0);

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
                            .build());
                }
            }
            dto.setPiecesManquantesProforma(piecesDto);
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
        dto.setTechniciensReparation(techDtos);
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepPaiementResponseDto getStepPaiement(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepPaiementResponseDto dto = new StepPaiementResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        dto.setVehiculeId(ot.getVehicule() != null ? ot.getVehicule().getId() : null);
        dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        dto.setOrdreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null);

        Facture facture = null;
        if (ot.getOrdreReparation() != null) {
            facture = factureRepository.findFirstByOrdreReparationId(ot.getOrdreReparation().getId()).orElse(null);
        }

        if (facture != null) {
            FactureSummaryDto fDto = new FactureSummaryDto();
            fDto.setId(facture.getId());
            fDto.setStatut(facture.getStatut() != null ? facture.getStatut().name() : null);
            fDto.setMontantRestant(facture.getResteAPayer() != null ? facture.getResteAPayer().doubleValue() : 0.0);
            dto.setFacture(fDto);
        }

        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepPretALivrerResponseDto getStepPretALivrer(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepPretALivrerResponseDto dto = new StepPretALivrerResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        dto.setVehiculeId(ot.getVehicule() != null ? ot.getVehicule().getId() : null);
        dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        dto.setOrdreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null);
        dto.setRemarquesPreparation(ot.getRemarquesPreparation());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepLivraisonResponseDto getStepLivraison(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepLivraisonResponseDto dto = new StepLivraisonResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : null);
        dto.setVehiculeId(ot.getVehicule() != null ? ot.getVehicule().getId() : null);
        dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        dto.setOrdreReparationId(ot.getOrdreReparation() != null ? ot.getOrdreReparation().getId() : null);
        dto.setDateSortie(ot.getDateSortie() != null ? ot.getDateSortie().toString() : null);
        dto.setKilometrageSortie(ot.getKilometrageSortie());
        dto.setRemarquesClient(ot.getRemarquesClient());
        return dto;
    }

    @Override
    @Transactional
    public StepAssignationDto updateStepAssignation(Long id, StepAssignationDto dto) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        if (dto.getTechniciensIds() != null) {
            ot.getLignesTechniciens().clear();
            for (Long tId : dto.getTechniciensIds()) {
                Technicien t = technicienRepository.findById(tId)
                        .orElseThrow(() -> new ResourceNotFoundException("Technicien non trouvé avec l'id : " + tId));
                LigneTechnicienOrdreTravail ligne = LigneTechnicienOrdreTravail.builder()
                        .ordreTravail(ot)
                        .technicien(t)
                        .build();
                ot.getLignesTechniciens().add(ligne);
            }
        }

        if (dto.getStatut() != null) {
            ot.setStatut(dto.getStatut());
        }
        ot.setUpdatedAt(LocalDateTime.now());
        ordreTravailRepository.save(ot);
        return dto;
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
        if (dto.getStatut() != null) {
            ot.setStatut(dto.getStatut());
        }
        ot.setUpdatedAt(LocalDateTime.now());
        ordreTravailRepository.save(ot);
        return dto;
    }

    @Override
    @Transactional
    public StepPretALivrerDto updateStepPretALivrer(Long id, StepPretALivrerDto dto) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        if (dto.getRemarquesPreparation() != null) {
            ot.setRemarquesPreparation(dto.getRemarquesPreparation());
        }
        if (dto.getStatut() != null) {
            ot.setStatut(dto.getStatut());
        }
        ot.setUpdatedAt(LocalDateTime.now());
        ordreTravailRepository.save(ot);
        return dto;
    }

    @Override
    @Transactional
    public StepLivraisonDto updateStepLivraison(Long id, StepLivraisonDto dto) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        if (dto.getDateSortie() != null) {
            try {
                ot.setDateSortie(LocalDateTime.parse(dto.getDateSortie()));
            } catch (Exception e) {
                ot.setDateSortie(LocalDateTime.now());
            }
        }
        if (dto.getKilometrageSortie() != null) {
            ot.setKilometrageSortie(dto.getKilometrageSortie());
        }
        if (dto.getRemarquesClient() != null) {
            ot.setRemarquesClient(dto.getRemarquesClient());
        }
        if (dto.getStatut() != null) {
            ot.setStatut(dto.getStatut());
        }
        ot.setUpdatedAt(LocalDateTime.now());
        ordreTravailRepository.save(ot);
        return dto;
    }
}
