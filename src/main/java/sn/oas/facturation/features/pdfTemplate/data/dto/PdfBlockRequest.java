package sn.oas.facturation.features.pdfTemplate.data.dto;
public record PdfBlockRequest(String id, String kind, String content, String align, String width,
                              Integer fontSize, String color, String backgroundColor, String columnWidths,
                              String tableStyle, String verticalAlign, Integer padding, boolean headerRow, String dataSource,
                              String positionMode, Integer offsetX, Integer offsetY) {
    public PdfBlockRequest(String id, String kind, String content, String align, String width) {
        this(id, kind, content, align, width, null, null, null, null, null, null, null, false, null, "flow", null, null);
    }

    public PdfBlockRequest(String id, String kind, String content, String align, String width,
                           Integer fontSize, String color, String backgroundColor, String columnWidths,
                           String tableStyle, String verticalAlign, Integer padding, boolean headerRow, String dataSource) {
        this(id, kind, content, align, width, fontSize, color, backgroundColor, columnWidths, tableStyle,
                verticalAlign, padding, headerRow, dataSource, "flow", null, null);
    }
}
