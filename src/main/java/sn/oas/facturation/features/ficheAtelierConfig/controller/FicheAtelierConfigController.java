package sn.oas.facturation.features.ficheAtelierConfig.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.ficheAtelier.data.entity.FicheAtelier;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneDefaut;
import sn.oas.facturation.features.ficheAtelier.data.entity.LigneReception;
import sn.oas.facturation.features.ficheAtelier.repository.FicheAtelierRepository;
import sn.oas.facturation.features.ficheAtelierConfig.data.entity.FicheAtelierConfig;
import sn.oas.facturation.features.ficheAtelierConfig.repository.FicheAtelierConfigRepository;
import sn.oas.facturation.features.garage.data.entity.Garage;
import sn.oas.facturation.features.garage.repository.GarageRepository;
import sn.oas.facturation.shared.documentNumber.DocumentNumberGeneratorService;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/fiche-atelier-configs")
@RequiredArgsConstructor
public class FicheAtelierConfigController {

    private final FicheAtelierConfigRepository repository;
    private final GarageRepository garageRepository;
    private final DocumentNumberGeneratorService documentNumberGeneratorService;
    private final FicheAtelierRepository ficheAtelierRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public static final String DEFAULT_CONFIG_JSON = """
            {
              "lignesReception": [
                { "nom": "Carrosserie", "etat": null, "archive": false },
                { "nom": "Intérieur / Habitacle", "etat": null, "archive": false },
                { "nom": "Vitrage / Pare-brise", "etat": null, "archive": false },
                { "nom": "Eclairage", "etat": null, "archive": false },
                { "nom": "Accessoires (Cric, roue de secours...)", "etat": null, "archive": false }
              ],
              "defautsConstates": [
                { "nom": "Mécanique", "designation": "", "archive": false },
                { "nom": "Électrique", "designation": "", "archive": false },
                { "nom": "Climatisation", "designation": "", "archive": false },
                { "nom": "Peinture", "designation": "", "archive": false },
                { "nom": "Tôlerie", "designation": "", "archive": false },
                { "nom": "Vidange", "designation": "", "archive": false }
              ],
              "rubriquesDefauts": [
                { "nom": "Mécanique", "designation": "", "archive": false },
                { "nom": "Électrique", "designation": "", "archive": false },
                { "nom": "Climatisation", "designation": "", "archive": false },
                { "nom": "Peinture", "designation": "", "archive": false },
                { "nom": "Tôlerie", "designation": "", "archive": false },
                { "nom": "Vidange", "designation": "", "archive": false }
              ],
              "lignesDefauts": [
                { "nom": "Mécanique", "designation": "", "archive": false },
                { "nom": "Électrique", "designation": "", "archive": false },
                { "nom": "Climatisation", "designation": "", "archive": false },
                { "nom": "Peinture", "designation": "", "archive": false },
                { "nom": "Tôlerie", "designation": "", "archive": false },
                { "nom": "Vidange", "designation": "", "archive": false }
              ],
              "defautsCarrosserie": [
                { "nom": "Mécanique", "designation": "", "archive": false },
                { "nom": "Électrique", "designation": "", "archive": false },
                { "nom": "Climatisation", "designation": "", "archive": false },
                { "nom": "Peinture", "designation": "", "archive": false },
                { "nom": "Tôlerie", "designation": "", "archive": false },
                { "nom": "Vidange", "designation": "", "archive": false }
              ]
            }
            """.trim();

    @GetMapping
    public ResponseEntity<List<FicheAtelierConfig>> getAll() {
        Garage garage = resolveGarage();
        List<FicheAtelierConfig> configs = (garage != null)
                ? repository.findByGarageId(garage.getId()).map(List::of).orElseGet(List::of)
                : repository.findAll();

        if (configs.isEmpty()) {
            FicheAtelierConfig defaultConfig = getOrCreateDefaultConfig(garage);
            return ResponseEntity.ok(List.of(enrichConfig(defaultConfig)));
        }
        return ResponseEntity.ok(configs.stream().map(this::enrichConfig).collect(Collectors.toList()));
    }

    @GetMapping("/current")
    public ResponseEntity<FicheAtelierConfig> getCurrent() {
        Garage garage = resolveGarage();
        FicheAtelierConfig config = (garage != null)
                ? repository.findByGarageId(garage.getId()).orElseGet(() -> getOrCreateDefaultConfig(garage))
                : repository.findFirstByOrderByIdAsc().orElseGet(() -> getOrCreateDefaultConfig(garage));
        return ResponseEntity.ok(enrichConfig(config));
    }

