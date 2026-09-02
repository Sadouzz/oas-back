package sn.oas.facturation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

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
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Endpoint WebSocket natif pur
        registry.addEndpoint("/ws-raw")
                .setAllowedOriginPatterns("*");
    }
}
