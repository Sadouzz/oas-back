package sn.oas.facturation.features.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import sn.oas.facturation.features.user.data.entity.Agent;
import sn.oas.facturation.shared.exception.BadRequestException;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;

@Service
public class AgentSignatureService {
    private static final String PREFIX = "v1:";
    private static final int NONCE_SIZE = 12;
    private static final int TAG_SIZE_BITS = 128;
    private static final int MAX_SIGNATURE_LENGTH = 1_500_000;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final String encryptionKey;

    public AgentSignatureService(@Value("${app.security.agent-signature-encryption-key:}") String encryptionKey) {
        this.encryptionKey = encryptionKey;
    }

    public boolean isConfigured(Agent agent) {
        return agent.getSignatureFicheAtelierChiffree() != null && !agent.getSignatureFicheAtelierChiffree().isBlank();
    }

    public String read(Agent agent) {
        String encrypted = agent.getSignatureFicheAtelierChiffree();
        return encrypted == null || encrypted.isBlank() ? null : decrypt(encrypted);
    }

    public void save(Agent agent, String signature) {
        validate(signature);
        agent.setSignatureFicheAtelierChiffree(encrypt(signature));
    }

    public void clear(Agent agent) {
        agent.setSignatureFicheAtelierChiffree(null);
    }

    private static void validate(String signature) {
        if (signature == null || signature.length() > MAX_SIGNATURE_LENGTH
                || !signature.matches("^data:image/png;base64,[A-Za-z0-9+/]+={0,2}$")) {
            throw new BadRequestException("La signature doit être une image PNG valide.");
        }
        try {
            byte[] bytes = Base64.getDecoder().decode(signature.substring("data:image/png;base64,".length()));
            if (bytes.length < 8 || bytes[0] != (byte) 0x89 || bytes[1] != 0x50 || bytes[2] != 0x4e || bytes[3] != 0x47) {
                throw new BadRequestException("Le fichier transmis n'est pas une image PNG valide.");
            }
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("La signature PNG est illisible.");
        }
    }

    private String encrypt(String value) {
        try {
            byte[] nonce = new byte[NONCE_SIZE];
            RANDOM.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key(), new GCMParameterSpec(TAG_SIZE_BITS, nonce));
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return PREFIX + Base64.getEncoder().encodeToString(ByteBuffer.allocate(nonce.length + encrypted.length)
                    .put(nonce).put(encrypted).array());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Impossible de chiffrer la signature de l'agent.", e);
        }
    }

    private String decrypt(String value) {
        if (!value.startsWith(PREFIX)) throw new IllegalStateException("Format de signature chiffrée inconnu.");
        try {
            byte[] payload = Base64.getDecoder().decode(value.substring(PREFIX.length()));
            if (payload.length <= NONCE_SIZE) throw new IllegalStateException("Signature chiffrée invalide.");
            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] nonce = new byte[NONCE_SIZE];
            buffer.get(nonce);
            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key(), new GCMParameterSpec(TAG_SIZE_BITS, nonce));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Impossible de déchiffrer la signature de l'agent.", e);
        }
    }

    private SecretKeySpec key() {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptionKey);
            if (decoded.length != 32) throw new IllegalStateException("AGENT_SIGNATURE_ENCRYPTION_KEY doit contenir une clé Base64 de 32 octets.");
            return new SecretKeySpec(decoded, "AES");
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("AGENT_SIGNATURE_ENCRYPTION_KEY doit être encodée en Base64.", e);
        }
    }
}
