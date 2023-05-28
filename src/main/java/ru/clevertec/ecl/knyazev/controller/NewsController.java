package ru.clevertec.ecl.knyazev.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.service.NewsService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@RestController
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired })
@Validated
public class NewsController {
	
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_PAGE_SIZE = 3;
	
	private NewsService newsServiceImpl;
	
	@GetMapping(value = "/news/{id}")
	public ResponseEntity<?> getNews(@PathVariable 
			                         @Positive(message = "News id must be greater than or equals to 1")
	                                 Long id,
	                                 @PageableDefault(page = DEFAULT_PAGE, size = DEFAULT_PAGE_SIZE)
									 @SortDefault(sort = "time")
									 Pageable pageable) {		
		
		try {
			NewsDTO newsDTO = newsServiceImpl.show(id, pageable);
			return ResponseEntity.ok().body(newsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@GetMapping(value = "/news")
	public ResponseEntity<?> getNews(@RequestParam(required = false, name = "text_part") @Size(min = 3, max = 100, message = "text part must be above or equals to 3 and less than or equals to 100 symbols") String textPart,
									 @PageableDefault(page = DEFAULT_PAGE, size = DEFAULT_PAGE_SIZE) 
	                                 @SortDefault(sort = "title") 
	                                 Pageable pageable) {		
		
		try {
			List<NewsDTO> newsDTO = newsServiceImpl.showAllByTextPart(textPart, pageable);
			return ResponseEntity.ok().body(newsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PostMapping(value = "/news")
	public ResponseEntity<?> addNews(@Valid @RequestBody NewsDTO newsDTO) {
		
		try {
			NewsDTO savedNewsDTO = newsServiceImpl.add(newsDTO);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(savedNewsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PutMapping("/news")
	public ResponseEntity<?> changeGiftCertificate(@Valid @RequestBody NewsDTO newsDTO) {
		
		try {
			NewsDTO updatedNewsDTO = newsServiceImpl.change(newsDTO);
			return ResponseEntity.ok().body(updatedNewsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@DeleteMapping("/news")
	public ResponseEntity<?> removeNews(@Valid @RequestBody NewsDTO newsDTO) {

		try {
			newsServiceImpl.remove(newsDTO);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (ServiceException e) {
			return ResponseEntity.notFound().build();
		}
		
	}
}
