package ru.clevertec.ecl.knyazev.dto;

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
public class CommentDTO {
	
	@Positive(message = "Message id must be equals to or above 1")
	private Long id;

	private String createDate;
	
	@NotBlank(message = "Error, comment text is invalid")
	@Size(min = 3, max = 800, message = "Comment title must contains from 3 to 800 symbols")
	private String text;
	
	@NotBlank(message = "Error, user name is invalid")
	@Size(min = 3, max = 800, message = "Comment user name must contains from 3 to 35 symbols")
	private String userName;
	
	private NewsDTO newsDTO;
	
}
