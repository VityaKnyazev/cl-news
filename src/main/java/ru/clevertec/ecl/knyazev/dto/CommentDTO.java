package ru.clevertec.ecl.knyazev.dto;

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

@Schema(description = "Comment DTO - comment information")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(value = Include.NON_NULL)
public class CommentDTO {
	
	@Schema(description = "Identifier")
	@Positive(message = "Message id must be equals to or above 1")
	private Long id;

	@Schema(description = "Creation date")
	private String createDate;
	
	@Schema(description = "Comment text")
	@Size(min = 3, max = 800, message = "Comment title must contains from 3 to 800 symbols")
	private String text;
	
	@Schema(description = "User name")
	@Size(min = 3, max = 35, message = "Comment user name must contains from 3 to 35 symbols")
	private String userName;
	
	@Schema(description = "News DTO - news information")
	private NewsDTO newsDTO;
	
}
