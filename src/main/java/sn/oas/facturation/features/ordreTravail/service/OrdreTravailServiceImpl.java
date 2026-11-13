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
import sn.oas.facturation.features.bonDeLivraison.data.entity.BonDeLivraison;
import sn.oas.facturation.features.bonDeLivraison.repository.BonDeLivraisonRepository;
import sn.oas.facturation.features.bonDeLivraison.service.BonDeLivraisonService;
import sn.oas.facturation.features.bonDeSortie.data.entity.BonDeSortie;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortiePiece;
import sn.oas.facturation.features.bonDeSortie.data.entity.LigneBonDeSortieMainDoeuvre;
import sn.oas.facturation.features.bonDeSortie.data.enums.StatutBon;
import sn.oas.facturation.features.bonDeSortie.repository.BonDeSortieRepository;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationMainDoeuvre;
import sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece;
import sn.oas.facturation.features.main_doeuvre.data.entity.MainDoeuvre;
import sn.oas.facturation.features.piecedetache.data.entity.PDP;
import sn.oas.facturation.features.piecedetache.data.entity.PDG;
import sn.oas.facturation.features.piecedetache.data.entity.PieceDetache;
import sn.oas.facturation.features.ordreReparation.data.enums.StatutOrdreReparation;
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
import sn.oas.facturation.features.proforma.dto.ProformaDetailsResponseDto;
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
    private final BonDeLivraisonService bonDeLivraisonService;
    private final BonDeLivraisonRepository bonDeLivraisonRepository;

    @Override
    @Transactional
    public OrdreTravailResponseDto creer(OrdreTravailRequest request) {
        if (request == null) {
            throw new BadRequestException("Les données de l'ordre de travail sont obligatoires");
        }
        if ((request.getBonDeSortieIds() != null && !request.getBonDeSortieIds().isEmpty())
                || request.getBonDeSortieId() != null) {
            throw new IllegalArgumentException("Le bon de sortie est créé depuis son OT et ne peut pas être pré-rattaché.");
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
                .lignesTechniciens(new ArrayList<>())
                .build();

        // Ajout des techniciens
        if (request.getTechnicienIds() != null && !request.getTechnicienIds().isEmpty()) {
            synchroniserTechniciens(ordreTravail, request.getTechnicienIds());
        } else if (ordreReparation != null) {
            for (Technicien tech : techniciensDiagnostic(ordreReparation)) {
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

        // Ajout du bon de sortie (un seul BS par OT)
        if (request.getBonDeSortieIds() != null && !request.getBonDeSortieIds().isEmpty()) {
            if (request.getBonDeSortieIds().size() > 1) {
                throw new BadRequestException("Un ordre de travail ne peut être associé qu'à un seul bon de sortie.");
            }
            bonDeSortieRepository.findById(request.getBonDeSortieIds().get(0)).ifPresent(ordreTravail::addBonDeSortie);
        } else if (request.getBonDeSortieId() != null) {
            bonDeSortieRepository.findById(request.getBonDeSortieId()).ifPresent(ordreTravail::addBonDeSortie);
        }

        OrdreTravail saved = ordreTravailRepository.save(ordreTravail);
        if (ordreReparation != null && (ordreReparation.getStatut() == null
                || ordreReparation.getStatut().getEtape() < StatutOrdreReparation.ORDRE_DE_TRAVAIL.getEtape())) {
            ordreReparation.setStatut(StatutOrdreReparation.ORDRE_DE_TRAVAIL);
            ordreReparationRepository.save(ordreReparation);
        }
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
            if (or.getStatut() == null
                    || or.getStatut().getEtape() < StatutOrdreReparation.ORDRE_DE_TRAVAIL.getEtape()) {
                or.setStatut(StatutOrdreReparation.ORDRE_DE_TRAVAIL);
                ordreReparationRepository.save(or);
            }
            return OrdreTravailResponseDto.from(existing.get());
        }

        List<Long> techIds = new ArrayList<>();
        techIds.addAll(techniciensDiagnostic(or).stream().map(Technicien::getId).toList());
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

        if (request.getBonDeSortieIds() != null || request.getBonDeSortieId() != null) {
            List<Long> bonsDemandes = request.getBonDeSortieIds() != null
                    ? request.getBonDeSortieIds()
                    : List.of(request.getBonDeSortieId());
            List<Long> bonsActuels = ordreTravail.getBonsDeSortie() == null ? List.of()
                    : ordreTravail.getBonsDeSortie().stream().map(BonDeSortie::getId).distinct().toList();
            if (bonsDemandes.size() > 1 || !new java.util.HashSet<>(bonsDemandes).equals(new java.util.HashSet<>(bonsActuels))) {
                throw new IllegalArgumentException("Le bon de sortie est géré depuis l'ordre de travail et ne peut pas être remplacé ou détaché.");
            }
        }

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
            if (request.getBonDeSortieIds().size() > 1) {
                throw new BadRequestException("Un ordre de travail ne peut être associé qu'à un seul bon de sortie.");
            }
            for (BonDeSortie oldBs : new ArrayList<>(ordreTravail.getBonsDeSortie())) {
                ordreTravail.removeBonDeSortie(oldBs);
            }
            if (!request.getBonDeSortieIds().isEmpty()) {
                bonDeSortieRepository.findById(request.getBonDeSortieIds().get(0)).ifPresent(ordreTravail::addBonDeSortie);
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
        if (statut == StatutOrdreTravail.TERMINE) {
            traiterFinReparationEtGenererBL(ot);
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
                proformaDto.setLignesPieces(sn.oas.facturation.shared.OrdreAffichageLignes.plusRecentesEnPremier(
                        proforma.getLignesFacturationPieces(), sn.oas.facturation.features.facturation.data.entity.LigneFacturationPiece::getId)
                        .stream().map(ligne -> {
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
                    lDto.setDateHeureSaisie(ligne.getDateHeureSaisie());
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
                    lDto.setDateHeureSaisie(ligne.getDateHeureSaisie());
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

        BonDeSortie bs = ot.getBonDeSortie();
        if (bs == null && ordre != null) {
            bs = ordre.getBonDeSortie();
        }
        if (bs == null && proforma != null && proforma.getBs() != null) {
            bs = proforma.getBs();
        }
        if (bs == null && proforma != null && proforma.getId() != null) {
            var bsFromRepo = bonDeSortieRepository.findByProformaId(proforma.getId());
            if (!bsFromRepo.isEmpty()) {
                bs = bsFromRepo.get(0);
            }
        }
        if (bs == null && !bsList.isEmpty()) {
            bs = bsList.get(0);
        }

        // 1. Toujours charger les lignes du Proforma (pièces et MO) de manière séparée et immuable
        if (proforma != null) {
            dto.setProformaId(proforma.getId());
            dto.setProformaNumero(proforma.getNumero());

            ProformaDetailsResponseDto proformaDetails = ProformaDetailsResponseDto.from(proforma);
            if (proformaDetails != null) {
                dto.setLignesPiecesProforma(proformaDetails.lignesPieces() != null ? new ArrayList<>(proformaDetails.lignesPieces()) : new ArrayList<>());
                dto.setLignesMainDoeuvresProforma(proformaDetails.lignesMainDoeuvres() != null ? new ArrayList<>(proformaDetails.lignesMainDoeuvres()) : new ArrayList<>());
            }
        } else {
            dto.setProformaId(ot.getProforma() != null ? ot.getProforma().getId() : null);
        }

        // 2. Charger les lignes du Bon de Sortie (pièces et MO) si un BS existe déjà
        if (bs != null) {
            BonDeSortieSummaryDto bsDto = new BonDeSortieSummaryDto();
            bsDto.setId(bs.getId());
            bsDto.setStatut(bs.getStatut() != null ? bs.getStatut().name() : null);
            bsDto.setDateSortiePrevue(bs.getDate() != null ? bs.getDate().toLocalDate() : null);
            dto.setBonDeSortie(bsDto);
            dto.setHasBonDeSortie(true);

            dto.setMontantTotalBons(bs.getMontantTotal() != null ? bs.getMontantTotal().doubleValue() : 0.0);
            int quantiteTotalePieces = 0;
            if (bs.getLignesBonDeSortiePieces() != null) {
                quantiteTotalePieces = bs.getLignesBonDeSortiePieces().stream()
                        .mapToInt(l -> l.getQuantite() != null ? l.getQuantite() : 0).sum();
            }
            dto.setQuantiteTotalePieces(quantiteTotalePieces);

            // Charger les lignes directement depuis le bon de sortie existant
            List<ProformaDetailsResponseDto.LignePieceResponseDto> piecesBsDto = new ArrayList<>();
            if (bs.getLignesBonDeSortiePieces() != null) {
                for (LigneBonDeSortiePiece lp : bs.getLignesBonDeSortiePieces()) {
                    PieceDetache piece = lp.getPiece();
                    String pieceType = piece != null && piece.getType() != null ? piece.getType().name()
                            : (piece instanceof PDP ? "PDP" : (piece instanceof PDG ? "PDG" : "PDS"));
                    boolean isCustom = "PDS".equalsIgnoreCase(pieceType) || piece == null;
                    Double prixCatalogue = piece != null && piece.getPrixUnitaire() != null ? piece.getPrixUnitaire() : null;
                    Double prixUnitaire = lp.getPrix() != null ? lp.getPrix().doubleValue() : (prixCatalogue != null ? prixCatalogue : 0.0);
                    Double tauxRemise = lp.getTauxRemise() != null ? lp.getTauxRemise().doubleValue() : 0.0;
                    int qte = lp.getQuantite() != null ? lp.getQuantite() : 1;
                    double brut = prixUnitaire * qte;
                    double total = lp.getMontantTotal() != null ? lp.getMontantTotal().doubleValue() : (brut * (1.0 - tauxRemise / 100.0));

                    piecesBsDto.add(ProformaDetailsResponseDto.LignePieceResponseDto.builder()
                            .id(lp.getId())
                            .pieceId(piece != null ? piece.getId() : null)
                            .type(pieceType)
                            .referencePiece(piece != null ? piece.getReference() : null)
                            .designationPiece(piece != null ? piece.getDesignation() : lp.getDesignation())
                            .designationPds(isCustom ? lp.getDesignation() : null)
                            .isCustom(isCustom)
                            .quantite(qte)
                            .prix(prixUnitaire)
                            .prixCatalogue(prixCatalogue)
                            .montantTotal(total)
                            .tauxRemise(tauxRemise)
                            .dateHeureSaisie(lp.getDateHeureSaisie())
                            .build());
                }
            }
            dto.setLignesPiecesBS(piecesBsDto);
            dto.setLignesPieces(piecesBsDto);

            List<ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto> moBsDto = new ArrayList<>();
            if (bs.getLignesBonDeSortieMainDoeuvres() != null) {
                for (LigneBonDeSortieMainDoeuvre lmo : bs.getLignesBonDeSortieMainDoeuvres()) {
                    MainDoeuvre mo = lmo.getMainDoeuvre();
                    String desc = lmo.getDescription() != null ? lmo.getDescription()
                            : (mo != null ? mo.getDescription() : "Main d'œuvre");
                    double tarif = lmo.getTarifHoraire() != null ? lmo.getTarifHoraire().doubleValue() : 0.0;
                    double heures = lmo.getNbreHeure() != null ? lmo.getNbreHeure().doubleValue() : 1.0;
                    double tauxRemise = lmo.getTauxRemise() != null ? lmo.getTauxRemise().doubleValue() : 0.0;
                    double brut = tarif * heures;
                    double total = lmo.getMontantTotal() != null ? lmo.getMontantTotal().doubleValue() : (brut * (1.0 - tauxRemise / 100.0));

                    moBsDto.add(ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto.builder()
                            .id(lmo.getId())
                            .mainDoeuvreId(mo != null ? mo.getId() : null)
                            .descriptionMainDoeuvre(desc)
                            .nbreHeure(heures)
                            .tarifHoraire(tarif)
                            .montantTotal(total)
                            .tauxRemise(tauxRemise)
                            .dateHeureSaisie(lmo.getDateHeureSaisie())
                            .build());
                }
            }
            dto.setLignesMainDoeuvresBS(moBsDto);
            dto.setLignesMainDoeuvres(moBsDto);
        } else {
            dto.setBonDeSortie(null);
            dto.setHasBonDeSortie(false);
            dto.setMontantTotalBons(0.0);
            dto.setQuantiteTotalePieces(0);
            dto.setLignesPiecesBS(new ArrayList<>());
            dto.setLignesMainDoeuvresBS(new ArrayList<>());

            // Si aucun BS n'existe encore, pré-remplir lignesPieces et lignesMainDoeuvres avec celles du proforma
            dto.setLignesPieces(new ArrayList<>(dto.getLignesPiecesProforma()));
            dto.setLignesMainDoeuvres(new ArrayList<>(dto.getLignesMainDoeuvresProforma()));
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
        if (techDtos.isEmpty() && ot.getOrdreReparation() != null) {
            for (Technicien tech : techniciensDiagnostic(ot.getOrdreReparation())) {
                techDtos.add(TechnicienSummaryDto.builder()
                        .id(tech.getId())
                        .nom(tech.getLastName())
                        .prenom(tech.getFirstName())
                        .specialite(tech.getSpecialite() != null ? tech.getSpecialite().name() : null)
                        .telephone(tech.getPhone())
                        .build());
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
        if (techDtos.isEmpty() && ot.getOrdreReparation() != null) {
            for (Technicien tech : techniciensDiagnostic(ot.getOrdreReparation())) {
                techDtos.add(TechnicienSummaryDto.builder()
                        .id(tech.getId())
                        .nom(tech.getLastName())
                        .prenom(tech.getFirstName())
                        .specialite(tech.getSpecialite() != null ? tech.getSpecialite().name() : null)
                        .telephone(tech.getPhone())
                        .build());
            }
        }
        dto.setTechniciensReparation(techDtos);
        List<DocumentOrdreTravail> docs = documentOrdreTravailRepository.findByOrdreTravailIdOrderByCreatedAtDesc(ot.getId());
        dto.setDocuments(docs.stream().map(DocumentOrdreTravailResponseDto::from).collect(Collectors.toList()));
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public StepBLResponseDto getStepBL(Long id) {
        OrdreTravail ot = getEntityById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ordre de travail non trouvé avec l'id : " + id));

        StepBLResponseDto dto = new StepBLResponseDto();
        dto.setId(ot.getId());
        dto.setNumero(ot.getNumero());
        dto.setStatut(ot.getStatut() != null ? ot.getStatut().name() : null);
        dto.setEtape(ot.getStatut() != null ? ot.getStatut().getEtape() : 5);

        if (ot.getVehicule() != null) {
            Vehicule v = ot.getVehicule();
            dto.setVehiculeId(v.getId());
            dto.setImmatriculation(v.getImmatriculation());
            dto.setMarque(v.getMarque());
            dto.setModele(v.getModele());
        }
        if (ot.getClient() != null) {
            Client c = ot.getClient();
            dto.setClientId(c.getId());
            dto.setClientNom(((c.getFirstName() != null ? c.getFirstName() : "") + " "
                    + (c.getLastName() != null ? c.getLastName() : "")).trim());
            dto.setClientTelephone(c.getPhone());
        }
        if (ot.getProforma() != null) {
            dto.setProformaId(ot.getProforma().getId());
        }
        if (ot.getOrdreReparation() != null) {
            dto.setOrdreReparationId(ot.getOrdreReparation().getId());
        }

        BonDeLivraison bl = null;
        if (ot.getOrdreReparation() != null) {
            bl = bonDeLivraisonRepository.findByOrdreReparationId(ot.getOrdreReparation().getId()).orElse(null);
        }

        if (bl != null) {
            dto.setHasBonDeLivraison(true);
            Long fId = bl.getFacture() != null ? bl.getFacture().getId() : null;
            String fNum = bl.getFacture() != null ? bl.getFacture().getNumero() : null;

            dto.setBonDeLivraison(StepBLResponseDto.BonDeLivraisonSummaryDto.builder()
                    .id(bl.getId())
                    .numero(bl.getNumero())
                    .dateCreation(bl.getDateCreation())
                    .statut(bl.getStatut() != null ? bl.getStatut().name() : null)
                    .remarque(bl.getRemarque())
                    .montantHT(bl.getMontantHT() != null ? bl.getMontantHT().doubleValue() : 0.0)
                    .montantTVA(bl.getMontantTVA() != null ? bl.getMontantTVA().doubleValue() : 0.0)
                    .montantTTC(bl.getMontantTTC() != null ? bl.getMontantTTC().doubleValue() : 0.0)
                    .tvaApplicable(bl.getTvaApplicable())
                    .factureId(fId)
                    .factureNumero(fNum)
                    .build());

            dto.setMontantHT(bl.getMontantHT() != null ? bl.getMontantHT().doubleValue() : 0.0);
            dto.setMontantTVA(bl.getMontantTVA() != null ? bl.getMontantTVA().doubleValue() : 0.0);
            dto.setMontantTTC(bl.getMontantTTC() != null ? bl.getMontantTTC().doubleValue() : 0.0);
            dto.setMontantTotal(bl.getMontantTotal() != null ? bl.getMontantTotal().doubleValue() : 0.0);
            dto.setTvaApplicable(bl.getTvaApplicable());

            double ht = dto.getMontantHT() != null ? dto.getMontantHT() : 0.0;
            double tva = dto.getMontantTVA() != null ? dto.getMontantTVA() : 0.0;
            dto.setTvaRate((ht > 0) ? Math.round((tva / ht * 100.0) * 100.0) / 100.0 : 18.0);

            List<ProformaDetailsResponseDto.LignePieceResponseDto> piecesDto = new ArrayList<>();
            if (bl.getLignesFacturationPieces() != null) {
                for (LigneFacturationPiece lp : bl.getLignesFacturationPieces()) {
                    PieceDetache piece = lp.getPiece();
                    String pieceType = piece != null && piece.getType() != null ? piece.getType().name()
                            : (piece instanceof PDP ? "PDP" : (piece instanceof PDG ? "PDG" : "PDS"));
                    boolean isCustom = "PDS".equalsIgnoreCase(pieceType) || piece == null || Boolean.TRUE.equals(lp.getIsCustom());
                    Double prixCatalogue = piece != null && piece.getPrixUnitaire() != null ? piece.getPrixUnitaire() : null;
                    Double prixUnitaire = lp.getPrix() != null ? lp.getPrix().doubleValue() : (prixCatalogue != null ? prixCatalogue : 0.0);
                    Double tauxRemise = lp.getTauxRemise() != null ? lp.getTauxRemise().doubleValue() : 0.0;
                    int qte = lp.getQuantite() != null ? lp.getQuantite() : 1;
                    double brut = prixUnitaire * qte;
                    double total = brut * (1.0 - tauxRemise / 100.0);

                    piecesDto.add(ProformaDetailsResponseDto.LignePieceResponseDto.builder()
                            .id(lp.getId())
                            .pieceId(piece != null ? piece.getId() : null)
                            .type(pieceType)
                            .referencePiece(piece != null ? piece.getReference() : null)
                            .designationPiece(piece != null ? piece.getDesignation() : lp.getDesignationPds())
                            .designationPds(isCustom ? lp.getDesignationPds() : null)
                            .isCustom(isCustom)
                            .quantite(qte)
                            .prix(prixUnitaire)
                            .prixCatalogue(prixCatalogue)
                            .montantTotal(total)
                            .tauxRemise(tauxRemise)
                            .dateHeureSaisie(lp.getDateHeureSaisie())
                            .build());
                }
            }
            dto.setLignesPieces(piecesDto);

            List<ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto> moDto = new ArrayList<>();
            if (bl.getLignesFacturationMainDoeuvres() != null) {
                for (LigneFacturationMainDoeuvre lmo : bl.getLignesFacturationMainDoeuvres()) {
                    MainDoeuvre mo = lmo.getMainDoeuvre();
                    String desc = mo != null ? mo.getDescription() : "Main d'œuvre";
                    double tarif = lmo.getTarifHoraire() != null ? lmo.getTarifHoraire().doubleValue() : 0.0;
                    double heures = lmo.getNbreHeure() != null ? lmo.getNbreHeure().doubleValue() : 1.0;
                    Double tauxRemise = lmo.getTauxRemise() != null ? lmo.getTauxRemise().doubleValue() : 0.0;
                    double brut = tarif * heures;
                    double total = brut * (1.0 - tauxRemise / 100.0);

                    moDto.add(ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto.builder()
                            .id(lmo.getId())
                            .mainDoeuvreId(mo != null ? mo.getId() : null)
                            .descriptionMainDoeuvre(desc)
                            .nbreHeure(heures)
                            .tarifHoraire(tarif)
                            .montantTotal(total)
                            .tauxRemise(tauxRemise)
                            .dateHeureSaisie(lmo.getDateHeureSaisie())
                            .build());
                }
            }
            dto.setLignesMainDoeuvres(moDto);

            if (bl.getFacture() != null) {
                dto.setHasFacture(true);
                Facture f = bl.getFacture();
                FactureSummaryDto fDto = new FactureSummaryDto();
                fDto.setId(f.getId());
                fDto.setNumero(f.getNumero());
                fDto.setStatut(f.getStatutPaiement() != null ? f.getStatutPaiement().name() : null);
                fDto.setMontantTotal(f.getMontantTotal() != null ? f.getMontantTotal().doubleValue() : 0.0);
                dto.setFacture(fDto);
            }
        } else {
            dto.setHasBonDeLivraison(false);
            BonDeSortie bs = trouverBonDeSortie(ot);
            if (bs != null) {
                List<ProformaDetailsResponseDto.LignePieceResponseDto> piecesDto = new ArrayList<>();
                if (bs.getLignesBonDeSortiePieces() != null) {
                    for (LigneBonDeSortiePiece lp : bs.getLignesBonDeSortiePieces()) {
                        PieceDetache piece = lp.getPiece();
                        String pieceType = piece != null && piece.getType() != null ? piece.getType().name()
                                : (piece instanceof PDP ? "PDP" : (piece instanceof PDG ? "PDG" : "PDS"));
                        boolean isCustom = "PDS".equalsIgnoreCase(pieceType) || piece == null;
                        Double prixCatalogue = piece != null && piece.getPrixUnitaire() != null ? piece.getPrixUnitaire() : null;
                        Double prixUnitaire = lp.getPrix() != null ? lp.getPrix().doubleValue() : (prixCatalogue != null ? prixCatalogue : 0.0);
                        Double tauxRemise = lp.getTauxRemise() != null ? lp.getTauxRemise().doubleValue() : 0.0;
                        int qte = lp.getQuantite() != null ? lp.getQuantite() : 1;
                        double brut = prixUnitaire * qte;
                        double total = lp.getMontantTotal() != null ? lp.getMontantTotal().doubleValue() : (brut * (1.0 - tauxRemise / 100.0));

                        piecesDto.add(ProformaDetailsResponseDto.LignePieceResponseDto.builder()
                                .id(lp.getId())
                                .pieceId(piece != null ? piece.getId() : null)
                                .type(pieceType)
                                .referencePiece(piece != null ? piece.getReference() : null)
                                .designationPiece(piece != null ? piece.getDesignation() : lp.getDesignation())
                                .designationPds(isCustom ? lp.getDesignation() : null)
                                .isCustom(isCustom)
                                .quantite(qte)
                                .prix(prixUnitaire)
                                .prixCatalogue(prixCatalogue)
                                .montantTotal(total)
                                .tauxRemise(tauxRemise)
                                .dateHeureSaisie(lp.getDateHeureSaisie())
                                .build());
                    }
                }
                dto.setLignesPieces(piecesDto);

                List<ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto> moDto = new ArrayList<>();
                if (bs.getLignesBonDeSortieMainDoeuvres() != null) {
                    for (LigneBonDeSortieMainDoeuvre lmo : bs.getLignesBonDeSortieMainDoeuvres()) {
                        MainDoeuvre mo = lmo.getMainDoeuvre();
                        String desc = lmo.getDescription() != null ? lmo.getDescription()
                                : (mo != null ? mo.getDescription() : "Main d'œuvre");
                        double tarif = lmo.getTarifHoraire() != null ? lmo.getTarifHoraire().doubleValue() : 0.0;
                        double heures = lmo.getNbreHeure() != null ? lmo.getNbreHeure().doubleValue() : 1.0;
                        Double tauxRemise = lmo.getTauxRemise() != null ? lmo.getTauxRemise().doubleValue() : 0.0;
                        double brut = tarif * heures;
                        double total = lmo.getMontantTotal() != null ? lmo.getMontantTotal().doubleValue() : (brut * (1.0 - tauxRemise / 100.0));

                        moDto.add(ProformaDetailsResponseDto.LigneMainDoeuvreResponseDto.builder()
                                .id(lmo.getId())
                                .mainDoeuvreId(mo != null ? mo.getId() : null)
                                .descriptionMainDoeuvre(desc)
                                .nbreHeure(heures)
                                .tarifHoraire(tarif)
                                .montantTotal(total)
                                .tauxRemise(tauxRemise)
                                .dateHeureSaisie(lmo.getDateHeureSaisie())
                                .build());
                    }
                }
                dto.setLignesMainDoeuvres(moDto);
                dto.setMontantHT(bs.getMontantTotal() != null ? bs.getMontantTotal().doubleValue() : 0.0);
                dto.setMontantTotal(bs.getMontantTotal() != null ? bs.getMontantTotal().doubleValue() : 0.0);
            }
        }

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
        if (ot.getStatut() == StatutOrdreTravail.TERMINE) {
            traiterFinReparationEtGenererBL(ot);
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

    private void traiterFinReparationEtGenererBL(OrdreTravail ot) {
        if (ot.getDateFinReparation() == null) {
            ot.setDateFinReparation(LocalDateTime.now());
        }
        BonDeSortie bs = trouverBonDeSortie(ot);
        if (bs != null && bonDeLivraisonService != null) {
            bonDeLivraisonService.creerOuMettreAJourDepuisBonDeSortie(bs, ot);
        }
        if (ot.getOrdreReparation() != null) {
            OrdreReparation ordre = ot.getOrdreReparation();
            if (ordre.getStatut() == StatutOrdreReparation.ORDRE_DE_TRAVAIL
                    || ordre.getStatut() == StatutOrdreReparation.PROFORMA) {
                ordre.setStatut(StatutOrdreReparation.BL);
                ordreReparationRepository.save(ordre);
            }
        }
    }

    private BonDeSortie trouverBonDeSortie(OrdreTravail ot) {
        if (ot == null) return null;
        if (ot.getBonDeSortie() != null) {
            return ot.getBonDeSortie();
        }
        if (ot.getOrdreReparation() != null && ot.getOrdreReparation().getId() != null) {
            List<BonDeSortie> list = bonDeSortieRepository.findByOrdreReparationIdOrderByDateDesc(ot.getOrdreReparation().getId());
            if (list != null && !list.isEmpty()) {
                BonDeSortie bs = list.get(0);
                ot.setBonDeSortie(bs);
                return bs;
            }
        }
        if (ot.getId() != null) {
            List<BonDeSortie> list = bonDeSortieRepository.findByOrdreTravailIdOrderByDateDesc(ot.getId());
            if (list != null && !list.isEmpty()) {
                BonDeSortie bs = list.get(0);
                ot.setBonDeSortie(bs);
                return bs;
            }
        }
        List<BonDeSortie> bsList = ot.getBonsDeSortie();
        if (bsList != null && !bsList.isEmpty()) {
            return bsList.get(0);
        }
        return null;
    }

    /**
     * Signale les lignes demandées qui ne sont pas couvertes par des bons de sortie validés.
     * Cet écart est informatif et ne bloque pas la fin de réparation.
     * Les quantités sont agrégées en base au lieu de charger les collections d'entités.
     */
    private List<Technicien> techniciensDiagnostic(OrdreReparation ordre) {
        if (ordre == null || ordre.getDiagnostic() == null) return List.of();
        var diagnostic = ordre.getDiagnostic();
        if (diagnostic.getTechniciens() != null && !diagnostic.getTechniciens().isEmpty()) {
            return new ArrayList<>(diagnostic.getTechniciens());
        }
        return diagnostic.getTechnicien() != null ? List.of(diagnostic.getTechnicien()) : List.of();
    }

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
                StatutBon.VALIDE)) {
            quantitesValidees.merge(clePiece(ligne.getPieceId(), ligne.getDesignation()),
                    quantite(ligne.getQuantiteTotale()), Long::sum);
        }
        for (var ligne : bonDeSortieRepository.sommerQuantitesMainDoeuvreValideesParOrdreReparation(ordreReparationId,
                StatutBon.VALIDE)) {
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
