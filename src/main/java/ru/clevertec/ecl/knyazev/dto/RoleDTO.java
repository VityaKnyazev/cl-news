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
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@JsonInclude(value = Include.NON_NULL)
public class RoleDTO {

	@Positive(message = "Role id must be equals to or above 1")
	private Long id;

	@Size(min = 3, max = 20, message = "Role name must contains from 3 to 20 symbols")
	private String name;

	private UserDTO userDTO;

}
