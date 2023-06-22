package ru.clevertec.ecl.knyazev.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.Charset;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.clevertec.ecl.knyazev.dto.RoleDTO;
import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.service.UserService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {
	
	private static final String LOGIN_REQUEST = "/login";
	
	private static final String REGISTER_REQUEST = "/signup";
	
	@Mock
	private UserService userServiceImpl;; 
	
	@InjectMocks
	private UserController userController;
	
	private MockMvc mockMVC;
	
	private ObjectMapper objectMapper;
	
	@BeforeEach
	public void setUp() {
		mockMVC = MockMvcBuilders.standaloneSetup(userController)
				.defaultRequest(MockMvcRequestBuilders.get("/"))
				.build();
		
		objectMapper = new Jackson2ObjectMapperBuilder().build();
	}
	
	@Test
	public void checkLogInUserShouldReturnOkAndJWTBody() throws Exception {
		
		String expectedJWT = """
				             eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9
				             .eyJuYW1lIjoiSXZhbiIsImV4cCI6MTY4NzI0NDg1MH0.
				             AeVqV9DVrWr-nuXIVwpB2RfQGzY8zJ6WLOe0TiaxNGTlBGa7zz
				             -UZBNgElxV3302fv_wuE0TJG39ObKEwWgNzw""";

		Mockito.when(userServiceImpl.authenticateUser(Mockito.any(UserDTO.class)))
			   .thenReturn(expectedJWT);
		
		UserDTO inputUserDTO = UserDTO.builder()
						              .name("Vano")
						              .password("12345")
						              .email("vano@mail.ru")
						              .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(LOGIN_REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputUserDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		String actualJWT = mvcResult.getResponse().getContentAsString();
		
		assertAll(
			() -> assertThat(actualStatus).isEqualTo(200),
			() -> assertThat(actualJWT).isEqualTo(expectedJWT)
		);
		
	}
	
	@Test
	public void checkLogInUserShouldReturnNotAuthorized() throws Exception {
		
		Mockito.when(userServiceImpl.authenticateUser(Mockito.any(UserDTO.class)))
		   .thenThrow(ServiceException.class);
		
		UserDTO inputUserDTO = UserDTO.builder()
                .name("Vano")
                .password("12345")
                .email("vano@mail.ru")
                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(LOGIN_REQUEST)
					  .contentType(MediaType.APPLICATION_JSON)
					  .characterEncoding(Charset.forName("UTF-8"))
					  .content(objectMapper.writeValueAsBytes(inputUserDTO)))
					  .andReturn();
		
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(401);
	}
	
	@Test
	public void checkSignUpUserShouldReturnCreated() throws Exception {
		
		UserDTO expectedUserDTO = UserDTO.builder()
				.id(5L)
				.name("Vanya")
				.password("{Bcrypt}hash")
				.email("vanya@mail.ru")
				.enabled(true)
				.rolesDTO(new ArrayList<>() {
					
					private static final long serialVersionUID = 1L;

				{
					add(RoleDTO.builder()
							   .id(1L)
							   .name("ROLE_SUBSCRIBER")
							   .build());
				}})
				.build();

		Mockito.when(userServiceImpl.registerUser(Mockito.any(UserDTO.class)))
		                            .thenReturn(expectedUserDTO);
		
		UserDTO inputUserDTO = UserDTO.builder()
						              .name("Vano")
						              .password("12345")
						              .email("vano@mail.ru")
						              .build();
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(REGISTER_REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputUserDTO)))
									  .andReturn();

		Integer actualStatus = mvcResult.getResponse().getStatus();
		UserDTO actualUserDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), UserDTO.class);
		
		assertAll(
			() -> assertThat(actualStatus).isEqualTo(201),
			() -> assertThat(actualUserDTO).isEqualTo(expectedUserDTO)
		);		
	}
	
	@Test
	public void checkSignUpUserShouldReturnBadRequest() throws Exception {
		
		Mockito.when(userServiceImpl.registerUser(Mockito.any(UserDTO.class)))
									.thenThrow(ServiceException.class);
		
		UserDTO inputUserDTO = UserDTO.builder()
						              .name("Vano")
						              .password("12345")
						              .email("vano@mail.ru")
						              .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(REGISTER_REQUEST)
						  .contentType(MediaType.APPLICATION_JSON)
						  .characterEncoding(Charset.forName("UTF-8"))
						  .content(objectMapper.writeValueAsBytes(inputUserDTO)))
						  .andReturn();
		
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
	}
	
}
