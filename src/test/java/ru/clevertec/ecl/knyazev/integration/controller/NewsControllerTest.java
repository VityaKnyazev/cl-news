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
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
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
public class NewsControllerTest {
	
	private static final String LOGIN_REQUEST = "/login";
	
	private static final String REQUEST = "/news";
	
	private MockMvc mockMvc;
	
	private ObjectMapper objectMapper;	
	
	
	@Test
	@Transactional
	public void checkGetNewsShouldReturnOk() throws Exception {
		
		String inputIdRequest = REQUEST + "/18";
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(inputIdRequest))
				  .andReturn();

		int actualStatus = result.getResponse().getStatus();
		String actualJsonBody = result.getResponse().getContentAsString(Charset.forName("UTF-8"));		
		NewsDTO actualNewsDTO = objectMapper.readValue(actualJsonBody, NewsDTO.class);
		
		Integer defaultCommentsSize = 3;

		assertAll(
			() -> assertThat(actualStatus).isEqualTo(200),
			() -> assertThat(actualJsonBody).isEqualTo(TestData.newsDTOOnId()),
			() -> assertThat(actualNewsDTO.getComments()).isNotEmpty(),
			() -> assertThat(actualNewsDTO.getComments()).hasSize(defaultCommentsSize)
		);
		
	}
	
	@Test
	@Transactional
	public void checkGetNewsShouldReturnBadRequest() throws Exception {
		
		String inputIdRequest = REQUEST + "/125884";
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(inputIdRequest))
				  .andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}	
	
	@Test
	@Transactional
	public void checkGetAllNewsShouldReturnOk() throws Exception {
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(REQUEST))
								  .andReturn();
		
		int actualStatus = result.getResponse().getStatus();
	
		CollectionType newsDTOsListType = objectMapper.getTypeFactory().constructCollectionType(List.class, NewsDTO.class);
		List<NewsDTO> actualNewsDTOs = objectMapper.readValue(result.getResponse().getContentAsByteArray(), newsDTOsListType);
		
		Integer defaultNewSize = 3;
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(actualNewsDTOs).isNotEmpty(),
					() -> assertThat(actualNewsDTOs.get(0).getId()).isGreaterThan(0L),
					() -> assertThat(actualNewsDTOs).hasSize(defaultNewSize),
					() -> assertThat(actualNewsDTOs.get(0).getComments()).isNull()
				);
		
	}
	
	@Test
	@Transactional
	public void checkGetAllNewsShouldReturnOkOnTextPart() throws Exception {
		
		String inputCommentTextPart = "рот";
		 
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(REQUEST)
								  .param("text_part", inputCommentTextPart))
								  .andReturn();
		
		int actualStatus = result.getResponse().getStatus();
		
		CollectionType newsDTOsListType = objectMapper.getTypeFactory().constructCollectionType(List.class, NewsDTO.class);
		List<NewsDTO> newsDTOs = objectMapper.readValue(result.getResponse().getContentAsByteArray(), newsDTOsListType);
		
		Integer defaultNewSize = 3;
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(newsDTOs).isNotEmpty(),
					() -> assertThat(newsDTOs).allMatch(c -> c.getText().contains(inputCommentTextPart)),
					() -> assertThat(newsDTOs).hasSize(defaultNewSize),
					() -> assertThat(newsDTOs.get(0).getComments()).isNull()
				);
		
	}
	
	@Test
	@Transactional
	public void checkGetAllNewsShouldReturnBadRequest() throws Exception {
		
		String inputPage = "125";
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(REQUEST)
				  .param("page", inputPage))
				  .andReturn();

		int actualStatus = result.getResponse().getStatus();	
				
		assertThat(actualStatus).isEqualTo(400);
		
	}

	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalistAndAdmin")
	@Transactional
	public void checkAddNewsShouldReturnOk(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
			
		String savingNewsDTO = TestData.savingNewsDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		NewsDTO actualNewsDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), NewsDTO.class);
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(201),
					() -> assertThat(actualNewsDTO).isNotNull(),
					() -> assertThat(actualNewsDTO.getId()).isGreaterThan(0L)
				);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalistAndAdmin")
	@Transactional
	public void checkAddNewsShouldReturnBadRequest(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
			
		String savingInvalidNewsDTO = TestData.savingInvalidNewsDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingInvalidNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriber")
	@Transactional
	public void checkAddNewsShouldReturnForbiden(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String savingNewsDTO = TestData.savingNewsDTO();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(403);
		
	}
	
	@Test
	@Transactional
	public void checkAddNewsShouldReturnNotAuthorized() throws Exception {
		
		String savingNewsDTO = TestData.savingNewsDTO();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(401);
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalistAndAdmin")
	@Transactional
	public void checkChangeNewsShouldReturnOk(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String changingNewsDTO = TestData.changingNewsDTO();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		NewsDTO actualNewsDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), NewsDTO.class);

		assertAll(
				() -> assertThat(actualStatus).isEqualTo(200),
				() -> assertThat(actualNewsDTO).isNotNull(),
				() -> assertThat(actualNewsDTO.getTitle()).isEqualTo("Пожалуйста, измени название этой новости"),
				() -> assertThat(actualNewsDTO.getText()).isEqualTo("Пожалуйста, измени текст этой новости")
			);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalistAndAdmin")
	@Transactional
	public void checkChangeNewsShouldReturnBadRequest(String jsonUser) throws Exception {
				
		String jwtTokenHeaderValue = loginUser(jsonUser);
			
		String changingInvalidNewsDTO = TestData.changingInvalidNewsDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingInvalidNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriber")
	@Transactional
	public void checkChangeNewsShouldReturnForbiden(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String changingNewsDTO = TestData.changingNewsDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(403);
	}
	
	@Test
	@Transactional
	public void checkChangeNewsShouldReturnNotAuthorized() throws Exception {
		
		String changingNewsDTO = TestData.changingNewsDTO();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(401);
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalistAndAdmin")
	@Transactional
	public void checkRemoveNewsShouldReturnNoContent(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String removingNews = TestData.removingEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingNews))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(204);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleJournalistAndAdmin")
	@Transactional
	public void checkRemoveNewsShouldReturnNotFound(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String removingInvalidNews = TestData.removingInvalidEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingInvalidNews))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(404);
		
	}
	
	@ParameterizedTest
	@MethodSource("getJSONForUserWithRoleSubscriber")
	@Transactional
	public void checkRemoveNewsShouldReturnForbiden(String jsonUser) throws Exception {
		
		String jwtTokenHeaderValue = loginUser(jsonUser);
		
		String removingNews = TestData.removingEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.header("Authorization", jwtTokenHeaderValue)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingNews))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(403);
		
	}
	
	@Test
	@Transactional
	public void checkRemoveNewsShouldReturnNotAuthorized() throws Exception {
			
		String removingNews = TestData.removingEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingNews))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(401);
		
	}
	
	private static Stream<String> getJSONForUserWithRoleJournalistAndAdmin() {
		return Stream.of(
				TestData.getUserWithRoleJournalist(),
				TestData.getUserWithRoleAdmin()
			);
	}	
	
	private static Stream<String> getJSONForUserWithRoleSubscriber() {
		return Stream.of(
					TestData.getUserWithRoleSubscriber()				
				);
	}	
	
	/**
	 * 
	 * login user for testing security enndpoints.
	 * Using feign client + wireMock stand-alone server
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
