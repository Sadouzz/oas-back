package sn.oas.facturation.features.media;

import com.cloudinary.Cloudinary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.features.media.dto.MediaSignatureResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/media")
@RequiredArgsConstructor
@Tag(name = "Médias", description = "Signature d'upload Cloudinary — le fichier part directement du client vers Cloudinary, jamais par ce serveur")
public class MediaController {

    private final Cloudinary cloudinary;

    @Value("${app.cloudinary.cloud-name}")
    private String cloudName;

    @Value("${app.cloudinary.api-key}")
    private String apiKey;

    /**
     * Dossier autorisé : lettres/chiffres/-/_ par segment, séparés par "/". Rejette ".." et les caractères
     * spéciaux pour éviter qu'un dossier forgé sorte de l'arborescence "oas/...".
     */
    private static final String FOLDER_PATTERN = "^[A-Za-z0-9_-]+(/[A-Za-z0-9_-]+)*$";

    @GetMapping("/signature")
    @PreAuthorize("hasAnyRole('AGENT', 'SUPER_AGENT', 'MASTER', 'CHEF_ATELIER', 'AGENT_MAGASIN', 'TECHNICIEN')")
    @Operation(summary = "Génère une signature d'upload Cloudinary de courte durée pour un dossier donné")
    public ResponseEntity<?> getSignature(@RequestParam(required = false) String folder, Authentication authentication) {
        String requestedFolder = folder == null || folder.isBlank() ? "oas/temp" : folder;
        String safeFolder = sanitizeFolder(requestedFolder);
        if (folder != null && safeFolder == null) {
            return ResponseEntity.badRequest().body("Dossier invalide.");
        }
        if (!isAllowedFolder(safeFolder, authentication)) {
            throw new AccessDeniedException("Ce dossier d'upload n'est pas autorisé pour ce rôle.");
        }

        long timestamp = System.currentTimeMillis() / 1000L;

        Map<String, Object> paramsToSign = new HashMap<>();
        paramsToSign.put("timestamp", timestamp);
        if (safeFolder != null) {
            paramsToSign.put("folder", safeFolder);
        }

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);

        return ResponseEntity.ok(new MediaSignatureResponse(signature, timestamp, apiKey, cloudName, safeFolder));
    }

    private boolean isAllowedFolder(String folder, Authentication authentication) {
        if (folder == null || authentication == null) return false;
        boolean technician = authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_TECHNICIEN".equals(a.getAuthority()) || "TECHNICIEN".equals(a.getAuthority()));
        boolean staff = authentication.getAuthorities().stream().anyMatch(a -> switch (a.getAuthority()) {
            case "ROLE_AGENT", "AGENT", "ROLE_SUPER_AGENT", "SUPER_AGENT", "ROLE_MASTER", "MASTER",
                    "ROLE_CHEF_ATELIER", "CHEF_ATELIER", "ROLE_AGENT_MAGASIN", "AGENT_MAGASIN" -> true;
            default -> false;
        });
        if ("oas/temp".equals(folder)) return technician || staff;
        if ("oas/ordres-reparation/diagnostic".equals(folder)) return technician || staff;
        return "oas/pdf-templates".equals(folder) && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SUPER_AGENT".equals(a.getAuthority()) || "SUPER_AGENT".equals(a.getAuthority())
                        || "ROLE_MASTER".equals(a.getAuthority()) || "MASTER".equals(a.getAuthority()));
    }

    private String sanitizeFolder(String folder) {
        if (folder == null || folder.isBlank()) return null;
        String trimmed = folder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        if (!trimmed.matches(FOLDER_PATTERN)) return null;
        return trimmed;
    }
}
