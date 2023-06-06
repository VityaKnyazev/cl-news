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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.service.NewsService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@RestController
@Tag(name = "News", description = "Show, add, change, remove news")
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired })
@Validated
public class NewsController {
	
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_PAGE_SIZE = 3;
	
	private NewsService newsServiceImpl;
		
	@GetMapping(value = "/news/{id}")
	@Operation(description = "Show news by id")
	public ResponseEntity<?> getNews(@Parameter(description = "News id")
									 @PathVariable 
									 @NotNull(message = "News id must be not null")
			                         @Positive(message = "News id must be greater than or equals to 1")
	                                 Long id,
	                                 @Parameter(description = "Pageable for page, size and sorting comments in news", 
	                                            required = false)
	                                 @PageableDefault(page = DEFAULT_PAGE, size = DEFAULT_PAGE_SIZE)
									 @SortDefault(sort = "time")
									 Pageable pageable) {		
		
		try {
			NewsDTO newsDTO = newsServiceImpl.showById(id, pageable);
			return ResponseEntity.ok().body(newsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@GetMapping(value = "/news")
	@Operation(description = "Show all news or show all news on part of text value")
	public ResponseEntity<?> getAllNews(@Parameter(description = "Searching by part of text in news", required = false)
			                            @RequestParam(required = false, name = "text_part")
			                            @Size(min = 3, max = 100, 
			                                  message = "text part must be above or equals to 3 and less than or equals to 100 symbols") 
			                            String textPart,
			                            @Parameter(description = "Pageable param for page, size and sorting", required = false)
									    @PageableDefault(page = DEFAULT_PAGE, size = DEFAULT_PAGE_SIZE) 
	                                    @SortDefault(sort = "title") 
	                                    Pageable pageable) {		
		
		try {
			List<NewsDTO> newsDTO = newsServiceImpl.showAllOrByTextPart(textPart, pageable);
			return ResponseEntity.ok().body(newsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PostMapping(value = "/news")
	@Operation(description = "Add news")
	public ResponseEntity<?> addNews(@Parameter(description = "News dto for adding")
									 @Valid 
									 @RequestBody 
									 NewsDTO newsDTO) {
		
		try {
			NewsDTO savedNewsDTO = newsServiceImpl.add(newsDTO);
			return ResponseEntity.status(HttpStatus.CREATED)
					.body(savedNewsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PutMapping("/news")
	@Operation(description = "Change news")
	public ResponseEntity<?> changeNews(@Parameter(description = "News DTO for changing")
										@Valid 
										@RequestBody 
										NewsDTO newsDTO) {
		
		try {
			NewsDTO updatedNewsDTO = newsServiceImpl.change(newsDTO);
			return ResponseEntity.ok().body(updatedNewsDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@DeleteMapping("/news")
	@Operation(description = "Remove news")
	public ResponseEntity<?> removeNews(@Parameter(description = "News DTO for removing")
										@Valid 
										@RequestBody 
										NewsDTO newsDTO) {

		try {
			newsServiceImpl.remove(newsDTO);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (ServiceException e) {
			return ResponseEntity.notFound().build();
		}
		
	}
}
