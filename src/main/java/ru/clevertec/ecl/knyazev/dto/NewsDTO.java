package ru.clevertec.ecl.knyazev.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "News DTO - news information")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(value = Include.NON_NULL)
public class NewsDTO {
	
	@Schema(description = "Identifier")
	@Positive(message = "News id must be equals to or above 1")
	private Long id;

	@Schema(description = "Date of news creation")
	private String createDate;
	
	@Schema(description = "News title")
	@Size(min = 3, max = 100, message = "News title must contains from 3 to 100 symbols")
	private String title;
	
	@Schema(description = "News text")
	@Size(min = 3, message = "Error, news text must contains at least 3 symbols")
	private String text;
	
	@Schema(description = "News author name")
	@Size(min = 3, max = 40, message = "Error, news author name must contains from 3 to 40 symbols")
	private String authorName;
	
	@Schema(description = "List of comments DTO - comments information")
	private List<CommentDTO> comments;
	
}
