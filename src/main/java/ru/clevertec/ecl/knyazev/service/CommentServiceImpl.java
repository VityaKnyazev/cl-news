package ru.clevertec.ecl.knyazev.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;
import ru.clevertec.ecl.knyazev.mapper.CommentMapper;
import ru.clevertec.ecl.knyazev.repository.CommentRepository;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@Service
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired } )
@Slf4j
public class CommentServiceImpl implements CommentService {
	
	private static final String FINDING_ERROR = "Not found";
	private static final String ADDING_ERROR = "Error on adding comment";
	private static final String CHANGING_ERROR = "Error on changing comment";
	private static final String REMOVING_ERROR = "Error on removing comment";
	
	private static final String USER_NAME_ERROR = "Current user didn't create current comment or user name has been changed";
	
	private CommentMapper commentMapperImpl;
	
	private CommentRepository commentRepository;

	@Override
	@Transactional(readOnly = true)
	public CommentDTO showById(Long id) throws ServiceException {
				
		Comment commentDB = commentRepository.findById(id).orElseThrow(() -> {
			log.error("Error comment with id ={} was not found", id);
			return new ServiceException(FINDING_ERROR);
		});
		
		return commentMapperImpl.toCommentDTO(commentDB);	
		
	}

	@Override
	@Transactional(readOnly = true)
	public List<CommentDTO> showAll(Pageable pageable) throws ServiceException {
		
		List<Comment> comments = commentRepository.findAll(pageable).toList();
		
		if (comments.isEmpty()) {
			log.error("Error. Can't find comments on given page={} and pagesize={}", pageable.getPageNumber(), pageable.getPageSize());
			throw new ServiceException(FINDING_ERROR);
		} else {
			return commentMapperImpl.toCommentsDTO(comments);
		}
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentDTO> showAllOrByTextPart(String textPart, Pageable pageable) throws ServiceException {
		
		List<CommentDTO> commentsDTO = new ArrayList<>();
		
		if (textPart != null && !textPart.isBlank()) {
			textPart = "%" + textPart + "%";
			
			List<Comment> comments = commentRepository.findAllByPartCommentText(textPart, pageable);
			
			if (comments.isEmpty()) {
				log.error("Error. Can't find comments on given text part={}, page={} and pagesize={}", textPart, pageable.getPageNumber(), pageable.getPageSize());
				throw new ServiceException(FINDING_ERROR);
			} else {
				commentsDTO = commentMapperImpl.toCommentsDTO(comments);
			}
			
		} else {
			commentsDTO = showAll(pageable);
		}
		
		return commentsDTO;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<CommentDTO> showAllByNewsId(Long newsId, Pageable pageable) throws ServiceException {

		if (newsId == null) {
			log.error("Error searching comments on null news id");
			throw new ServiceException(FINDING_ERROR);
		}
		
		List<Comment> comments = commentRepository.findAllByNewsId(newsId, pageable);
		
		if (comments.isEmpty()) {
			log.info("Can't find comments on given newsId={}, page={} and pagesize={}", newsId, pageable.getPageNumber(), pageable.getPageSize());
		}

		return commentMapperImpl.toCommentsDTOWithoutNews(comments);		
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public CommentDTO add(CommentDTO commentDTO) throws ServiceException {

		if (commentDTO == null || commentDTO.getId() != null || 
		    commentDTO.getNewsDTO() == null || commentDTO.getNewsDTO().getId() == null ||
		    commentDTO.getNewsDTO().getId() < 1L) {
			log.error("Error adding comment on invalid comment id or invalid news id");
			throw new ServiceException(ADDING_ERROR);
		}
		
		try {
			Comment savingComment = commentMapperImpl.toComment(commentDTO);
			savingComment.setTime(LocalDateTime.now());
			
			savingComment.setUserName(SecurityUserService.getSecurityUserName());
			
			Comment savedComment = commentRepository.save(savingComment);
			
			return commentMapperImpl.toCommentDTO(savedComment);
		} catch (DataAccessException e) {
			log.error("Error when adding comment: {}", e.getMessage(), e);
			throw new ServiceException(ADDING_ERROR);
		}
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public CommentDTO change(CommentDTO commentDTO) throws ServiceException {
		
		if (commentDTO == null || commentDTO.getId() == null || commentDTO.getId() < 1L ||
			commentDTO.getNewsDTO() == null) {
			log.error("Error changing comment on invalid comment id or invalid news");
			throw new ServiceException(CHANGING_ERROR);
		}
		
		try {
			Comment dbComment = commentRepository.findById(commentDTO.getId())
					                             .orElseThrow(() -> new ServiceException(CHANGING_ERROR));
			
			if (!isCurrentUserAdminOrRecordCreater(dbComment.getUserName())) {
				log.error(USER_NAME_ERROR);
				throw new ServiceException(CHANGING_ERROR);
			}
			
			Comment changingComment = commentMapperImpl.toComment(commentDTO);
			
			String changingCommentText = changingComment.getText();
			Long changingCommentNewsId = changingComment.getNews().getId();			
					
			if (changingCommentText != null) {
				dbComment.setText(changingCommentText);
			}
			
			if (changingCommentNewsId != null && changingCommentNewsId > 0L) {
				News dbNews = dbComment.getNews();
				dbNews.setId(changingCommentNewsId);
				dbComment.setNews(dbNews);
			}
			
			return commentMapperImpl.toCommentDTO(commentRepository.save(dbComment));
			
		} catch (DataAccessException e) {
			log.error("Error when changing comment: {}", e.getMessage(), e);
			throw new ServiceException(CHANGING_ERROR);
		}
		
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public void remove(CommentDTO commentDTO) throws ServiceException {
				
		if (commentDTO == null || commentDTO.getId() == null || commentDTO.getId() < 1L) {
			log.error("Error removing on invalid comment id");
			throw new ServiceException(REMOVING_ERROR);
		}
		
		try {
			Comment dbComment = commentRepository.findById(commentDTO.getId())
					                             .orElseThrow(() -> new ServiceException(REMOVING_ERROR));
			
			if (!isCurrentUserAdminOrRecordCreater(dbComment.getUserName())) {
				log.error(USER_NAME_ERROR);
				throw new ServiceException(REMOVING_ERROR);
			}
			
			commentRepository.delete(dbComment);
			
		} catch (DataAccessException e) {
			log.error("Error when removing comment: {}", e.getMessage(), e);
			throw new ServiceException(REMOVING_ERROR);
		}
		
	}	

}
