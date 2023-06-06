package ru.clevertec.ecl.knyazev.service;

import org.springframework.data.domain.Pageable;

import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface NewsService extends Service<NewsDTO> {
	
	/**
	 * 
	 * Show NewsDTO news by Long id with pageable comments
	 * 
	 * @param id news id for showing
	 * @param commentsPageable Pageable object for pagination and sorting comments in news
	 * @return NewsDTO news with pageable and sorted comments
	 * @throws ServiceException if nothing found
	 * 
	 */
	NewsDTO showById(Long id, Pageable commentsPageable) throws ServiceException;
	
}
