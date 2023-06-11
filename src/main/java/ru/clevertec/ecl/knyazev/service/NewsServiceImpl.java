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
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.entity.News;
import ru.clevertec.ecl.knyazev.mapper.NewsMapper;
import ru.clevertec.ecl.knyazev.repository.NewsRepository;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@Service
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired } )
@Slf4j
public class NewsServiceImpl implements NewsService {
	
	private static final String FINDING_ERROR = "Not found";
	private static final String ADDING_ERROR = "Error on adding news";
	private static final String CHANGING_ERROR = "Error on changing news";
	private static final String REMOVING_ERROR = "Error on removing news";
	
	private NewsMapper newsMapperImpl;
	
	private NewsRepository newsRepository;
	
	private CommentService commentServiceImpl;

	@Override
	@Transactional(readOnly = true)
	public NewsDTO showById(Long id) throws ServiceException {
		
		News newsDB = newsRepository.findById(id).orElseThrow(() -> {
			log.error("Error news with id ={} was not found", id);
			return new ServiceException(FINDING_ERROR);
		});
		
		return newsMapperImpl.toNewsDTOWithoutComments(newsDB);
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public NewsDTO showById(Long id, Pageable commentsPageable) throws ServiceException {
		
		NewsDTO newsDTO = showById(id);
		
		List<CommentDTO> commentsDTO = commentServiceImpl.showAllByNewsId(id, commentsPageable);
		
		newsDTO.setComments(commentsDTO.isEmpty() ? null : commentsDTO);
		
		return newsDTO;
	}

	@Override
	@Transactional(readOnly = true)
	public List<NewsDTO> showAll(Pageable pageable) throws ServiceException {
		
		List<News> news = newsRepository.findAll(pageable).toList();
		
		if (news.isEmpty()) {
			log.error("Error. Can't find news on given page={} and pagesize={}", pageable.getPageNumber(), pageable.getPageSize());
			throw new ServiceException(FINDING_ERROR);
		} else {
			return newsMapperImpl.toNewsDTOsWithoutComments(news);
		}
		
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<NewsDTO> showAllOrByTextPart(String textPart, Pageable pageable) throws ServiceException {
		
		List<NewsDTO> newsDTO = new ArrayList<>();
		
		if (textPart != null && !textPart.isBlank()) {
			textPart = "%" + textPart + "%";
			
			List<News> news = newsRepository.findAllByPartNewsText(textPart, pageable);
			
			if (news.isEmpty()) {
				log.error("Error. Can't find news on given text part={}, page={} and pagesize={}", textPart, pageable.getPageNumber(), pageable.getPageSize());
				throw new ServiceException(FINDING_ERROR);
			} else {
				newsDTO = newsMapperImpl.toNewsDTOsWithoutComments(news);
			}
			
		} else {
			newsDTO = showAll(pageable);
		}
		
		return newsDTO;
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public NewsDTO add(NewsDTO newsDTO) throws ServiceException {

		if (newsDTO == null || newsDTO.getId() != null) {
			log.error("Error saving on null news or not null news id");
			throw new ServiceException(ADDING_ERROR);
		}
		
		try {
			News savingNews = newsMapperImpl.toNews(newsDTO);
			savingNews.setTime(LocalDateTime.now());
			
			News savedNews = newsRepository.save(savingNews);
			
			return newsMapperImpl.toNewsDTO(savedNews);
		} catch (DataAccessException e) {
			log.error("Error when adding news: {}", e.getMessage(), e);
			throw new ServiceException(ADDING_ERROR);
		}
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public NewsDTO change(NewsDTO newsDTO) throws ServiceException {
		
		if (newsDTO == null || newsDTO.getId() == null || newsDTO.getId() < 1L) {
			log.error("Error changing on null news or null news id or invalid news id");
			throw new ServiceException(CHANGING_ERROR);
		}
		
		try {
			News dbNews = newsRepository.findById(newsDTO.getId()).orElseThrow(() -> new ServiceException(CHANGING_ERROR));
			News changingNews = newsMapperImpl.toNews(newsDTO);
			
			String changingNewsTitle = changingNews.getTitle();
			String changingNewsText = changingNews.getText();
			
			if (changingNewsTitle != null) {
				dbNews.setTitle(changingNewsTitle);
			}
			
			if (changingNewsText != null) {
				dbNews.setText(changingNewsText);
			}
			
			return newsMapperImpl.toNewsDTOWithoutComments(newsRepository.save(dbNews));
			
		} catch (DataAccessException e) {
			log.error("Error when changing news: {}", e.getMessage(), e);
			throw new ServiceException(CHANGING_ERROR);
		}
		
	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public void remove(NewsDTO newsDTO) throws ServiceException {
				
		if (newsDTO == null || newsDTO.getId() == null || newsDTO.getId() < 1L) {
			log.error("Error removing on null news or null news id or invalid news id");
			throw new ServiceException(REMOVING_ERROR);
		}
		
		try {
			News dbNews = newsRepository.findById(newsDTO.getId()).orElseThrow(() -> new ServiceException(REMOVING_ERROR));
			
			newsRepository.delete(dbNews);
			
		} catch (DataAccessException e) {
			log.error("Error when removing news: {}", e.getMessage(), e);
			throw new ServiceException(REMOVING_ERROR);
		}
		
	}	

}
