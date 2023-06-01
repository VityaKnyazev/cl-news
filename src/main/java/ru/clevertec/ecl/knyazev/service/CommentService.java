package ru.clevertec.ecl.knyazev.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface CommentService extends Service<CommentDTO>{
	
	/**
	 * 
	 * Show all comments by news id with pagination and sorting
	 * 
	 * @param newsId news id
	 * @param pageable Pageable object for comments pagination and sorting
	 * @return List<Comment> comments on news id
	 * @throws ServiceException on invalid news id or when nothing found
	 * 
	 */
	public List<Comment> showAllByNewsId(Long newsId, Pageable pageable) throws ServiceException;
	
}
