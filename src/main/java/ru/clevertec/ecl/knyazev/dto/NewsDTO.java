package ru.clevertec.ecl.knyazev.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(value = Include.NON_NULL)
public class NewsDTO {
	
	@Positive(message = "News id must be equals to or above 1")
	private Long id;

	private String createDate;
	
	@Size(min = 3, max = 100, message = "News title must contains from 3 to 100 symbols")
	private String title;
	
	@Size(min = 3, message = "Error, news text must contains at least 3 symbols")
	private String text;
	
	private List<CommentDTO> comments;
	
}
