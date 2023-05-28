package ru.clevertec.ecl.knyazev.dto;

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
public class CommentDTO {
	
	@Positive(message = "Message id must be equals to or above 1")
	private Long id;

	private String createDate;
	
	@Size(min = 3, max = 800, message = "Comment title must contains from 3 to 800 symbols")
	private String text;
	
	@Size(min = 3, max = 35, message = "Comment user name must contains from 3 to 35 symbols")
	private String userName;
	
	private NewsDTO newsDTO;
	
}
