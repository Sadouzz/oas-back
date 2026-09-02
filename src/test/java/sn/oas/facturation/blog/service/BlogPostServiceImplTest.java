package sn.oas.facturation.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sn.oas.facturation.features.blog.data.entity.BlogComment;
import sn.oas.facturation.features.blog.data.entity.BlogPost;
import sn.oas.facturation.features.blog.data.enums.ReactionType;
import sn.oas.facturation.features.blog.dto.BlogCommentRequest;
import sn.oas.facturation.features.blog.repository.BlogCommentRepository;
import sn.oas.facturation.features.blog.repository.BlogPostRepository;
import sn.oas.facturation.features.blog.service.BlogPostServiceImpl;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceImplTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private BlogCommentRepository blogCommentRepository;

    @InjectMocks
    private BlogPostServiceImpl blogPostService;

    @Test
    void addComment_shouldCreateNewCommentAndSetAdminFlag() {
        BlogPost post = BlogPost.builder()
                .id(1L)
                .title("Titre")
                .metaDescription("Meta")
                .description("Description")
                .datePublication(LocalDateTime.now())
                .build();
        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(blogCommentRepository.save(any(BlogComment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogComment result = blogPostService.addComment(1L,
                new BlogCommentRequest("Très bien", "Alice", "alice@example.com", null, false));

        assertThat(result.getContent()).isEqualTo("Très bien");
        assertThat(result.getAuthorName()).isEqualTo("Alice");
        assertThat(result.isAdmin()).isFalse();
        verify(blogCommentRepository).save(any(BlogComment.class));
    }

    @Test
    void reactToComment_shouldIncrementLikeCount() {
        BlogComment comment = BlogComment.builder()
                .id(2L)
                .content("Salut")
                .likes(0)
                .dislikes(0)
                .build();
        when(blogCommentRepository.findById(2L)).thenReturn(Optional.of(comment));
        when(blogCommentRepository.save(any(BlogComment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        BlogComment result = blogPostService.reactToComment(2L, ReactionType.LIKE);

        assertThat(result.getLikes()).isEqualTo(1);
        assertThat(result.getDislikes()).isZero();
    }
}
