package sn.oas.facturation.features.blog.dto;

import java.time.LocalDateTime;

public record BlogPostRequest(
        String title,
        String metaDescription,
        LocalDateTime datePublication,
        String description,
        String images,
        String category,
        String readTime,
        Boolean featured) {
}
