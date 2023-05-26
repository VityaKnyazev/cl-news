package ru.clevertec.ecl.knyazev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.clevertec.ecl.knyazev.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
