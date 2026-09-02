package sn.oas.facturation.features.blog.data.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String authorName;

    @Column(nullable = true)
    private String authorEmail;

    @Column(nullable = false)
    @Builder.Default
    private boolean admin = false;

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    @Column(nullable = false)
    @Builder.Default
    private int dislikes = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BlogPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BlogComment parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BlogComment> replies = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime dateCreation;
}
