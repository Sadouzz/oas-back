package sn.oas.facturation.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.Technicien;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.data.enums.Role;
import sn.oas.facturation.auth.data.enums.TypeUser;
import sn.oas.facturation.auth.dto.AuthResponse;
import sn.oas.facturation.auth.dto.LoginRequest;
import sn.oas.facturation.auth.dto.RegisterRequest;
import sn.oas.facturation.auth.repository.ConnectionHistoryRepository;
import sn.oas.facturation.auth.repository.UserRepository;
import sn.oas.facturation.client.repository.ClientRepository;
import sn.oas.facturation.garage.data.entity.Garage;
import sn.oas.facturation.garage.repository.GarageRepository;
import sn.oas.facturation.security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final ConnectionHistoryService connectionHistoryService;
    private final HttpServletRequest httpRequest;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final GarageRepository garageRepository;
    private final sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService documentNumberGeneratorService;

    @Override
    public AuthResponse login(LoginRequest request) {
        String ip = connectionHistoryService.getClientIp(httpRequest);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());
            String role = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .findFirst()
                    .orElse("ROLE_USER");
            connectionHistoryService.saveConnectionLog(request.username(), ip, "SUCCESS");
            
            Long garageId = null;
            String garageName = null;
            User user = userRepository.findByUsername(userDetails.getUsername())
                    .or(() -> userRepository.findByEmail(userDetails.getUsername()))
                    .orElse(null);
            if (user instanceof Agent agent && agent.getGarage() != null) {
                garageId = agent.getGarage().getId();
                garageName = agent.getGarage().getNom();
            }
            
            return AuthResponse.of(token, userDetails.getUsername(), role, garageId, garageName);
        } catch (AuthenticationException e) {
            connectionHistoryService.saveConnectionLog(request.username(), ip, "FAILED");
            throw new BadCredentialsException("Username ou mot de passe incorrect");
        }
    }

    @Override
    public void register(RegisterRequest request) {
        if (userService.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already in use: " + request.username());
        }
        if (request.email() != null && !request.email().isEmpty() && userService.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already in use: " + request.email());
        }
        if (request.phone() != null && !request.phone().isEmpty() && userService.existsByPhone(request.phone())) {
            throw new IllegalArgumentException("Phone number already in use: " + request.phone());
        }
        User user;

        String matricule = request.matricule();
        if (request.type() == TypeUser.CLIENT && (matricule == null || matricule.trim().isEmpty() || userService.existsByMatricule(matricule))) {
            String maxMatricule = clientRepository.findMaxClientMatricule();
            long nextNumber = clientRepository.count() + 1;
            if (maxMatricule != null && maxMatricule.startsWith("CLT-")) {
                try {
                    String numStr = maxMatricule.substring(4);
                    nextNumber = Math.max(nextNumber, Long.parseLong(numStr) + 1);
                } catch (NumberFormatException ignored) {}
            }
            matricule = String.format("CLT-%05d", nextNumber);
            while (userService.existsByMatricule(matricule)) {
                nextNumber++;
                matricule = String.format("CLT-%05d", nextNumber);
            }
        }

        if (matricule != null && userService.existsByMatricule(matricule)) {
            throw new IllegalArgumentException("Matricule already in use: " + matricule);
        }

        if (request.type() == TypeUser.AGENT)
        {
            Garage garage = null;
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                User currentUser = userRepository.findByUsername(auth.getName())
                        .or(() -> userRepository.findByEmail(auth.getName()))
                        .orElse(null);
                if (currentUser instanceof Agent currentAgent && currentAgent.getRole() == sn.oas.facturation.auth.data.enums.Role.MASTER) {
                    garage = currentAgent.getGarage();
                }
            }
            if (garage == null && request.garageId() != null) {
                garage = garageRepository.findById(request.garageId())
                        .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé"));
            }

            // Auto-generate matricule for Agent if not provided
            String agentMatricule = matricule;
            if (agentMatricule == null || agentMatricule.trim().isEmpty()) {
                agentMatricule = documentNumberGeneratorService.generateNextNumber(sn.oas.facturation.shared.documentNumber.DocumentType.AG);
            }

            user = Agent.builder()
                    .matricule(agentMatricule)
                    .phone(request.phone())
                    .username(request.username())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .type(request.type())
                    .role(request.role())
                    .garage(garage)
                    .build();
        } else if (request.type() == TypeUser.CLIENT) {
            user = Client.builder()
                    .matricule(matricule)
                    .phone(request.phone())
                    .username(request.username())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .type(request.type())
                    .build();
        } else if (request.type() == TypeUser.TECHNICIEN) {
            // Garde-fou de sécurité : un compte technicien ne peut être créé que par un membre
            // du staff authentifié (SUPER_AGENT, MASTER ou CHEF_ATELIER — le même périmètre
            // que le garde de route du frontend gestion/techniciens). AuthController.signup()
            // rejette déjà explicitement type=TECHNICIEN pour l'inscription publique ; ce
            // contrôle est la défense en profondeur côté service, quel que soit l'appelant
            // (y compris un futur endpoint qui oublierait le filtrage par route).
            verifierCreateurTechnicienAutorise();

            // Résolution du garage : garage de l'agent connecté (sauf SUPER_AGENT, cf.
            // X-Garage-ID), sinon repli sur request.garageId(). Même sémantique que
            // DocumentNumberGeneratorService.getCurrentGarage().
            Garage garage = resolveGarageForStaffCreation(request.garageId());

            // Auto-generate matricule for Technicien if not provided, en réutilisant la
            // numérotation DocumentType.MEC existante (pas de nouveau type de document).
            String technicienMatricule = matricule;
            if (technicienMatricule == null || technicienMatricule.trim().isEmpty()) {
                technicienMatricule = documentNumberGeneratorService.generateNextNumber(garage,
                        sn.oas.facturation.shared.documentNumber.DocumentType.MEC);
            }

            user = Technicien.builder()
                    .matricule(technicienMatricule)
                    .phone(request.phone())
                    .username(request.username())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .type(request.type())
                    .adresse(request.adresse())
                    .specialite(request.specialite())
                    .garage(garage)
                    .build();
        }
        else {
            throw new IllegalArgumentException("Type d'utilisateur non reconnu");
        }
        userService.saveUser(user);
    }

    private static final java.util.Set<Role> ROLES_AUTORISES_CREATION_TECHNICIEN =
            java.util.Set.of(Role.SUPER_AGENT, Role.MASTER);

    /**
     * Vérifie que la création d'un compte Technicien est bien à l'origine d'un Agent
     * authentifié avec le rôle SUPER_AGENT ou MASTER (un technicien ne peut pas se créer
     * lui-même, et le chef d'atelier n'est pas habilité à créer des comptes technicien).
     * Lève IllegalArgumentException sinon (traduit en 409 par GlobalExceptionHandler pour
     * /api/auth/signup, et en 400 par les contrôleurs staff qui catchent RuntimeException,
     * ex. TechnicienController).
     */
    private void verifierCreateurTechnicienAutorise() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autorise = auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")
                && auth.getPrincipal() instanceof Agent agent
                && agent.getRole() != null
                && ROLES_AUTORISES_CREATION_TECHNICIEN.contains(agent.getRole());
        if (!autorise) {
            throw new IllegalArgumentException(
                    "La création d'un compte technicien nécessite d'être authentifié en tant que SUPER_AGENT ou MASTER");
        }
    }

    /**
     * Résout le garage à assigner lors d'une création "staff" (ex: Technicien) : garage de
     * l'agent actuellement connecté (sauf SUPER_AGENT, qui doit passer par l'en-tête
     * X-Garage-ID ou un garageId explicite), sinon repli sur le garageId fourni dans la
     * requête. Reproduit la sémantique de DocumentNumberGeneratorService.getCurrentGarage().
     */
    private Garage resolveGarageForStaffCreation(Long requestGarageId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")
                && auth.getPrincipal() instanceof Agent currentAgent) {
            if (currentAgent.getRole() == sn.oas.facturation.auth.data.enums.Role.SUPER_AGENT) {
                String garageIdHeader = httpRequest.getHeader("X-Garage-ID");
                if (garageIdHeader != null && !garageIdHeader.isEmpty()) {
                    return garageRepository.findById(Long.parseLong(garageIdHeader))
                            .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé"));
                }
            } else if (currentAgent.getGarage() != null) {
                return currentAgent.getGarage();
            }
        }
        if (requestGarageId != null) {
            return garageRepository.findById(requestGarageId)
                    .orElseThrow(() -> new IllegalArgumentException("Garage non trouvé"));
        }
        return null;
    }

    @Override
    public Agent getAgentConnecte() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable"));
        if (!(user instanceof Agent agent)) {
            throw new IllegalStateException("Cette opération requiert un compte Agent");
        }
        return agent;
    }
}
