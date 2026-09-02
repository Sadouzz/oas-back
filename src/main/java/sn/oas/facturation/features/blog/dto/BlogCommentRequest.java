package sn.oas.facturation.features.blog.dto;

public record BlogCommentRequest(
        String content,
        String authorName,
        String authorEmail,
        Long parentId,
        boolean admin) {
}
