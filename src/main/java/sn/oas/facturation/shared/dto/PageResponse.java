package sn.oas.facturation.shared.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> {

    private List<T> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isFirst;
    private boolean isLast;
    private boolean hasNext;
    private boolean hasPrev;
    private List<Integer> pages;

    /**
     * Convertit un objet org.springframework.data.domain.Page en PageResponse standardisé.
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        if (page == null) {
            return PageResponse.<T>builder()
                    .content(Collections.emptyList())
                    .currentPage(0)
                    .pageSize(0)
                    .totalElements(0)
                    .totalPages(0)
                    .isFirst(true)
                    .isLast(true)
                    .hasNext(false)
                    .hasPrev(false)
                    .pages(Collections.emptyList())
                    .build();
        }

        int totalPages = page.getTotalPages();
        List<Integer> pagesList = totalPages > 0
                ? IntStream.range(0, totalPages).boxed().toList()
                : Collections.emptyList();

        return PageResponse.<T>builder()
                .content(page.getContent())
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(totalPages)
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrev(page.hasPrevious())
                .pages(pagesList)
                .build();
    }
}
