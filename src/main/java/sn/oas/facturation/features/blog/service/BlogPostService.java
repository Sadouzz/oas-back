package sn.oas.facturation.features.blog.service;

import sn.oas.facturation.features.blog.data.entity.BlogComment;
import sn.oas.facturation.features.blog.data.entity.BlogPost;
import sn.oas.facturation.features.blog.data.enums.ReactionType;
import sn.oas.facturation.features.blog.dto.BlogCommentRequest;
import sn.oas.facturation.features.blog.dto.BlogPostRequest;

import java.util.List;

public interface BlogPostService {
    BlogPost createPost(BlogPostRequest request);

    BlogPost updatePost(Long id, BlogPostRequest request);

    void deletePost(Long id);

    List<BlogPost> getAllPosts();

    BlogPost getPostById(Long id);

    BlogComment addComment(Long postId, BlogCommentRequest request);

    BlogComment replyToComment(Long commentId, BlogCommentRequest request);

    BlogComment reactToComment(Long commentId, ReactionType reactionType);
}
