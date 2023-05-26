package ru.clevertec.ecl.knyazev.service;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.dto.mapper.NewsMapper;
import ru.clevertec.ecl.knyazev.entity.News;
import ru.clevertec.ecl.knyazev.repository.NewsRepository;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@Service
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired } )
@Log4j2
public class NewsServiceImpl implements NewsService {
	
	private static final String FINDING_ERROR = "Not found";
	private static final String ADDING_ERROR = "Error on adding news";
	private static final String CHANGING_ERROR = "Error on changing news";
	private static final String REMOVING_ERROR = "Error on removing news";
	
	private NewsMapper newsMapperImpl;
	
	private NewsRepository newsRepository;

	@Override
	public NewsDTO show(Long id) throws ServiceException {
		
		if (id == null) {
			log.error("Error searching news on null id");
			throw new ServiceException(FINDING_ERROR);
		}
		
		Optional<News> newsWrap = newsRepository.findById(id);
		
		if (newsWrap.isEmpty()) {
			log.error("Error news with id ={} was not found", id);
			throw new ServiceException(FINDING_ERROR);
		} else {
			return newsMapperImpl.toNewsDTO(newsWrap.get());
		}
		
	}

	@Override
	public List<NewsDTO> showAll(Pageable pageable) throws ServiceException {
		return null;
	}

	@Override
	public NewsDTO add(NewsDTO t) throws ServiceException {
		return null;
	}

	@Override
	public NewsDTO change(NewsDTO t) throws ServiceException {
		return null;
	}

	@Override
	public void remove(NewsDTO t) throws ServiceException {
		
	}	

}
