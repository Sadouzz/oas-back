package sn.oas.facturation.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.data.domain.Page;
import sn.oas.facturation.shared.dto.ApiResponse;
import sn.oas.facturation.shared.dto.ErrorResponse;
import sn.oas.facturation.shared.dto.PageResponse;

@RestControllerAdvice(basePackages = "sn.oas.facturation")
public class ApiResponseInterceptor implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public ApiResponseInterceptor() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Override
    public boolean supports(
            @NonNull MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            @NonNull ServerHttpResponse response) {

        String path = request.getURI().getPath();
        if (isExcludedPath(path)) {
            return body;
        }

        // Fichiers binaires, images, téléchargements de PDF
        if (body instanceof byte[] || body instanceof Resource) {
            return body;
        }

        // Déjà enveloppé ou erreur globale
        if (body instanceof ApiResponse<?> || body instanceof ErrorResponse) {
            return body;
        }

        // Conversion automatique des objets Spring Data Page en PageResponse enrichi
        if (body instanceof Page<?> springPage) {
            return ApiResponse.success(PageResponse.from(springPage));
        }

        // PageResponse déjà construit
        if (body instanceof PageResponse<?>) {
            return ApiResponse.success(body);
        }

        // Cas particulier des chaînes de caractères (évite ClassCastException dans Spring MVC)
        if (body instanceof String str) {
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            try {
                return objectMapper.writeValueAsString(ApiResponse.success(str));
            } catch (Exception e) {
                return ApiResponse.success(str);
            }
        }

        // Réponses vides
        if (body == null) {
            return ApiResponse.success("Opération effectuée avec succès", null);
        }

        // Enveloppe standard pour toutes les autres réponses
        return ApiResponse.success(body);
    }

    private boolean isExcludedPath(String path) {
        return path == null
                || path.equals("/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-resources")
                || path.startsWith("/webjars")
                || path.startsWith("/actuator");
    }
}
