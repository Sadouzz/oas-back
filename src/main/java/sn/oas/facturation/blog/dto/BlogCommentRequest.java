package sn.oas.facturation.blog.dto;

public record BlogCommentRequest(
        String content,
        String authorName,
        String authorEmail,
        Long parentId,
        boolean admin) {
}
