package ru.clevertec.ecl.knyazev.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ru.clevertec.ecl.knyazev.entity.News;

public interface NewsRepository extends JpaRepository<News, Long> {

}
