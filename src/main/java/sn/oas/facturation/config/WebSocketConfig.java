package sn.oas.facturation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import sn.oas.facturation.features.auth.service.CustomUserDetailsService;
import sn.oas.facturation.security.JwtUtil;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    @Value("${app.cors.allowed-origins:http://localhost:4200}")
    private String allowedOrigins;

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                if (accessor == null || accessor.getCommand() == null) return message;

                if (accessor.getCommand() == StompCommand.CONNECT && accessor.getUser() == null) {
                    String authorization = accessor.getFirstNativeHeader("Authorization");
                    if (authorization == null || !authorization.startsWith("Bearer ")) {
                        throw new AccessDeniedException("Authentification WebSocket requise.");
                    }
                    String token = authorization.substring(7).trim();
                    if (token.isEmpty() || !jwtUtil.validateJwtToken(token)) {
                        throw new AccessDeniedException("Jeton WebSocket invalide ou expiré.");
                    }
                    String username = jwtUtil.getUsernameFromToken(token);
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    if (!userDetails.isEnabled()) {
                        throw new AccessDeniedException("Compte désactivé.");
                    }
                    Authentication authentication = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    accessor.setUser(authentication);
                }

                if (accessor.getCommand() == StompCommand.SEND) {
                    throw new AccessDeniedException("Les clients ne peuvent pas publier sur le canal de notification.");
                }
                if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
                    Principal principal = accessor.getUser();
                    String destination = accessor.getDestination();
                    if (principal == null || !"/user/queue/notifications".equals(destination)) {
                        throw new AccessDeniedException("Abonnement WebSocket non autorisé.");
                    }
                }
                return message;
            }
        });
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un broker simple en mémoire pour les messages à destination des clients
        config.enableSimpleBroker("/topic", "/queue");
        // Préfixe pour les messages envoyés par le client via @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        // Préfixe pour cibler un utilisateur en particulier
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint WebSocket principal avec support SockJS et CORS permissif
        List<String> origins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins.toArray(String[]::new))
                .withSockJS();

        // Endpoint WebSocket natif pur
        registry.addEndpoint("/ws-raw")
                .setAllowedOrigins(origins.toArray(String[]::new));
    }
}
