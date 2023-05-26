package ru.clevertec.ecl.knyazev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.service.NewsService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@RestController
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = {@Autowired})
@Validated
public class NewsController {
	
	private NewsService newsServiceImpl;
	
	@GetMapping(value = "/news/{id}")
	public ResponseEntity<?> getNews(@PathVariable @Positive(message = "News id must be greater than or equals to 1") Long id) {		
		
		try {
			NewsDTO newsDTO = newsServiceImpl.show(id);
			return ResponseEntity.ok().body(newsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
}
