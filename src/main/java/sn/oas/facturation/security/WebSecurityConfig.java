package sn.oas.facturation.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final AuthTokenFilter authTokenFilter;
    private final AuthEntryPointJwt unauthorizedHandler;

    @Value("${app.cors.allowed-origins}")
    private List<String> allowedOrigins;
    // private String[] allowedOrigins;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // config.setAllowedOrigins(List.of(allowedOrigins));
        config.setAllowedOrigins(allowedOrigins);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/actuator/**",
                                "/error",
                                "/api/blog",
                                "/api/blog/**",
                                "/api/partenaires",
                                "/api/partenaires/**",
                                "/api/fournisseurs",
                                "/api/fournisseurs/**",
                                "/api/client/marketplace/produits",
                                "/api/client/marketplace/produits/**",
                                "/ws/**",
                                "/ws-raw/**")
                        .permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/admin/garages", "/api/admin/garages/**")
                        .hasAnyAuthority("ROLE_SUPER_AGENT", "SUPER_AGENT", "CLIENT", "ROLE_CLIENT")
                        .requestMatchers("/api/admin/users/**")
                        .hasAnyAuthority("ROLE_SUPER_AGENT", "SUPER_AGENT", "ROLE_MASTER", "MASTER")
                        .requestMatchers("/api/admin/**")
                        .hasAnyAuthority("ROLE_SUPER_AGENT", "SUPER_AGENT", "ROLE_MASTER", "MASTER",
                                "ROLE_CHEF_ATELIER", "CHEF_ATELIER", "ROLE_AGENT_MAGASIN", "AGENT_MAGASIN",
                                "ROLE_AGENT", "AGENT", "CLIENT", "ROLE_CLIENT")
                        .requestMatchers("/api/technicien/**")
                        .hasAnyAuthority("ROLE_TECHNICIEN", "TECHNICIEN")
                        .requestMatchers("/api/ordres-reparation", "/api/ordres-reparation/**")
                        .hasAnyAuthority("ROLE_AGENT", "AGENT", "ROLE_SUPER_AGENT", "SUPER_AGENT", "ROLE_MASTER", "MASTER",
                                "ROLE_CHEF_ATELIER", "CHEF_ATELIER")

                        .anyRequest().authenticated());

        http.addFilterBefore(
                authTokenFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
