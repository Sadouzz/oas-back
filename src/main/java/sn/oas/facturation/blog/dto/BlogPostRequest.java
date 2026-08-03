package sn.oas.facturation.blog.dto;

import java.time.LocalDateTime;

public record BlogPostRequest(
        String title,
        String metaDescription,
        LocalDateTime datePublication,
        String description,
        String images) {
}
