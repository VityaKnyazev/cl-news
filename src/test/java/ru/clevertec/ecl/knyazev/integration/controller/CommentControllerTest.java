package ru.clevertec.ecl.knyazev.integration.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.Charset;
import java.util.List;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import lombok.AllArgsConstructor;
import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.integration.testconfig.TestConfig;
import ru.clevertec.ecl.knyazev.integration.testconfig.testcontainers.PostgreSQLContainersConfig;
import ru.clevertec.ecl.knyazev.integration.testconfig.wiremock.WireMockServerConfig;
import ru.clevertec.ecl.knyazev.integration.util.TestData;

@ActiveProfiles(profiles = { "test" })
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = { "spring.config.location=classpath:application-test.yaml" })
@EnableConfigurationProperties
@ContextHierarchy({
		@ContextConfiguration(classes = WireMockServerConfig.class),
		@ContextConfiguration(classes = PostgreSQLContainersConfig.class),
		@ContextConfiguration(classes = TestConfig.class)
})
@AllArgsConstructor(onConstructor_ = { @Autowired } )
public class CommentControllerTest {
	
	private static final String REQUEST = "/comments";
	
	private static final String LOGIN_REQUEST = "/login";
	
	private MockMvc mockMvc;
	
	private ObjectMapper objectMapper;
	
	@Test
	@Transactional
	public void checkGetCommentShouldReturnOk() throws Exception {		
			
		String inputIdRequest = REQUEST + "/18";
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(inputIdRequest))
				  .andReturn();

		int actualStatus = result.getResponse().getStatus();
		String actualJsonBody = result.getResponse().getContentAsString(Charset.forName("UTF-8"));


		assertAll(
			() -> assertThat(actualStatus).isEqualTo(200),
			() -> assertThat(actualJsonBody).isEqualTo(TestData.commentDTOOnId())
		);
		
	}
	
	@Test
	@Transactional
	public void checkGetCommentShouldReturnBadRequest() throws Exception {
		
		String inputIdRequest = REQUEST + "/12584";
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(inputIdRequest))
				  .andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}	
		
	@Test
	@Transactional
	public void checkgetAllCommentsShouldReturnOk() throws Exception {
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(REQUEST))
								  .andReturn();
		
		int actualStatus = result.getResponse().getStatus();
	
		CollectionType commentsDTOListType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentDTO.class);
		List<CommentDTO> actualCommentsDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), commentsDTOListType);
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(actualCommentsDTO).isNotEmpty(),
					() -> assertThat(actualCommentsDTO.get(0).getId()).isGreaterThan(0L)
				);
		
	}
	
	@Test
	@Transactional
	public void checkGetAllCommentsShouldReturnOkOnTextPart() throws Exception {
		
		String inputCommentTextPart = "все";
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(REQUEST)
								  .param("text_part", inputCommentTextPart))
								  .andReturn();
		
		int actualStatus = result.getResponse().getStatus();
		
		CollectionType commentDTOListType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentDTO.class);
		List<CommentDTO> commentsDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), commentDTOListType);
		
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(commentsDTO).isNotEmpty(),
					() -> assertThat(commentsDTO).allMatch(c -> c.getText().contains(inputCommentTextPart))
				);
	}
	
	@Test
	@Transactional
	public void checkGetAllCommentsShouldReturnBadRequest() throws Exception {
		
		String inputPage = "125";
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(REQUEST)
				  .param("page", inputPage))
				  .andReturn();

		int actualStatus = result.getResponse().getStatus();	
		
		
		assertThat(actualStatus).isEqualTo(400);
		
	}

	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriberAndAdmin")
	@Transactional
	public void checkAddCommentShouldReturnOk(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
			
		String savingCommentDTO = TestData.savingCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		CommentDTO actualCommentDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), CommentDTO.class);
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(201),
					() -> assertThat(actualCommentDTO).isNotNull(),
					() -> assertThat(actualCommentDTO.getNewsDTO().getId()).isEqualTo(1)
				);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriberAndAdmin")
	@Transactional
	public void checkAddCommentShouldReturnBadRequest(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
			
		String savingInvalidCommentDTO = TestData.savingInvalidCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingInvalidCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalist")
	@Transactional
	public void checkAddCommentShouldReturnForbiden(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String savingCommentDTO = TestData.savingCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(403);
		
	}
	
	@Test
	@Transactional
	public void checkAddCommentShouldReturnNotAuthorized() throws Exception {
		
		String savingCommentDTO = TestData.savingCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(401);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriberAndAdmin")
	@Transactional
	public void checkChangeCommentShouldReturnOk(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String changingCommentDTO = TestData.changingCommentDTO();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		CommentDTO actualCommentDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), CommentDTO.class);

		assertAll(
				() -> assertThat(actualStatus).isEqualTo(200),
				() -> assertThat(actualCommentDTO).isNotNull(),
				() -> assertThat(actualCommentDTO.getUserName()).isEqualTo("Alex")
			);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriberAndAdmin")
	@Transactional
	public void checkChangeCommentShouldReturnBadRequest(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
			
		String changingInvalidCommentDTO = TestData.changingInvalidCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingInvalidCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalist")
	@Transactional
	public void checkChangeCommentShouldReturnForbiden(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
			
		String changingCommentDTO = TestData.changingCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(403);
		
	}
	
	@Test
	@Transactional
	public void checkChangeCommentShouldReturnNotAuthorized() throws Exception {
							
		String changingCommentDTO = TestData.changingCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(401);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriberAndAdmin")
	@Transactional
	public void checkRemoveCommentShouldReturnNoContent(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String removingComment = TestData.removingEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingComment))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(204);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriberAndAdmin")
	@Transactional
	public void checkRemoveCommentShouldReturnNotFound(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String removingInvalidComment = TestData.removingInvalidEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingInvalidComment))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(404);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalist")
	@Transactional
	public void checkRemoveCommentShouldReturnForbiden(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String removingInvalidComment = TestData.removingInvalidEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingInvalidComment))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(403);
	}
	
	@Test
	public void checkRemoveCommentShouldReturnNotAuthorized() throws Exception {
		
		String removingInvalidComment = TestData.removingInvalidEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingInvalidComment))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(401);
	}
						  
	private static Stream<String> getJSONForUserWithRoleJournalist() {
		return Stream.of(
				TestData.getUserWithRoleJournalist()		
			);
	}	
	
	private static Stream<String> getJSONForUserWithRoleSubscriberAndAdmin() {
		return Stream.of(
					TestData.getUserWithRoleSubscriber(),
					TestData.getUserWithRoleAdmin()				
				);
	}	
	
	/**
	 * 
	 * login user using feign client + wireMock stand-alone server
	 * and return authentication token
	 * 
	 * @param loginUserJSON user name and password in JSON format
	 * @return authentication java web token with prefix "Bearer "
	 * @throws Exception if /login request failed
	 * 
	 */
	private String loginUser(String loginUserJSON) throws Exception {
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(LOGIN_REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(loginUserJSON))
				.andReturn();
		
		String jwtToken = result.getResponse().getContentAsString();
		
		return "Bearer " + jwtToken;
	}
	
}
