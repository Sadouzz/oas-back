package sn.oas.facturation.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.oas.facturation.auth.data.entity.Agent;
import sn.oas.facturation.auth.data.entity.Client;
import sn.oas.facturation.auth.data.entity.User;
import sn.oas.facturation.auth.data.enums.TypeUser;
import sn.oas.facturation.auth.dto.AuthResponse;
import sn.oas.facturation.auth.dto.LoginRequest;
import sn.oas.facturation.auth.dto.RegisterRequest;
import sn.oas.facturation.auth.repository.ConnectionHistoryRepository;
import sn.oas.facturation.auth.repository.UserRepository;
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
            return AuthResponse.of(token, userDetails.getUsername(), role);
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
        User user;

        if (request.type() == TypeUser.AGENT)
        {
            user = Agent.builder()
                    .matricule(request.matricule())
                    .phone(request.phone())
                    .username(request.username())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .type(request.type())
                    .role(request.role())
                    .build();
        } else if (request.type() == TypeUser.CLIENT) {
            user = Client.builder()
                    .matricule(request.matricule())
                    .phone(request.phone())
                    .username(request.username())
                    .firstName(request.firstName())
                    .lastName(request.lastName())
                    .email(request.email())
                    .password(passwordEncoder.encode(request.password()))
                    .type(request.type())
                    .build();
        }
        else {
            throw new IllegalArgumentException("Type d'utilisateur non reconnu");
        }
        userService.saveUser(user);
    }
}
