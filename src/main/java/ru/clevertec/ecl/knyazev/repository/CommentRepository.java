package ru.clevertec.ecl.knyazev.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.clevertec.ecl.knyazev.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {
	
	List<Comment> findAllByNewsId(Long newsId, Pageable pageable);

	@Query(value = "SELECT c FROM Comment c WHERE c.text LIKE ?1")
	List<Comment> findAllByPartCommentText(String textPart, Pageable pageable);
	
}
