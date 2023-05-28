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
import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.service.CommentService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@RestController
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired })
@Validated
public class CommentController {
	
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_PAGE_SIZE = 3;
	
	private CommentService commentServiceImpl;
	
	@GetMapping(value = "/comments/{id}")
	public ResponseEntity<?> getComment(@PathVariable 
			                         @Positive(message = "Comment id must be greater than or equals to 1") 
	                                 Long id) {		
		
		try {
			CommentDTO commentDTO = commentServiceImpl.show(id);
			return ResponseEntity.ok().body(commentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@GetMapping(value = "/comments")
	public ResponseEntity<?> getComments(@RequestParam(required = false, name = "text_part") @Size(min = 3, max = 100, 
	                                                   message = "text part must be above or equals to 3 and less than or equals to 100 symbols") String textPart,
									     @RequestParam(required = false, name = "news_id") @Positive(message = "news id must be above or equals to 1") Long newsId,
									     @PageableDefault(page = DEFAULT_PAGE, size = DEFAULT_PAGE_SIZE) 
	                                     @SortDefault(sort = "time") 
	                                     Pageable pageable) {		
		
		try {
			List<CommentDTO> commentDTO = commentServiceImpl.showAllByRequestParams(newsId, textPart, pageable);
			return ResponseEntity.ok().body(commentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PostMapping(value = "/comments")
	public ResponseEntity<?> addComment(@Valid @RequestBody CommentDTO commentDTO) {
		
		try {
			CommentDTO savedCommentDTO = commentServiceImpl.add(commentDTO);
			return ResponseEntity.status(HttpStatus.CREATED)
					             .body(savedCommentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PutMapping("/comments")
	public ResponseEntity<?> changeComment(@Valid @RequestBody CommentDTO commentDTO) {
		
		try {
			CommentDTO updatedCommentDTO = commentServiceImpl.change(commentDTO);
			return ResponseEntity.ok().body(updatedCommentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@DeleteMapping("/comments")
	public ResponseEntity<?> removeComment(@Valid @RequestBody CommentDTO commentDTO) {

		try {
			commentServiceImpl.remove(commentDTO);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (ServiceException e) {
			return ResponseEntity.notFound().build();
		}
		
	}
}
