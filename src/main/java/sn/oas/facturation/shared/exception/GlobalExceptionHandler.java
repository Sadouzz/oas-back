package sn.oas.facturation.shared.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sn.oas.facturation.shared.dto.ErrorResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Ressource non trouvée : {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex, HttpServletRequest request) {
        log.warn("Conflit de ressource : {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("Requête invalide : {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Argument invalide : {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        log.warn("État illégal : {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class, UnauthorizedException.class})
    public ResponseEntity<ErrorResponse> handleAuthentication(Exception ex, HttpServletRequest request) {
        log.warn("Échec d'authentification : {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                ex.getMessage() != null ? ex.getMessage() : "Identifiants invalides ou session expirée",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler({AccessDeniedException.class, ForbiddenException.class})
    public ResponseEntity<ErrorResponse> handleAccessDenied(Exception ex, HttpServletRequest request) {
        log.warn("Accès refusé : {} - URI: {}", ex.getMessage(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.FORBIDDEN.value(),
                "Forbidden",
                "Vous n'avez pas les autorisations nécessaires pour accéder à cette ressource",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Erreur de validation des champs : {} - URI: {}", errors, request.getRequestURI());
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Erreur de validation sur les champs soumis",
                request.getRequestURI(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        log.warn("Méthode HTTP non supportée : {} pour {} ", ex.getMethod(), request.getRequestURI());
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        Throwable rootCause = ex.getMostSpecificCause();
        String rootMsg = rootCause != null ? rootCause.getMessage() : ex.getMessage();
        log.error("Violation d'intégrité des données : {} - URI: {}", rootMsg, request.getRequestURI());

        String message = "Une contrainte d'intégrité a été violée (doublon ou référence invalide).";
        if (rootMsg != null) {
            String lower = rootMsg.toLowerCase();
            if (lower.contains("immatriculation")) {
                message = "Un véhicule avec cette immatriculation existe déjà.";
            } else if (lower.contains("numero_chassis") || lower.contains("numerochassis")) {
                message = "Un véhicule avec ce numéro de châssis existe déjà.";
            } else if (lower.contains("telephone") || lower.contains("phone")) {
                message = "Ce numéro de téléphone est déjà utilisé.";
            } else if (lower.contains("email")) {
                message = "Cette adresse email est déjà utilisée.";
            } else if (rootMsg.contains("Detail:") || rootMsg.contains("Détail :")) {
                String detail = rootMsg.contains("Detail:")
                        ? rootMsg.substring(rootMsg.indexOf("Detail:") + 7).trim()
                        : rootMsg.substring(rootMsg.indexOf("Détail :") + 8).trim();
                message = "Conflit d'intégrité : " + detail;
            } else {
                message = rootMsg;
            }
        }

        ErrorResponse error = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                "Conflict",
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, HttpServletRequest request) {
        log.error("Erreur interne inattendue sur {}: ", request.getRequestURI(), ex);
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage() != null ? ex.getMessage() : "Une erreur interne inattendue est survenue",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
