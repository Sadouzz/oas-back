package sn.oas.facturation.media;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sn.oas.facturation.media.dto.MediaSignatureResponse;

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
    @Operation(summary = "Génère une signature d'upload Cloudinary de courte durée pour un dossier donné")
    public ResponseEntity<?> getSignature(@RequestParam(required = false) String folder) {
        String safeFolder = sanitizeFolder(folder);
        if (folder != null && safeFolder == null) {
            return ResponseEntity.badRequest().body("Dossier invalide.");
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

    private String sanitizeFolder(String folder) {
        if (folder == null || folder.isBlank()) return null;
        String trimmed = folder.trim().replaceAll("^/+", "").replaceAll("/+$", "");
        if (!trimmed.matches(FOLDER_PATTERN)) return null;
        return trimmed;
    }
}
