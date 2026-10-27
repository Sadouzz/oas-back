package sn.oas.facturation.features.ordreTravail.data.enums;

public enum TypeDocumentOt {
    PHOTO,
    PDF,
    DOCUMENT,
    AUTRE;

    public static TypeDocumentOt fromString(String val) {
        if (val == null || val.isBlank()) {
            return DOCUMENT;
        }
        String upper = val.trim().toUpperCase();
        return switch (upper) {
            case "PHOTO", "IMAGE", "JPG", "JPEG", "PNG", "WEBP", "GIF" -> PHOTO;
            case "PDF" -> PDF;
            case "DOC", "DOCX", "DOCUMENT", "WORD", "TXT", "XLS", "XLSX" -> DOCUMENT;
            default -> {
                for (TypeDocumentOt t : values()) {
                    if (t.name().equalsIgnoreCase(upper)) {
                        yield t;
                    }
                }
                yield AUTRE;
            }
        };
    }
}