    @GetMapping({"/used-items", "/usage", "/ordres-usage"})
    public ResponseEntity<Map<String, Object>> getUsedItems() {
        return ResponseEntity.ok(Map.of(
                "reception", getUsedReceptionNames(),
                "defauts", getUsedDefautNames(),
                "receptionCounts", getUsedReceptionCount(),
                "defautsCounts", getUsedDefautsCount(),
                "ordresUsage", Collections.emptyMap()
        ));
    }

    @PostMapping
    public ResponseEntity<FicheAtelierConfig> create(@RequestBody(required = false) String rawBody) {
        Garage garage = resolveGarage();
        JsonNode payload = parseJson(rawBody);
        String jsonToSave = extractAndProcessConfigJson(payload, garage != null ? repository.findByGarageId(garage.getId()).orElse(null) : null);

        FicheAtelierConfig config;
        if (garage != null) {
            var existing = repository.findByGarageId(garage.getId());
            if (existing.isPresent()) {
                config = existing.get();
                config.setConfigJson(jsonToSave);
                return ResponseEntity.ok(enrichConfig(repository.save(config)));
            }
        }

        config = FicheAtelierConfig.builder()
                .garage(garage)
                .configJson(jsonToSave)
                .build();
        return ResponseEntity.ok(enrichConfig(repository.save(config)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<FicheAtelierConfig> update(@PathVariable Long id, @RequestBody(required = false) String rawBody) {
        FicheAtelierConfig existing = repository.findById(id).orElse(null);
        JsonNode payload = parseJson(rawBody);
        String jsonToSave = extractAndProcessConfigJson(payload, existing);

        if (existing != null) {
            existing.setConfigJson(jsonToSave);
            return ResponseEntity.ok(enrichConfig(repository.save(existing)));
        } else {
            Garage garage = resolveGarage();
            FicheAtelierConfig entity = FicheAtelierConfig.builder()
                    .id(id)
                    .garage(garage)
                    .configJson(jsonToSave)
                    .build();
            return ResponseEntity.ok(enrichConfig(repository.save(entity)));
        }
    }

    private JsonNode parseJson(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return objectMapper.readTree(raw);
        } catch (Exception e) {
            log.warn("Erreur parsing JSON rawBody: {}", e.getMessage());
            return null;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private Garage resolveGarage() {
        try {
            Garage g = documentNumberGeneratorService.getCurrentGarage();
            if (g != null) return g;
        } catch (Exception ignored) {}
        return garageRepository.findAll().stream().findFirst().orElse(null);
    }

    private FicheAtelierConfig getOrCreateDefaultConfig(Garage garage) {
        if (garage == null) {
            garage = resolveGarage();
        }
        if (garage != null) {
            var existing = repository.findByGarageId(garage.getId());
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        FicheAtelierConfig config = FicheAtelierConfig.builder()
                .garage(garage)
                .configJson(DEFAULT_CONFIG_JSON)
                .build();
        return repository.save(config);
    }

    private String normalizeKey(String s) {
        if (s == null) return "";
        String normalized = Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "");
    }

    private Map<String, Long> getUsedReceptionCount() {
        Map<String, Long> counts = new HashMap<>();
        try {
            List<FicheAtelier> fiches = ficheAtelierRepository.findAll();
            for (FicheAtelier f : fiches) {
                if (f.getLignesReception() != null) {
                    for (LigneReception lr : f.getLignesReception()) {
                        if (lr.getNom() != null && !lr.getNom().isBlank()) {
                            String k = normalizeKey(lr.getNom());
                            counts.put(k, counts.getOrDefault(k, 0L) + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Erreur comptage used reception: {}", e.getMessage());
        }
        return counts;
    }

    private Map<String, Long> getUsedDefautsCount() {
        Map<String, Long> counts = new HashMap<>();
        try {
            List<FicheAtelier> fiches = ficheAtelierRepository.findAll();
            for (FicheAtelier f : fiches) {
                if (f.getLignesDefauts() != null) {
                    for (LigneDefaut ld : f.getLignesDefauts()) {
                        if (ld.getNom() != null && !ld.getNom().isBlank()) {
                            String k = normalizeKey(ld.getNom());
                            counts.put(k, counts.getOrDefault(k, 0L) + 1);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Erreur comptage used defauts: {}", e.getMessage());
        }
        return counts;
    }

    private Set<String> getUsedReceptionNames() {
        return getUsedReceptionCount().keySet();
    }

    private Set<String> getUsedDefautNames() {
        return getUsedDefautsCount().keySet();
    }

    private FicheAtelierConfig enrichConfig(FicheAtelierConfig config) {
        if (config == null || config.getConfigJson() == null || config.getConfigJson().isBlank()) {
            return config;
        }
        try {
            JsonNode rootNode = objectMapper.readTree(config.getConfigJson());
            if (!rootNode.isObject()) {
                return config;
            }
            ObjectNode root = (ObjectNode) rootNode;
            Map<String, Long> usedReceptionCounts = getUsedReceptionCount();
            Map<String, Long> usedDefautsCounts = getUsedDefautsCount();

            // Enrich lignesReception
            if (root.has("lignesReception") && root.get("lignesReception").isArray()) {
                ArrayNode arr = (ArrayNode) root.get("lignesReception");
                ArrayNode enrichedArr = objectMapper.createArrayNode();
                for (JsonNode item : arr) {
                    enrichedArr.add(normalizeReceptionNode(item, usedReceptionCounts));
                }
                root.set("lignesReception", enrichedArr);
            }

            // Enrich defautsConstates and related keys
            String[] defautKeys = {"defautsConstates", "rubriquesDefauts", "lignesDefauts", "defautsCarrosserie"};
            for (String key : defautKeys) {
                if (root.has(key) && root.get(key).isArray()) {
                    ArrayNode arr = (ArrayNode) root.get(key);
                    ArrayNode enrichedArr = objectMapper.createArrayNode();
                    for (JsonNode item : arr) {
                        enrichedArr.add(normalizeDefautNode(item, usedDefautsCounts));
                    }
                    root.set(key, enrichedArr);
                }
            }

            config.setConfigJson(root.toString());
        } catch (Exception e) {
            log.warn("Impossible d'enrichir la config atelier: {}", e.getMessage());
        }
        return config;
    }

    private ObjectNode normalizeReceptionNode(JsonNode item, Map<String, Long> usedCounts) {
        ObjectNode obj = objectMapper.createObjectNode();
        String nom = item.isTextual() ? item.asText() : (item.hasNonNull("nom") ? item.get("nom").asText() : "");
        if (item.isObject()) {
            obj.setAll((ObjectNode) item);
        } else {
            obj.put("nom", nom);
            obj.putNull("etat");
            obj.put("archive", false);
        }
        if (!obj.has("archive")) {
            obj.put("archive", false);
        }

        String k = normalizeKey(nom);
        long count = usedCounts.getOrDefault(k, 0L);
        boolean utilise = count > 0;

        obj.put("utilise", utilise);
        obj.put("utilisee", utilise);
        obj.put("estUtilise", utilise);
        obj.put("estUtilisee", utilise);
        obj.put("used", utilise);
        obj.put("isUsed", utilise);
        obj.put("nbUtilisations", count);
        obj.put("utilisations", count);
        obj.put("count", count);
        obj.put("usageCount", count);
        obj.put("enUtilisation", utilise);
        obj.put("canDelete", !utilise);
        obj.put("supprimable", !utilise);
        obj.put("deletable", !utilise);
        obj.put("verrouille", utilise);
        obj.put("locked", utilise);

        boolean isArchive = obj.get("archive").asBoolean(false);
        obj.put("active", !isArchive);
        obj.put("actif", !isArchive);

        return obj;
    }

    private ObjectNode normalizeDefautNode(JsonNode item, Map<String, Long> usedCounts) {
        ObjectNode obj = objectMapper.createObjectNode();
        String nom = item.isTextual() ? item.asText() : (item.hasNonNull("nom") ? item.get("nom").asText() : "");
        if (item.isObject()) {
            obj.setAll((ObjectNode) item);
        } else {
            obj.put("nom", nom);
            obj.put("designation", "");
            obj.put("archive", false);
        }
        if (!obj.has("archive")) {
            obj.put("archive", false);
        }
        if (!obj.has("designation")) {
            obj.put("designation", "");
        }

        String k = normalizeKey(nom);
        long count = usedCounts.getOrDefault(k, 0L);
        boolean utilise = count > 0;

        obj.put("utilise", utilise);
        obj.put("utilisee", utilise);
        obj.put("estUtilise", utilise);
        obj.put("estUtilisee", utilise);
        obj.put("used", utilise);
        obj.put("isUsed", utilise);
        obj.put("nbUtilisations", count);
        obj.put("utilisations", count);
        obj.put("count", count);
        obj.put("usageCount", count);
        obj.put("enUtilisation", utilise);
        obj.put("canDelete", !utilise);
        obj.put("supprimable", !utilise);
        obj.put("deletable", !utilise);
        obj.put("verrouille", utilise);
        obj.put("locked", utilise);

        boolean isArchive = obj.get("archive").asBoolean(false);
        obj.put("active", !isArchive);
        obj.put("actif", !isArchive);

        return obj;
    }

    private String extractAndProcessConfigJson(JsonNode payload, FicheAtelierConfig existingConfig) {
        if (payload == null) {
            return DEFAULT_CONFIG_JSON;
        }

        JsonNode targetNode;
        if (payload.has("configJson")) {
            JsonNode cj = payload.get("configJson");
            if (cj.isTextual()) {
                try {
                    targetNode = objectMapper.readTree(cj.asText());
                } catch (Exception e) {
                    return cj.asText();
                }
            } else {
                targetNode = cj;
            }
        } else {
            targetNode = payload;
        }

        if (!targetNode.isObject()) {
            return targetNode.toString();
        }

        ObjectNode target = (ObjectNode) targetNode;
        Set<String> usedReception = getUsedReceptionNames();
        Set<String> usedDefauts = getUsedDefautNames();

        if (existingConfig != null && existingConfig.getConfigJson() != null) {
            try {
                JsonNode oldTree = objectMapper.readTree(existingConfig.getConfigJson());
                preserveArchivedOrUsedReception(oldTree, target, usedReception);
                preserveArchivedOrUsedDefauts(oldTree, target, usedDefauts);
            } catch (Exception e) {
                log.warn("Erreur conservation items archivés: {}", e.getMessage());
            }
        }

        return target.toString();
    }

    private void preserveArchivedOrUsedReception(JsonNode oldTree, ObjectNode target, Set<String> usedReception) {
        if (!oldTree.has("lignesReception") || !oldTree.get("lignesReception").isArray()) return;
        if (!target.has("lignesReception") || !target.get("lignesReception").isArray()) return;

        ArrayNode targetArr = (ArrayNode) target.get("lignesReception");
        Set<String> targetKeys = new HashSet<>();
        for (JsonNode n : targetArr) {
            String name = n.isTextual() ? n.asText() : (n.hasNonNull("nom") ? n.get("nom").asText() : "");
            if (!name.isBlank()) targetKeys.add(normalizeKey(name));
        }

        ArrayNode oldArr = (ArrayNode) oldTree.get("lignesReception");
        for (JsonNode oldNode : oldArr) {
            String oldName = oldNode.isTextual() ? oldNode.asText() : (oldNode.hasNonNull("nom") ? oldNode.get("nom").asText() : "");
            if (oldName.isBlank()) continue;
            String k = normalizeKey(oldName);

            if (usedReception.contains(k) && !targetKeys.contains(k)) {
                ObjectNode archivedItem = objectMapper.createObjectNode();
                archivedItem.put("nom", oldName);
                archivedItem.putNull("etat");
                archivedItem.put("archive", true);
                archivedItem.put("utilise", true);
                archivedItem.put("utilisee", true);
                archivedItem.put("canDelete", false);
                targetArr.add(archivedItem);
                targetKeys.add(k);
            }
        }
    }

    private void preserveArchivedOrUsedDefauts(JsonNode oldTree, ObjectNode target, Set<String> usedDefauts) {
        String key = "defautsConstates";
        if (!oldTree.has(key) || !oldTree.get(key).isArray()) return;
        if (!target.has(key) || !target.get(key).isArray()) return;

        ArrayNode targetArr = (ArrayNode) target.get(key);
        Set<String> targetKeys = new HashSet<>();
        for (JsonNode n : targetArr) {
            String name = n.isTextual() ? n.asText() : (n.hasNonNull("nom") ? n.get("nom").asText() : "");
            if (!name.isBlank()) targetKeys.add(normalizeKey(name));
        }

        ArrayNode oldArr = (ArrayNode) oldTree.get(key);
        for (JsonNode oldNode : oldArr) {
            String oldName = oldNode.isTextual() ? oldNode.asText() : (oldNode.hasNonNull("nom") ? oldNode.get("nom").asText() : "");
            if (oldName.isBlank()) continue;
            String k = normalizeKey(oldName);

            if (usedDefauts.contains(k) && !targetKeys.contains(k)) {
                ObjectNode archivedItem = objectMapper.createObjectNode();
                archivedItem.put("nom", oldName);
                archivedItem.put("designation", "");
                archivedItem.put("archive", true);
                archivedItem.put("utilise", true);
                archivedItem.put("utilisee", true);
                archivedItem.put("canDelete", false);
                targetArr.add(archivedItem);
                targetKeys.add(k);
            }
        }
    }
}
