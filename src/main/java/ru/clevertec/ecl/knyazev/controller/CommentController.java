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
import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.service.CommentService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;


@RestController
@Tag(name = "Comments", description = "Show, add, change, remove comments")
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired })
@Validated
public class CommentController {
	
	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_PAGE_SIZE = 3;
	
	private CommentService commentServiceImpl;
	
	@GetMapping(value = "/comments/{id}")
	@Operation(description = "Show comment by id")
	public ResponseEntity<?> getComment(@Parameter(description = "Comment id")
										@PathVariable
										@NotNull(message = "Comment id must must be no null")
			                         	@Positive(message = "Comment id must be greater than or equals to 1") 
	                                 	Long id) {		
		
		try {
			CommentDTO commentDTO = commentServiceImpl.showById(id);
			return ResponseEntity.ok().body(commentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@GetMapping(value = "/comments")
	@Operation(description = "Show all comments or show all comments on part of text value")	
	public ResponseEntity<?> getAllComments(@Parameter(description = "Searching by part of text in comments", required = false)
			                                @RequestParam(required = false, name = "text_part") 
			                                @Size(min = 3, max = 100, 
	                                                   message = "text part must be above or equals to 3 and less than or equals to 100 symbols") 
											String textPart,
			                                @Parameter(description = "Pageable param for page, size and sorting", required = false)
			                                @PageableDefault(page = DEFAULT_PAGE, size = DEFAULT_PAGE_SIZE) 
	                                        @SortDefault(sort = "time") 
	                                        Pageable pageable) {		
		
		try {
			List<CommentDTO> commentDTO = commentServiceImpl.showAllOrByTextPart(textPart, pageable);
			return ResponseEntity.ok().body(commentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PostMapping(value = "/comments")
	@Operation(description = "Add comment")	
	public ResponseEntity<?> addComment(@Parameter(description = "Comment dto for adding")
										@Valid 
										@RequestBody 
										CommentDTO commentDTO) {
		
		try {
			CommentDTO savedCommentDTO = commentServiceImpl.add(commentDTO);
			return ResponseEntity.status(HttpStatus.CREATED)
					             .body(savedCommentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@PutMapping("/comments")
	@Operation(description = "Change comment")	
	public ResponseEntity<?> changeComment(@Parameter(description = "Comment dto for changing")
										   @Valid 
										   @RequestBody 
										   CommentDTO commentDTO) {
		
		try {
			CommentDTO updatedCommentDTO = commentServiceImpl.change(commentDTO);
			return ResponseEntity.ok().body(updatedCommentDTO);
		} catch (ServiceException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
		
	}
	
	@DeleteMapping("/comments")
	@Operation(description = "Remove comment")	
	public ResponseEntity<?> removeComment(@Parameter(description = "Comment dto for removing")
										   @Valid 
										   @RequestBody 
										   CommentDTO commentDTO) {

		try {
			commentServiceImpl.remove(commentDTO);
			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
		} catch (ServiceException e) {
			return ResponseEntity.notFound().build();
		}
		
	}
	
}
