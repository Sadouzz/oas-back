package sn.oas.facturation.blog.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.oas.facturation.blog.data.entity.BlogComment;
import sn.oas.facturation.blog.data.entity.BlogPost;
import sn.oas.facturation.blog.data.enums.ReactionType;
import sn.oas.facturation.blog.dto.BlogCommentRequest;
import sn.oas.facturation.blog.dto.BlogPostRequest;
import sn.oas.facturation.blog.repository.BlogCommentRepository;
import sn.oas.facturation.blog.repository.BlogPostRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostServiceImpl implements BlogPostService {

    private final BlogPostRepository blogPostRepository;
    private final BlogCommentRepository blogCommentRepository;

    @Override
    @Transactional
    public BlogPost createPost(BlogPostRequest request) {
        BlogPost post = BlogPost.builder()
                .title(request.title())
                .metaDescription(request.metaDescription())
                .datePublication(request.datePublication() != null ? request.datePublication() : LocalDateTime.now())
                .description(request.description())
                .images(request.images())
                .category(request.category() != null ? request.category() : "Conseils automobiles")
                .readTime(request.readTime() != null ? request.readTime() : "5 min de lecture")
                .featured(request.featured() != null ? request.featured() : false)
                .build();
        return blogPostRepository.save(post);
    }

    @Override
    @Transactional
    public BlogPost updatePost(Long id, BlogPostRequest request) {
        BlogPost post = getPostById(id);
        post.setTitle(request.title());
        post.setMetaDescription(request.metaDescription());
        post.setDatePublication(
                request.datePublication() != null ? request.datePublication() : post.getDatePublication());
        post.setDescription(request.description());
        post.setImages(request.images());
        post.setCategory(request.category() != null ? request.category() : post.getCategory());
        post.setReadTime(request.readTime() != null ? request.readTime() : post.getReadTime());
        post.setFeatured(request.featured() != null ? request.featured() : post.getFeatured());
        return blogPostRepository.save(post);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        BlogPost post = getPostById(id);
        blogPostRepository.delete(post);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPost> getAllPosts() {
        return blogPostRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPost getPostById(Long id) {
        return blogPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article de blog introuvable"));
    }

    @Override
    @Transactional
    public BlogComment addComment(Long postId, BlogCommentRequest request) {
        BlogPost post = getPostById(postId);
        BlogComment comment = BlogComment.builder()
                .content(request.content())
                .authorName(request.authorName())
                .authorEmail(request.authorEmail())
                .admin(request.admin())
                .dateCreation(LocalDateTime.now())
                .post(post)
                .build();
        post.getComments().add(comment);
        return blogCommentRepository.save(comment);
    }

    @Override
    @Transactional
    public BlogComment replyToComment(Long commentId, BlogCommentRequest request) {
        BlogComment parent = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));
        BlogComment reply = BlogComment.builder()
                .content(request.content())
                .authorName(request.authorName())
                .authorEmail(request.authorEmail())
                .admin(request.admin())
                .dateCreation(LocalDateTime.now())
                .post(parent.getPost())
                .parent(parent)
                .build();
        parent.getReplies().add(reply);
        return blogCommentRepository.save(reply);
    }

    @Override
    @Transactional
    public BlogComment reactToComment(Long commentId, ReactionType reactionType) {
        BlogComment comment = blogCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Commentaire introuvable"));
        if (reactionType == ReactionType.LIKE) {
            comment.setLikes(comment.getLikes() + 1);
        } else {
            comment.setDislikes(comment.getDislikes() + 1);
        }
        return blogCommentRepository.save(comment);
    }
}
