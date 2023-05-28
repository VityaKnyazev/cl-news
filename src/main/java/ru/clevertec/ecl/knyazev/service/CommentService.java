package ru.clevertec.ecl.knyazev.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface CommentService extends Service<CommentDTO>{
	
	public List<Comment> showAllByNewsId(Long newsId, Pageable pageable) throws ServiceException;
	
}
