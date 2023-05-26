package ru.clevertec.ecl.knyazev.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class NewsDTO {
	
	@Positive(message = "News id must be equals to or above 1")
	private Long id;

	private String createDate;
	
	@NotBlank(message = "Error news title is invalid")
	@Size(min = 3, max = 100, message = "News title must contains from 3 to 100 symbols")
	private String title;
	
	@NotBlank(message = "Error, news text is invalid")
	private String text;
	
	private List<CommentDTO> comments;
	
}
