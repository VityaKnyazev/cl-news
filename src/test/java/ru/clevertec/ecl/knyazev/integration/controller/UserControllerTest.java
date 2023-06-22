package ru.clevertec.ecl.knyazev.integration.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.Charset;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.integration.testconfig.TestConfig;
import ru.clevertec.ecl.knyazev.integration.testconfig.testcontainers.PostgreSQLContainersConfig;
import ru.clevertec.ecl.knyazev.integration.testconfig.wiremock.WireMockServerConfig;

@ActiveProfiles(profiles = { "test" })
@SpringBootTest
@AutoConfigureMockMvc
@EnableConfigurationProperties
@ContextHierarchy({
		@ContextConfiguration(classes = WireMockServerConfig.class),
		@ContextConfiguration(classes = PostgreSQLContainersConfig.class),		
		@ContextConfiguration(classes = TestConfig.class)
})
@AllArgsConstructor(onConstructor_ = { @Autowired } )
public class UserControllerTest {
	
	private static final String LOGIN_REQUEST = "/login";
	
	private static final String REGISTER_REQUEST = "/signup";
	
	private MockMvc mockMvc;
	
	private ObjectMapper objectMapper;

	@ParameterizedTest
	@MethodSource("getUserDTOforAuthentication")
	@Transactional
	public void checklogInUserShouldReturnOkAndToken(UserDTO attemptAuthValidUserDTO) throws Exception {
				
		String attemptAuthValidUser = objectMapper.writeValueAsString(attemptAuthValidUserDTO);
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(attemptAuthValidUser))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		String actualJWToken = result.getResponse()
				   					 .getContentAsString(Charset.forName("UTF-8"));
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(actualJWToken).isNotNull(),
					() -> assertThat(actualJWToken).hasSizeGreaterThan(0)
				);
		
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidUserDTOforAuthentication")
	@Transactional
	public void checklogInUserShouldReturnUNAUTHORIZED(UserDTO attemptAuthInvalidUserDTO) throws Exception {
				
		String attemptAuthInvalidUser = objectMapper.writeValueAsString(attemptAuthInvalidUserDTO);
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(attemptAuthInvalidUser))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(401);
		
	}
	
	@Test
	@Transactional
	public void checkSignUpUserShouldReturnCreated() throws Exception {
		
		UserDTO atemptValidUserRegisterationDTO = UserDTO.builder()
													.name("Andrey")
													.password("andrey")
													.build();
		
		String atemptValidUserRegisteration = objectMapper.writeValueAsString(atemptValidUserRegisterationDTO);
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(atemptValidUserRegisteration))
				.andReturn();
		
		int actualStatus = result.getResponse().getStatus();
		UserDTO registeredUserDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), UserDTO.class);
		
		assertAll( 
			() -> assertThat(actualStatus).isEqualTo(201),
			() -> assertThat(registeredUserDTO.getName()).isEqualTo("Andrey"),
			() -> assertThat(registeredUserDTO.getPassword()).hasSize(68),
			() -> assertThat(registeredUserDTO.getRolesDTO()).anyMatch(r -> r.getName().equals("ROLE_SUBSCRIBER"))
		);
		
	}
	
	@Test
	@Transactional
	public void checkSignUpUserShouldReturnBadRequest() throws Exception {
		
		UserDTO atemptInvalidUserRegisterationDTO = UserDTO.builder()
				.name("Mariya")
				.password("mariya")
				.build();

		String atemptInvalidUserRegisteration = objectMapper.writeValueAsString(atemptInvalidUserRegisterationDTO);
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REGISTER_REQUEST)
		.contentType(MediaType.APPLICATION_JSON)
		.characterEncoding(Charset.forName("UTF-8"))
		.content(atemptInvalidUserRegisteration))
		.andReturn();
		
		int actualStatus = result.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}		
	
	private static Stream<UserDTO> getUserDTOforAuthentication() {
		
		return Stream.of(
				UserDTO.builder()
					   .name("Alex")
					   .password("alex")
					   .build(),
				UserDTO.builder()
				       .name("Taker")
				       .password("taker")
				       .build(),
			    UserDTO.builder()
			           .name("Root")
			           .password("root")
			           .build()
		);
		
	}
	
	private static Stream<UserDTO> getInvalidUserDTOforAuthentication() {
		
		return Stream.of(
				UserDTO.builder()
					   .name("Maxim")
					   .password("12345")
					   .build(),
				UserDTO.builder()
				       .name("Anatolii")
				       .password("qwerty")
				       .build(),
			    UserDTO.builder()
			           .name("Admin")
			           .password("root")
			           .build()
		);
		
	}
	
}
