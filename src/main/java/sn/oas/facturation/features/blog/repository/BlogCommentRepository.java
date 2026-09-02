package sn.oas.facturation.features.blog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.oas.facturation.features.blog.data.entity.BlogComment;

@Repository
public interface BlogCommentRepository extends JpaRepository<BlogComment, Long> {
}
