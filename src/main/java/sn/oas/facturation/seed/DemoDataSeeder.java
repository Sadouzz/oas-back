package sn.oas.facturation.seed;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.Technicien;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.auth.data.enums.Specialite;
import sn.oas.facturation.auth.data.enums.TypeUser;
import sn.oas.facturation.auth.repository.AgentRepository;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.ficheAtelier.data.entity.LigneReception;
import sn.oas.facturation.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.repository.GarageRepository;
import sn.oas.facturation.technicien.repository.TechnicienRepository;
import sn.oas.facturation.messagerie.data.entity.Message;
import sn.oas.facturation.messagerie.repository.MessageRepository;
import sn.oas.facturation.rendezvous.data.entity.RendezVous;
import sn.oas.facturation.rendezvous.data.enums.RendezVousStatus;
import sn.oas.facturation.rendezvous.repository.RendezVousRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;
import sn.oas.facturation.shared.documentNumber.DocumentType;
import sn.oas.facturation.vehicule.data.entity.Vehicule;
import sn.oas.facturation.vehicule.repository.VehiculeRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Seed de données de démonstration pour l'environnement local.
 * Suit le même principe d'idempotence que {@link sn.oas.facturation.blog.BlogDataSeeder} :
 * chaque bloc ne s'exécute que si sa table est vide, donc un redémarrage répété
 * ne duplique rien. Le module "Ordre de réparation" n'est volontairement pas
 * seedé ici (propriété exclusive de l'équipe qui le développe).
 */
@Component
@RequiredArgsConstructor
@Slf4j
@Order(20)
public class DemoDataSeeder implements CommandLineRunner {

    private static final String DEMO_PASSWORD = "Passer@2026";

    private final GarageRepository garageRepository;
    private final AgentRepository agentRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;
    private final VehiculeRepository vehiculeRepository;
    private final TechnicienRepository technicienRepository;
    private final RendezVousRepository rendezVousRepository;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    public void run(String... args) {
        log.info("=== Vérification des données de démonstration ===");

        List<Garage> garages = seedGarages();
        Garage dakar = garages.get(0);
        Garage thies = garages.get(1);

        List<Agent> agents = seedAgents(dakar, thies);

        List<Client> clients = seedClients();
        List<Vehicule> vehicules = seedVehicules(clients);

        seedTechniciens(dakar, thies);

        List<RendezVous> rendezVousList = seedRendezVous(dakar, thies, clients, vehicules);

        seedFichesAtelier(rendezVousList);

        seedMessages(dakar, clients, agents);

        log.info("=== Données de démonstration prêtes (mot de passe commun : {}) ===", DEMO_PASSWORD);
    }

    // ── Garages ──────────────────────────────────────────────

    private List<Garage> seedGarages() {
        if (garageRepository.count() == 0) {
            log.info("--- Création des garages de démonstration ---");
            Garage dakar = garageRepository.save(Garage.builder()
                    .nom("Orient Auto Service - Dakar")
                    .localite("Dakar, Sénégal")
                    .prefixe("OAS")
                    .numeroFixe("+221 33 123 45 67")
                    .numeroWhatsapp("+221 77 123 45 67")
                    .email("dakar@orientautoservice.sn")
                    .build());
            Garage thies = garageRepository.save(Garage.builder()
                    .nom("Orient Auto Service - Thiès")
                    .localite("Thiès, Sénégal")
                    .prefixe("OAT")
                    .numeroFixe("+221 33 951 22 33")
                    .numeroWhatsapp("+221 77 951 22 33")
                    .email("thies@orientautoservice.sn")
                    .build());
            return List.of(dakar, thies);
        }
        // Réutilise les garages existants (ordre par id pour rester déterministe).
        List<Garage> existing = garageRepository.findAll();
        existing.sort((a, b) -> Long.compare(a.getId(), b.getId()));
        if (existing.size() < 2) {
            // Un seul garage existant : on le duplique logiquement en second point de référence
            // pour ne pas planter le reste du seed, sans rien créer de nouveau en base.
            return List.of(existing.get(0), existing.get(0));
        }
        return existing;
    }

    // ── Agents ───────────────────────────────────────────────

    private List<Agent> seedAgents(Garage dakar, Garage thies) {
        record ASpec(String firstName, String lastName, String username, String phone, String email, Role role, Garage garage) {}
        List<ASpec> specs = List.of(
                new ASpec("Amadou", "Diallo", "admin", "+221771111101", "amadou.diallo@orientautoservice.sn", Role.SUPER_AGENT, dakar),
                new ASpec("Fatou", "Ndiaye", "fatou.ndiaye", "+221771111102", "fatou.ndiaye@orientautoservice.sn", Role.MASTER, dakar),
                new ASpec("Moussa", "Sow", "moussa.sow", "+221771111103", "moussa.sow@orientautoservice.sn", Role.CHEF_ATELIER, dakar),
                new ASpec("Aissatou", "Ba", "aissatou.ba", "+221771111104", "aissatou.ba@orientautoservice.sn", Role.AGENT, dakar),
                new ASpec("Ibrahima", "Kane", "ibrahima.kane", "+221771111105", "ibrahima.kane@orientautoservice.sn", Role.AGENT_MAGASIN, dakar),
                new ASpec("Cheikh", "Faye", "cheikh.faye", "+221771111106", "cheikh.faye@orientautoservice.sn", Role.AGENT, thies),
                new ASpec("Awa", "Diop", "awa.diop", "+221771111107", "awa.diop@orientautoservice.sn", Role.CHEF_ATELIER, thies)
        );

        List<Agent> result = new java.util.ArrayList<>();
        boolean createdAny = false;
        for (ASpec s : specs) {
            Agent agent = userRepository.findByUsername(s.username())
                    .filter(Agent.class::isInstance)
                    .map(Agent.class::cast)
                    .orElse(null);
            if (agent == null) {
                agent = agentRepository.save(newAgent(s.firstName(), s.lastName(), s.username(), s.phone(), s.email(), s.role(), s.garage()));
                createdAny = true;
            }
            result.add(agent);
        }
        if (createdAny) {
            log.info("--- Agents de démonstration créés ---");
        }
        return result;
    }

    private Agent newAgent(String firstName, String lastName, String username, String phone, String email, Role role, Garage garage) {
        return Agent.builder()
                .matricule(documentNumberGeneratorService.generateNextNumber(garage, DocumentType.AG))
                .phone(phone)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .type(TypeUser.AGENT)
                .role(role)
                .garage(garage)
                .build();
    }

    // ── Clients ──────────────────────────────────────────────
    // Idempotence par username (et non par count()==0) : un compte client
    // préexistant (ex. compte de test créé manuellement) ne doit pas empêcher
    // la création des clients de démonstration nommés ci-dessous.

    private List<Client> seedClients() {
        Object[][] specs = {
                {"Mamadou", "Diagne", "mamadou.diagne", "+221701111201", "mamadou.diagne@gmail.com", 1},
                {"Khadija", "Sarr", "khadija.sarr", "+221701111202", "khadija.sarr@gmail.com", 2},
                {"Ousmane", "Fall", "ousmane.fall", "+221701111203", "ousmane.fall@gmail.com", 3},
                {"Bineta", "Cissé", "bineta.cisse", "+221701111204", "bineta.cisse@gmail.com", 4},
                {"Abdoulaye", "Thiam", "abdoulaye.thiam", "+221701111205", "abdoulaye.thiam@gmail.com", 5},
                {"Aminata", "Sy", "aminata.sy", "+221701111206", "aminata.sy@gmail.com", 6},
        };

        List<Client> result = new java.util.ArrayList<>();
        boolean createdAny = false;
        for (Object[] s : specs) {
            String username = (String) s[2];
            Client client = userRepository.findByUsername(username)
                    .filter(Client.class::isInstance)
                    .map(Client.class::cast)
                    .orElse(null);
            if (client == null) {
                client = newClient((String) s[0], (String) s[1], username, (String) s[3], (String) s[4], (int) s[5]);
                client = clientRepository.save(client);
                createdAny = true;
            }
            result.add(client);
        }
        if (createdAny) {
            log.info("--- Clients de démonstration créés ({} au total dans cette liste) ---", result.size());
        }
        return result;
    }

    private Client newClient(String firstName, String lastName, String username, String phone, String email, int seq) {
        return Client.builder()
                .matricule(String.format("CLT-%05d", seq))
                .phone(phone)
                .username(username)
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(passwordEncoder.encode(DEMO_PASSWORD))
                .type(TypeUser.CLIENT)
                .build();
    }

    // ── Véhicules ────────────────────────────────────────────

    private List<Vehicule> seedVehicules(List<Client> clients) {
        if (clients.size() < 6) {
            log.warn("Moins de 6 clients disponibles, seed des véhicules ignoré.");
            return List.of();
        }

        record VSpec(String immat, String marque, String modele, int annee, double km, String chassis, int clientIdx) {}
        List<VSpec> specs = List.of(
                new VSpec("DK-2234-AB", "Toyota", "Hilux", 2019, 52000.0, "JT1BR32E9Y0123001", 0),
                new VSpec("DK-4471-CD", "Toyota", "Corolla", 2021, 21000.0, "JT1BR32E9Y0123002", 1),
                new VSpec("DK-5589-EF", "Peugeot", "308", 2018, 78000.0, "VF3LCYHZPHS123003", 1),
                new VSpec("DK-6612-GH", "Renault", "Duster", 2020, 35500.0, "VF1HSRA0X64123004", 2),
                new VSpec("TH-1123-IJ", "Hyundai", "Tucson", 2022, 9800.0, "KMHJ381DGGU123005", 3),
                new VSpec("DK-7745-KL", "Kia", "Sportage", 2017, 95000.0, "KNAPC81BGH7123006", 4),
                new VSpec("TH-3387-MN", "Mercedes-Benz", "Sprinter", 2016, 142000.0, "WD3PE7CD5GP123007", 5),
                new VSpec("DK-8890-OP", "Nissan", "Qashqai", 2020, 41200.0, "SJNFAAJ11U1123008", 5),
                new VSpec("DK-9932-QR", "Toyota", "Land Cruiser", 2015, 168000.0, "JTMHV05J904123009", 0)
        );

        List<Vehicule> result = new java.util.ArrayList<>();
        boolean createdAny = false;
        for (VSpec s : specs) {
            Vehicule vehicule = vehiculeRepository.findByImmatriculation(s.immat()).orElse(null);
            if (vehicule == null) {
                vehicule = vehiculeRepository.save(Vehicule.builder()
                        .immatriculation(s.immat())
                        .marque(s.marque())
                        .modele(s.modele())
                        .annee(s.annee())
                        .kilometrage(s.km())
                        .numeroChassis(s.chassis())
                        .client(clients.get(s.clientIdx()))
                        .build());
                createdAny = true;
            }
            result.add(vehicule);
        }
        if (createdAny) {
            log.info("--- Véhicules de démonstration créés ---");
        }
        return result;
    }

    // ── Techniciens ──────────────────────────────────────────
    // Remplace l'ancien module mecanicien/ : Technicien est un compte utilisateur à part
    // entière (login propre), donc idempotence par username comme pour Agent/Client.

    private void seedTechniciens(Garage dakar, Garage thies) {
        record TSpec(String firstName, String lastName, String username, String phone, String email, Specialite specialite, Garage garage) {}
        List<TSpec> specs = List.of(
                new TSpec("Alioune Badara", "Diouf", "alioune.diouf", "+221771111201", "alioune.diouf@orientautoservice.sn", Specialite.MECANIQUE_GENERALE, dakar),
                new TSpec("Serigne", "Mbaye", "serigne.mbaye", "+221771111202", "serigne.mbaye@orientautoservice.sn", Specialite.ELECTRICITE_AUTO, dakar),
                new TSpec("Modou", "Lô Ndoye", "modou.ndoye", "+221771111203", "modou.ndoye@orientautoservice.sn", Specialite.CARROSSERIE_PEINTURE, dakar),
                new TSpec("Pape Abdou", "Fall", "pape.fall", "+221771111204", "pape.fall@orientautoservice.sn", Specialite.MECANIQUE_GENERALE, thies),
                new TSpec("Lamine", "Gueye", "lamine.gueye", "+221771111205", "lamine.gueye@orientautoservice.sn", Specialite.DIAGNOSTIC_ELECTRONIQUE, thies)
        );

        boolean createdAny = false;
        for (TSpec s : specs) {
            boolean exists = userRepository.findByUsername(s.username())
                    .filter(Technicien.class::isInstance)
                    .isPresent();
            if (!exists) {
                technicienRepository.save(Technicien.builder()
                        .matricule(documentNumberGeneratorService.generateNextNumber(s.garage(), DocumentType.MEC))
                        .phone(s.phone())
                        .username(s.username())
                        .firstName(s.firstName())
                        .lastName(s.lastName())
                        .email(s.email())
                        .password(passwordEncoder.encode(DEMO_PASSWORD))
                        .type(TypeUser.TECHNICIEN)
                        .specialite(s.specialite())
                        .garage(s.garage())
                        .build());
                createdAny = true;
            }
        }
        if (createdAny) {
            log.info("--- Techniciens de démonstration créés ---");
        }
    }

    // ── Rendez-vous ──────────────────────────────────────────

    private List<RendezVous> seedRendezVous(Garage dakar, Garage thies, List<Client> clients, List<Vehicule> vehicules) {
        if (rendezVousRepository.count() > 0) {
            return rendezVousRepository.findAll();
        }
        if (clients.size() < 6 || vehicules.size() < 9) {
            log.warn("Données clients/véhicules insuffisantes, seed des rendez-vous ignoré.");
            return List.of();
        }
        log.info("--- Création des rendez-vous de démonstration ---");

        LocalDateTime now = LocalDateTime.now();

        List<RendezVous> rdvs = List.of(
                rdv(dakar, clients.get(0), vehicules.get(0), now.plusDays(2).withHour(9).withMinute(0), "Vidange + révision générale", RendezVousStatus.EN_ATTENTE, null),
                rdv(dakar, clients.get(1), vehicules.get(1), now.plusDays(1).withHour(14).withMinute(30), "Bruit suspect au freinage", RendezVousStatus.CONFIRME, "Plaquettes à vérifier en priorité"),
                rdv(dakar, clients.get(1), vehicules.get(2), now.plusDays(3).withHour(10).withMinute(0), "Climatisation ne refroidit plus", RendezVousStatus.EN_ATTENTE, null),
                rdv(dakar, clients.get(2), vehicules.get(3), now.minusDays(5).withHour(11).withMinute(0), "Contrôle technique + pneus", RendezVousStatus.TERMINE, "Deux pneus avant remplacés"),
                rdv(thies, clients.get(3), vehicules.get(4), now.plusDays(4).withHour(15).withMinute(0), "Voyant moteur allumé", RendezVousStatus.CONFIRME, "Diagnostic électronique programmé"),
                rdv(dakar, clients.get(4), vehicules.get(5), now.minusDays(10).withHour(9).withMinute(30), "Embrayage à changer", RendezVousStatus.TERMINE, "Embrayage complet remplacé"),
                rdv(thies, clients.get(5), vehicules.get(6), now.minusDays(2).withHour(8).withMinute(0), "Entretien courant Sprinter", RendezVousStatus.CONFIRME, null),
                rdv(dakar, clients.get(5), vehicules.get(7), now.plusDays(6).withHour(16).withMinute(0), "Changement batterie", RendezVousStatus.EN_ATTENTE, null),
                rdv(dakar, clients.get(0), vehicules.get(8), now.minusDays(1).withHour(13).withMinute(0), "Demande annulée par le client", RendezVousStatus.ANNULE, "Client indisponible"),
                rdv(dakar, clients.get(2), vehicules.get(3), now.minusDays(15).withHour(10).withMinute(0), "Fuite d'huile moteur", RendezVousStatus.REFUSE, "Créneau garage indisponible, à replanifier")
        );
        return rendezVousRepository.saveAll(rdvs);
    }

    private RendezVous rdv(Garage garage, Client client, Vehicule vehicule, LocalDateTime date, String motif, RendezVousStatus statut, String commentaire) {
        return RendezVous.builder()
                .numero(documentNumberGeneratorService.generateNextNumber(garage, DocumentType.RDV))
                .client(client)
                .vehicule(vehicule)
                .garage(garage)
                .dateRendezVous(date)
                .motif(motif)
                .statut(statut)
                .commentaire(commentaire)
                .build();
    }

    // ── Fiches atelier ───────────────────────────────────────

    private void seedFichesAtelier(List<RendezVous> rendezVousList) {
        if (ficheAtelierRepository.count() > 0 || rendezVousList.isEmpty()) {
            return;
        }
        log.info("--- Création des fiches atelier de démonstration ---");

        // Les deux rendez-vous CONFIRME/TERMINE créés ci-dessus, dans l'ordre de seedRendezVous().
        RendezVous rdvFreinage = rendezVousList.get(1);   // CONFIRME - Khadija Sarr / Corolla
        RendezVous rdvEmbrayage = rendezVousList.get(5);  // TERMINE - Abdoulaye Thiam / Sportage

        List<FicheAtelier> fiches = List.of(
                FicheAtelier.builder()
                        .rendezVous(rdvFreinage)
                        .client(rdvFreinage.getClient())
                        .vehicule(rdvFreinage.getVehicule())
                        .garage(rdvFreinage.getGarage())
                        .nomChauffeur("Khadija Sarr")
                        .telephoneChauffeur("+221701111202")
                        .niveauEssence("1/2")
                        .kilometrage(21000)
                        .designationTravaux("Contrôle et remplacement des plaquettes de frein avant")
                        .lignesReception(List.of(
                                LigneReception.builder().nom("Autoradio").etat(true).build(),
                                LigneReception.builder().nom("Roue de secours").etat(true).build(),
                                LigneReception.builder().nom("Cric et clé").etat(true).build()
                        ))
                        .lignesDefauts(List.of(
                                LigneDefaut.builder().nom("Rayure carrosserie (aile avant droite)").present(true).build(),
                                LigneDefaut.builder().nom("Impact pare-brise").present(false).build()
                        ))
                        .nb("Client à recontacter avant tout remplacement de pièce supplémentaire.")
                        .dateSortiePrevue(rdvFreinage.getDateRendezVous().plusDays(1))
                        .garantie("3 mois")
                        .build(),

                FicheAtelier.builder()
                        .rendezVous(rdvEmbrayage)
                        .client(rdvEmbrayage.getClient())
                        .vehicule(rdvEmbrayage.getVehicule())
                        .garage(rdvEmbrayage.getGarage())
                        .nomChauffeur("Abdoulaye Thiam")
                        .telephoneChauffeur("+221701111205")
                        .niveauEssence("Full")
                        .kilometrage(95000)
                        .designationTravaux("Remplacement kit d'embrayage complet + volant moteur")
                        .lignesReception(List.of(
                                LigneReception.builder().nom("Autoradio").etat(true).build(),
                                LigneReception.builder().nom("Tapis de sol").etat(true).build()
                        ))
                        .lignesDefauts(List.of(
                                LigneDefaut.builder().nom("Rayure carrosserie").present(false).build()
                        ))
                        .nb("Véhicule restitué propre, essai routier concluant.")
                        .dateSortiePrevue(rdvEmbrayage.getDateRendezVous().plusDays(2))
                        .garantie("6 mois")
                        .signatureSortieBase64(null)
                        .build()
        );
        ficheAtelierRepository.saveAll(fiches);
    }

    // ── Messages ─────────────────────────────────────────────

    private void seedMessages(Garage dakar, List<Client> clients, List<Agent> agents) {
        if (messageRepository.count() > 0 || clients.isEmpty() || agents.isEmpty()) {
            return;
        }
        log.info("--- Création des messages de démonstration ---");

        Client client = clients.get(1); // Khadija Sarr
        Agent agent = agents.stream().filter(a -> "aissatou.ba".equals(a.getUsername())).findFirst().orElse(agents.get(0));

        List<Message> messages = List.of(
                Message.builder()
                        .numero(documentNumberGeneratorService.generateNextNumber(dakar, DocumentType.MSG))
                        .client(client)
                        .expediteur(client)
                        .destinataire(null)
                        .garage(dakar)
                        .contenu("Bonjour, où en est la réparation de ma Corolla (bruit au freinage) ?")
                        .lu(true)
                        .build(),
                Message.builder()
                        .numero(documentNumberGeneratorService.generateNextNumber(dakar, DocumentType.MSG))
                        .client(client)
                        .expediteur(agent)
                        .destinataire(client)
                        .garage(dakar)
                        .contenu("Bonjour Khadija, le diagnostic est fait : les plaquettes avant doivent être changées. Nous vous appelons dans l'heure pour valider.")
                        .lu(false)
                        .build()
        );
        messageRepository.saveAll(messages);
    }
}
