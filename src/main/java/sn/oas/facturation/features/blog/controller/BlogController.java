package sn.oas.facturation.features.blog.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.oas.facturation.features.blog.data.entity.BlogComment;
import sn.oas.facturation.features.blog.data.entity.BlogPost;
import sn.oas.facturation.features.blog.data.enums.ReactionType;
import sn.oas.facturation.features.blog.dto.BlogCommentRequest;
import sn.oas.facturation.features.blog.dto.BlogPostRequest;
import sn.oas.facturation.features.blog.service.BlogPostService;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
@RequiredArgsConstructor
@Tag(name = "Blog", description = "Gestion du blog et des commentaires")
public class BlogController {

    private final BlogPostService blogPostService;

    @GetMapping
    @Operation(summary = "Lister les articles du blog")
    public ResponseEntity<List<BlogPost>> getAllPosts() {
        return ResponseEntity.ok(blogPostService.getAllPosts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un article du blog")
    public ResponseEntity<BlogPost> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(blogPostService.getPostById(id));
    }

    @PostMapping
    @Operation(summary = "Créer un article de blog (admin)")
    public ResponseEntity<BlogPost> createPost(@RequestBody BlogPostRequest request) {
        return ResponseEntity.ok(blogPostService.createPost(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un article de blog (admin)")
    public ResponseEntity<BlogPost> updatePost(@PathVariable Long id, @RequestBody BlogPostRequest request) {
        return ResponseEntity.ok(blogPostService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un article de blog (admin)")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{postId}/comments")
    @Operation(summary = "Ajouter un commentaire à un article (visiteur ou admin)")
    public ResponseEntity<BlogComment> addComment(@PathVariable Long postId, @RequestBody BlogCommentRequest request) {
        return ResponseEntity.ok(blogPostService.addComment(postId, request));
    }

    @PostMapping("/comments/{commentId}/reply")
    @Operation(summary = "Répondre à un commentaire existant")
    public ResponseEntity<BlogComment> replyToComment(@PathVariable Long commentId,
            @RequestBody BlogCommentRequest request) {
        return ResponseEntity.ok(blogPostService.replyToComment(commentId, request));
    }

    @PostMapping("/comments/{commentId}/react")
    @Operation(summary = "Réagir à un commentaire avec like ou dislike")
    public ResponseEntity<BlogComment> reactToComment(@PathVariable Long commentId,
            @RequestParam ReactionType reactionType) {
        return ResponseEntity.ok(blogPostService.reactToComment(commentId, reactionType));
    }
}
