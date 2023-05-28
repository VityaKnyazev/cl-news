package ru.clevertec.ecl.knyazev.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface CommentService extends Service<CommentDTO>{
	
	public List<CommentDTO> showAllByNewsId(Long newsId, Pageable pageable) throws ServiceException;
	
	public List<CommentDTO> showAllByRequestParams(Long newsId,
			                                       String textPart, 
			                                       Pageable pageable) throws ServiceException;
	
}
