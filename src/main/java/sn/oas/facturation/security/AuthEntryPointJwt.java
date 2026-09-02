package sn.oas.facturation.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
public class AuthEntryPointJwt implements AuthenticationEntryPoint {

    private final HandlerExceptionResolver resolver;

    public AuthEntryPointJwt(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) {
        // Transfère automatiquement l'exception à GlobalExceptionHandler !
        resolver.resolveException(request, response, null, authException);
    }
}
