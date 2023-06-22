package ru.clevertec.ecl.knyazev.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.service.UserService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;


@RestController
@Tag(name = "Users", description = "signup, register user")
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired })
@Validated
public class UserController {
	
	private UserService userServiceImpl;
	

	@PostMapping(value = "/login")
	@Operation(description = "Authenticate user in application")	
	public ResponseEntity<?> logInUser(@Parameter(description = "User data for authentification")
										@Valid 
										@RequestBody 
										UserDTO userDTO) {
		
		
		try {
			String jwtToken = userServiceImpl.authenticateUser(userDTO);
			return ResponseEntity.ok().body(jwtToken);
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
		}
		
	}
	
	@PostMapping("/signup")
	@Operation(description = "Register user")	
	public ResponseEntity<?> signUpUser(@Parameter(description = "User dto for register in application")
										   @Valid 
										   @RequestBody 
										   UserDTO userDTO) {
		
		try {
			
			UserDTO registeredUserDTO = userServiceImpl.registerUser(userDTO);
			return ResponseEntity.status(HttpStatus.CREATED).body(registeredUserDTO);
			
		} catch (ServiceException e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
		
	}
	
}
