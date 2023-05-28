package ru.clevertec.ecl.knyazev.service;

import org.springframework.data.domain.Pageable;

import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface NewsService extends Service<NewsDTO> {
	
	NewsDTO show(Long id, Pageable commentsPageable) throws ServiceException;
	
}
