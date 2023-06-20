package ru.clevertec.ecl.knyazev.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.validation.constraints.Email;
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
public class UserDTO {

	@Positive(message = "User id must be equals to or above 1")
	private Long id;

	@Size(min = 3, max = 30, message = "User name must contains from 3 to 30 symbols")
	private String name;

	@Size(min = 3, max = 68, message = "User password must contains 68 symbols")
	private String password;

	@Email(message = "Invalid user email")
	private String email;

	private Boolean enabled;

	private List<RoleDTO> rolesDTO;

}
