package ru.clevertec.ecl.knyazev.integration.controller;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.Charset;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.integration.testconfig.TestConfig;
import ru.clevertec.ecl.knyazev.integration.testconfig.testcontainers.PostgreSQLContainersConfig;
import ru.clevertec.ecl.knyazev.integration.util.TestData;

@ActiveProfiles(profiles = { "test" })
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc(addFilters = false)
@EnableConfigurationProperties
@ContextHierarchy({
		@ContextConfiguration(classes = PostgreSQLContainersConfig.class),
		@ContextConfiguration(classes = TestConfig.class)
})
public class NewsControllerTest {
	
	private static final String REQUEST = "/news";
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Test
	@Transactional
	public void checkGetNewsShouldReturnOk() throws Exception {
		
		String inputIdRequest = REQUEST + "/3";
		
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

	@Test
	@Transactional
	public void checkAddNewsShouldReturnOk() throws Exception {
			
		String savingNewsDTO = TestData.savingNewsDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
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
	
	@Test
	@Transactional
	public void checkAddNewsShouldReturnBadRequest() throws Exception {
			
		String savingInvalidNewsDTO = TestData.savingInvalidNewsDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingInvalidNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	@Transactional
	public void checkChangeNewsShouldReturnOk() throws Exception {
		String changingNewsDTO = TestData.changingNewsDTO();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
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
	
	@Test
	@Transactional
	public void checkChangeNewsShouldReturnBadRequest() throws Exception {
			
		String changingInvalidNewsDTO = TestData.changingInvalidNewsDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingInvalidNewsDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	@Transactional
	public void checkRemoveNewsShouldReturnNoContent() throws Exception {
		
		String removingNews = TestData.removingEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingNews))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(204);
		
	}
	
	@Test
	@Transactional
	public void checkRemoveNewsShouldReturnBadRequest() throws Exception {
		
		String removingInvalidNews = TestData.removingInvalidEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(removingInvalidNews))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(404);
		
	}
	
}
