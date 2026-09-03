package sn.oas.facturation.features.blog.dto;

import lombok.Builder;
import sn.oas.facturation.features.blog.data.entity.BlogPost;

import java.time.LocalDateTime;

@Builder
public record BlogPostListResponse(
        Long id,
        String title,
        String metaDescription,
        LocalDateTime datePublication,
        String description,
        String images,
        String category,
        String readTime,
        Boolean featured,
        int commentsCount
) {
    public static BlogPostListResponse from(BlogPost p) {
        if (p == null) return null;
        return BlogPostListResponse.builder()
                .id(p.getId())
                .title(p.getTitle())
                .metaDescription(p.getMetaDescription())
                .datePublication(p.getDatePublication())
                .description(p.getDescription())
                .images(p.getImages())
                .category(p.getCategory())
                .readTime(p.getReadTime())
                .featured(p.getFeatured())
                .commentsCount(p.getComments() != null ? p.getComments().size() : 0)
                .build();
    }
}
