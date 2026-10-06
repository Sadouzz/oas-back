package sn.oas.facturation.features.pdfTemplate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.features.pdfTemplate.data.dto.PdfBlockRequest;
import sn.oas.facturation.features.pdfTemplate.data.dto.PdfTemplateResponse;
import sn.oas.facturation.features.pdfTemplate.data.dto.PdfTemplateWriteRequest;
import sn.oas.facturation.features.pdfTemplate.data.entity.PdfTemplate;
import sn.oas.facturation.features.pdfTemplate.repository.PdfTemplateRepository;
import sn.oas.facturation.features.user.data.entity.User;
import sn.oas.facturation.shared.exception.BadRequestException;
import sn.oas.facturation.shared.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PdfTemplateService {
    private final PdfTemplateRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<PdfTemplateResponse> list(String type) {
        List<PdfTemplate> templates = type == null || type.isBlank() ? repository.findAllByOrderByDocumentTypeAscNameAscVersionDesc()
                : repository.findAssignedToDocumentType(normalizeType(type));
        return templates.stream().map(this::response).toList();
    }

    @Transactional
    public PdfTemplateResponse create(PdfTemplateWriteRequest request, User user) {
        validate(request);
        Set<String> types = normalizeDocumentTypes(request);
        String type = types.contains("FACTURE") ? "FACTURE" : normalizeType(request.documentType());
        String layout = normalizeLayout(types.contains("FACTURE") ? "FACTURE" : type, request.layoutKey());
        int version = repository.findFirstByDocumentTypeAndNameOrderByVersionDesc(type, request.name().trim())
                .map(t -> t.getVersion() + 1).orElse(1);
        return persist(request, user, type, layout, version);
    }

    @Transactional
    public PdfTemplateResponse revise(Long id, PdfTemplateWriteRequest request, User user) {
        PdfTemplate previous = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modèle PDF introuvable."));
        validate(request);
        Set<String> types = normalizeDocumentTypes(request);
        String type = types.contains("FACTURE") ? "FACTURE" : normalizeType(request.documentType());
        String layout = normalizeLayout(types.contains("FACTURE") ? "FACTURE" : type, request.layoutKey());
        if (!previous.getName().equals(request.name().trim())) throw new BadRequestException("Une nouvelle version doit conserver le nom du modèle.");
        int version = Math.max(previous.getVersion() + 1, repository.findFirstByDocumentTypeAndNameOrderByVersionDesc(type, request.name().trim())
                .map(t -> t.getVersion() + 1).orElse(previous.getVersion() + 1));
        if (request.active()) previous.setActive(false);
        return persist(request, user, type, layout, version);
    }

    @Transactional
    public PdfTemplateResponse activate(Long id) {
        PdfTemplate selected = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modèle PDF introuvable."));
        if (assignedTypes(selected).contains("FACTURE")) repository.deactivateFamily("FACTURE", selected.getName(), normalizeLayout("FACTURE", selected.getLayoutKey()));
        selected.setActive(true);
        return response(repository.save(selected));
    }

    @Transactional
    public PdfTemplateResponse deactivate(Long id) {
        PdfTemplate selected = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modèle PDF introuvable."));
        selected.setActive(false);
        return response(repository.save(selected));
    }

    @Transactional(readOnly = true)
    public String renderIfConfigured(String type, Map<String, String> tokens) {
        PdfTemplate template = repository.findAssignedToDocumentType(normalizeType(type)).stream().filter(PdfTemplate::isActive).findFirst().orElse(null);
        if (template == null) return null;
        return render(template, tokens);
    }

    @Transactional(readOnly = true)
    public String renderIfConfigured(String type, String layoutKey, Map<String, String> tokens) {
        String normalizedType = normalizeType(type);
        PdfTemplate template = repository.findActiveForLayoutByVersion(normalizedType, normalizeLayout(normalizedType, layoutKey)).stream().findFirst().orElse(null);
        return template == null ? null : render(template, tokens);
    }

    @Transactional(readOnly = true)
    public String renderById(Long id, Map<String, String> tokens) {
        return renderById(id, tokens, Map.of());
    }

    @Transactional(readOnly = true)
    public String renderById(Long id, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        PdfTemplate template = repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Modèle PDF introuvable."));
        return render(template, tokens, tableData);
    }

    @Transactional(readOnly = true)
    public String renderPreview(List<PdfBlockRequest> blocks, Map<String, String> tokens) {
        return renderPreview(blocks, tokens, Map.of());
    }

    @Transactional(readOnly = true)
    public String renderPreview(List<PdfBlockRequest> blocks, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        validateBlocks(blocks);
        return renderBlocks(blocks, tokens, tableData);
    }

    @Transactional(readOnly = true)
    public String renderPreview(String documentType, List<PdfBlockRequest> blocks, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        validate(new PdfTemplateWriteRequest("Aperçu", documentType, false, blocks));
        return renderBlocks(blocks, tokens, tableData);
    }

    @Transactional(readOnly = true)
    public List<PdfTemplateResponse> activeInvoiceTemplates(String layoutKey) {
        return repository.findActiveForLayout("FACTURE", normalizeLayout("FACTURE", layoutKey))
                .stream().map(this::response).toList();
    }

    private String render(PdfTemplate template, Map<String, String> tokens) {
        return render(template, tokens, Map.of());
    }

    private String render(PdfTemplate template, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        try {
            List<PdfBlockRequest> blocks = objectMapper.readValue(template.getBlocksJson(), new TypeReference<>() {});
            return renderBlocks(blocks, tokens, tableData);
        } catch (Exception e) {
            throw new BadRequestException("Le modèle PDF actif est invalide.");
        }
    }

    private String renderBlocks(List<PdfBlockRequest> blocks, Map<String, String> tokens) {
        return renderBlocks(blocks, tokens, Map.of());
    }

    private String renderBlocks(List<PdfBlockRequest> blocks, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        StringBuilder html = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?><html xmlns=\"http://www.w3.org/1999/xhtml\"><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/><style>@page{size:A4;margin:12mm}body{position:relative;min-height:273mm;font-family:Arial,sans-serif;font-size:12px;color:#1f2937;margin:0}h1{font-size:24px}h2{font-size:18px}p{line-height:1.5}.center{text-align:center}.right{text-align:right}.divider{border:0;border-top:1px solid #d1d5db;margin:14px 0}img{max-width:100%;height:auto}table{page-break-inside:auto}tr{page-break-inside:avoid}td,th{overflow-wrap:anywhere}</style></head><body>");
        for (PdfBlockRequest block : blocks) appendBlock(html, block, tokens, tableData);
        return html.append("</body></html>").toString();
    }

    private PdfTemplateResponse persist(PdfTemplateWriteRequest request, User user, String type, String layout, int version) {
        if (request.active() && typesForRequest(request).contains("FACTURE")) repository.deactivateFamily("FACTURE", request.name().trim(), layout);
        try {
            PdfTemplate saved = repository.save(PdfTemplate.builder().name(request.name().trim()).documentType(type).layoutKey(layout).version(version)
                    .assignedDocumentTypes(new LinkedHashSet<>(typesForRequest(request)))
                    .active(request.active()).blocksJson(objectMapper.writeValueAsString(request.blocks())).createdBy(user).build());
            return response(saved);
        } catch (JsonProcessingException e) { throw new BadRequestException("Impossible d'enregistrer le contenu du modèle."); }
    }

    private PdfTemplateResponse response(PdfTemplate template) {
        try { return PdfTemplateResponse.from(template, objectMapper.readValue(template.getBlocksJson(), new TypeReference<>() {})); }
        catch (JsonProcessingException e) { throw new BadRequestException("Contenu de modèle illisible."); }
    }

    private void validate(PdfTemplateWriteRequest request) {
        validateBlocks(request.blocks());
        Set<String> documentTypes = normalizeDocumentTypes(request);
        if (!documentTypes.equals(Set.of("FACTURE")) && request.blocks().stream().anyMatch(block -> block.dataSource() != null && !block.dataSource().isBlank())) throw new BadRequestException("Les tableaux dynamiques pièces/main-d’œuvre sont réservés aux modèles affectés uniquement aux factures.");
    }

    private List<String> typesForRequest(PdfTemplateWriteRequest request) { return List.copyOf(normalizeDocumentTypes(request)); }

    private Set<String> normalizeDocumentTypes(PdfTemplateWriteRequest request) {
        List<String> requested = request.documentTypes() == null || request.documentTypes().isEmpty()
                ? List.of(request.documentType()) : request.documentTypes();
        Set<String> normalized = new LinkedHashSet<>();
        for (String type : requested) {
            if (type == null || type.isBlank()) throw new BadRequestException("Sélectionnez au moins un type de document.");
            String value = normalizeType(type);
            if (!List.of("FACTURE", "PROFORMA", "DEVIS_PREVISIONNEL", "BON_COMMANDE", "BON_RECEPTION", "AVOIR_HT", "AVOIR_TTC", "DIAGNOSTIC").contains(value)) {
                throw new BadRequestException("Type de document non pris en charge : " + value);
            }
            normalized.add(value);
        }
        if (normalized.isEmpty()) throw new BadRequestException("Sélectionnez au moins un type de document.");
        if (!normalized.contains(normalizeType(request.documentType()))) throw new BadRequestException("Le type principal doit faire partie des types associés.");
        return normalized;
    }

    private Set<String> assignedTypes(PdfTemplate template) {
        return template.getAssignedDocumentTypes() == null || template.getAssignedDocumentTypes().isEmpty()
                ? Set.of(template.getDocumentType()) : Set.copyOf(template.getAssignedDocumentTypes());
    }

    private void validateBlocks(List<PdfBlockRequest> blocks) {
        if (blocks != null && blocks.size() > 100) throw new BadRequestException("Un modèle ne peut pas contenir plus de 100 blocs.");
        if (blocks == null) throw new BadRequestException("La liste des blocs du modèle est obligatoire.");
        long totalContentLength = 0;
        for (PdfBlockRequest block : blocks) {
            if (block == null || !List.of("heading", "text", "image", "divider", "table").contains(block.kind())) throw new BadRequestException("Type de bloc PDF non pris en charge.");
            if (block.content() != null && block.content().length() > 2_800_000) throw new BadRequestException("Un contenu de bloc dépasse la limite autorisée.");
            totalContentLength += block.content() == null ? 0 : block.content().length();
            if ("image".equals(block.kind()) && block.content() != null && !block.content().isBlank()
                    && !block.content().matches("^https://res\\.cloudinary\\.com/[A-Za-z0-9_-]+/image/upload/[^\\s<>\"']+$")) throw new BadRequestException("Les images doivent être enregistrées sur Cloudinary.");
            if (block.fontSize() != null && (block.fontSize() < 6 || block.fontSize() > 72)) throw new BadRequestException("La taille du texte doit être comprise entre 6 et 72 pt.");
            if (block.padding() != null && (block.padding() < 0 || block.padding() > 48)) throw new BadRequestException("Le remplissage du bloc doit être compris entre 0 et 48 px.");
            String positionMode = block.positionMode() == null ? "flow" : block.positionMode();
            if (!List.of("flow", "absolute").contains(positionMode)) throw new BadRequestException("Mode de positionnement d'image invalide.");
            if (block.offsetX() != null && (block.offsetX() < 0 || block.offsetX() > 100)) throw new BadRequestException("La position horizontale doit être comprise entre 0 et 100 %.");
            if (block.offsetY() != null && (block.offsetY() < 0 || block.offsetY() > 100)) throw new BadRequestException("La position verticale doit être comprise entre 0 et 100 %.");
            if ("absolute".equals(positionMode) && (!"image".equals(block.kind()) || block.offsetX() == null || block.offsetY() == null)) throw new BadRequestException("Le positionnement libre exige une image et des coordonnées X/Y.");
            if (block.tableStyle() != null && !List.of("grid", "horizontal", "minimal").contains(block.tableStyle())) throw new BadRequestException("Style de tableau invalide.");
            if (block.columnWidths() != null && block.columnWidths().length() > 200) throw new BadRequestException("La configuration des colonnes est trop volumineuse.");
            if ("table".equals(block.kind())) validateTable(block);
        }
        if (totalContentLength > 8_000_000) throw new BadRequestException("Le modèle dépasse la taille totale autorisée de 8 Mo.");
    }

    private void appendBlock(StringBuilder html, PdfBlockRequest block, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        String align = switch (block.align() == null ? "left" : block.align()) { case "center" -> "center"; case "right" -> "right"; default -> "left"; };
        String content = interpolate(block.content(), tokens);
        switch (block.kind()) {
            case "heading" -> html.append("<h2 style=\"").append(styleFor(block, align)).append("\">").append(escape(content)).append("</h2>");
            case "text" -> html.append("<p style=\"").append(styleFor(block, align)).append("\">").append(escape(content).replace("\n", "<br/>")).append("</p>");
            case "table" -> appendTable(html, block, tokens, tableData);
            case "divider" -> html.append("<hr class=\"divider\"/>");
            case "image" -> {
                if (isCloudinaryImage(block.content()) || isLegacyEmbeddedImage(block.content())) {
                    boolean absolute = "absolute".equals(block.positionMode());
                    html.append("<div class=\"").append(absolute ? "" : align).append("\" style=\"");
                    if (absolute) html.append("position:absolute;left:").append(block.offsetX()).append("%;top:").append(block.offsetY()).append("%;z-index:1;");
                    html.append("width:").append(escapeWidth(block.width())).append(";").append(absolute ? "" : "display:block;").append("\"><img style=\"width:100%;height:auto;object-fit:contain\" src=\"").append(escape(block.content())).append("\"/></div>");
                }
            }
            default -> { }
        }
    }

    private void appendTable(StringBuilder html, PdfBlockRequest block, Map<String, String> tokens, Map<String, List<Map<String, String>>> tableData) {
        List<String> rows = block.content() == null ? List.of() : block.content().lines().toList();
        html.append("<table style=\"width:").append(escapeWidth(block.width())).append(";border-collapse:collapse;table-layout:fixed\"><tbody>");
        String[] widths = block.columnWidths() == null || block.columnWidths().isBlank() ? new String[0] : block.columnWidths().split(",");
        String tableStyle = block.tableStyle() == null ? "grid" : block.tableStyle();
        boolean dynamic = block.dataSource() != null && !block.dataSource().isBlank();
        List<Map<String, String>> sourceRows = dynamic ? tableData.getOrDefault(block.dataSource(), List.of()) : List.of();
        int firstDataLine = block.headerRow() && !rows.isEmpty() ? 1 : 0;
        List<String> outputRows = new java.util.ArrayList<>();
        if (block.headerRow() && !rows.isEmpty()) outputRows.add(rows.get(0));
        if (dynamic) {
            String rowTemplate = rows.size() > firstDataLine ? rows.get(firstDataLine) : "";
            for (Map<String, String> rowTokens : sourceRows) outputRows.add(rowTemplate);
        } else outputRows.addAll(rows.subList(Math.min(firstDataLine, rows.size()), rows.size()));
        for (int rowIndex = 0; rowIndex < outputRows.size(); rowIndex++) {
            String row = outputRows.get(rowIndex);
            Map<String, String> rowTokens = dynamic && rowIndex >= firstDataLine ? sourceRows.get(rowIndex - firstDataLine) : Map.of();
            String[] cells = row.split("\\t", -1);
            html.append("<tr>");
            for (int i = 0; i < cells.length; i++) {
                String width = widths.length > i && isPercent(widths[i].trim()) ? widths[i].trim() : "auto";
                boolean header = block.headerRow() && rowIndex == 0;
                String border = switch (tableStyle) { case "horizontal" -> "border-bottom:1px solid #9ca3af;"; case "minimal" -> ""; default -> "border:1px solid #9ca3af;"; };
                String verticalValue = block.verticalAlign();
                String vertical = "top".equals(verticalValue) || "middle".equals(verticalValue) || "bottom".equals(verticalValue) ? verticalValue : "middle";
                Map<String, String> cellTokens = new java.util.HashMap<>(tokens == null ? Map.of() : tokens);
                cellTokens.putAll(rowTokens);
                html.append(header ? "<th" : "<td").append(" style=\"").append(border).append("padding:").append(block.padding() == null ? 5 : block.padding()).append("px;width:").append(width).append(";vertical-align:").append(vertical).append(";text-align:").append("right".equals(block.align()) ? "right" : "left").append(";").append(header && "grid".equals(tableStyle) ? "background-color:#e5e7eb;font-weight:bold;" : "").append(validColor(block.color()) ? "color:" + block.color() + ";" : "").append(validColor(block.backgroundColor()) ? "background-color:" + block.backgroundColor() + ";" : "").append("\">").append(escape(interpolate(cells[i], cellTokens))).append(header ? "</th>" : "</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
    }

    private void validateTable(PdfBlockRequest block) {
        if (block.dataSource() != null && !block.dataSource().isBlank() && !List.of("LIGNES_PIECES", "LIGNES_MAIN_DOEUVRE").contains(block.dataSource())) {
            throw new BadRequestException("Source de tableau non prise en charge.");
        }
        if (block.content() == null || block.content().isBlank()) {
            if (block.dataSource() != null && !block.dataSource().isBlank()) throw new BadRequestException("Un tableau dynamique doit contenir ses colonnes et son modèle de ligne.");
            return;
        }
        List<String> rows = block.content().lines().toList();
        int columns = rows.get(0).split("\\t", -1).length;
        if (columns > 50 || rows.stream().anyMatch(row -> row.split("\\t", -1).length != columns)) {
            throw new BadRequestException("Les lignes d'un tableau doivent avoir le même nombre de colonnes (maximum 50).");
        }
        if (block.columnWidths() != null && !block.columnWidths().isBlank()) {
            String[] widths = block.columnWidths().split(",");
            if (widths.length != columns || java.util.Arrays.stream(widths).anyMatch(width -> !isPercent(width.trim()))) {
                throw new BadRequestException("Indiquez une largeur en pourcentage valide pour chaque colonne du tableau.");
            }
        }
        if (block.dataSource() != null && !block.dataSource().isBlank()) {
            int requiredRows = block.headerRow() ? 2 : 1;
            if (rows.size() < requiredRows) throw new BadRequestException("Un tableau dynamique doit contenir une ligne modèle" + (block.headerRow() ? " après son en-tête." : "."));
        }
    }

    private String styleFor(PdfBlockRequest block, String align) {
        return "text-align:" + align + ";" + (block.fontSize() == null ? "" : "font-size:" + block.fontSize() + "pt;")
                + (validColor(block.color()) ? "color:" + block.color() + ";" : "")
                + (validColor(block.backgroundColor()) ? "background-color:" + block.backgroundColor() + ";" : "")
                + (block.padding() == null ? "" : "padding:" + block.padding() + "px;");
    }

    private boolean validColor(String color) { return color != null && color.matches("#[0-9A-Fa-f]{6}"); }
    private boolean isPercent(String value) { return value != null && value.matches("(?:100|[1-9][0-9]?)%"); }
    private boolean isCloudinaryImage(String url) { return url != null && url.matches("^https://res\\.cloudinary\\.com/[A-Za-z0-9_-]+/image/upload/[^\\s<>\"']+$"); }
    private boolean isLegacyEmbeddedImage(String content) { return content != null && content.matches("^data:image/(png|jpeg);base64,[A-Za-z0-9+/=]+$"); }

    private String normalizeLayout(String type, String layout) {
        if (!"FACTURE".equals(type)) return "STANDARD";
        if (layout == null || layout.isBlank() || "STANDARD".equalsIgnoreCase(layout)) return "AVEC_ENTETE";
        String normalized = normalizeType(layout);
        if (!List.of("AVEC_ENTETE", "SANS_ENTETE").contains(normalized)) throw new BadRequestException("Variante de facture invalide.");
        return normalized;
    }
    private String interpolate(String text, Map<String, String> tokens) {
        if (text == null || tokens == null) return text == null ? "" : text;
        String result = text;
        for (var token : tokens.entrySet()) result = result.replace("{{" + token.getKey() + "}}", token.getValue() == null ? "" : token.getValue());
        return result;
    }
    private String escape(String text) { return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;"); }
    private String escapeWidth(String width) { return isPercent(width) ? width : "100%"; }
    private String normalizeType(String type) { return type.trim().toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9_-]", ""); }
}
