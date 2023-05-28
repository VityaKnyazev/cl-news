package ru.clevertec.ecl.knyazev.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import ru.clevertec.ecl.knyazev.entity.News;

public interface NewsRepository extends JpaRepository<News, Long> {
	
	@Query(value = "SELECT n FROM News n WHERE n.text LIKE ?1")
	List<News> findByPartNewsText(String partNewsText, Pageable pageable);
	
}
