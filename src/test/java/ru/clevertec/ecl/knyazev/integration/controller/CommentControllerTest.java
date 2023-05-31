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

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.integration.testconfig.TestConfig;
import ru.clevertec.ecl.knyazev.integration.testconfig.testcontainers.PostgreSQLContainersConfig;
import ru.clevertec.ecl.knyazev.integration.util.TestData;

@ActiveProfiles(profiles = { "test" })
@SpringBootTest(webEnvironment = WebEnvironment.MOCK)
@AutoConfigureMockMvc
@EnableConfigurationProperties
@ContextHierarchy({
		@ContextConfiguration(classes = PostgreSQLContainersConfig.class),
		@ContextConfiguration(classes = TestConfig.class)
})
public class CommentControllerTest {
	
	private static final String REQUEST = "/comments";
	
	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;	
	
	@Test
	@Transactional
	public void checkGetCommentShouldReturnOk() throws Exception {
		
		String inputIdRequest = REQUEST + "/4";
		
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
	public void checkAddCommentShouldReturnOk() throws Exception {
			
		String savingCommentDTO = TestData.savingCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
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
	
	@Test
	@Transactional
	public void checkAddCommentShouldReturnBadRequest() throws Exception {
			
		String savingInvalidCommentDTO = TestData.savingInvalidCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(savingInvalidCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	@Transactional
	public void checkChangeCommentShouldReturnOk() throws Exception {
		String changingCommentDTO = TestData.changingCommentDTO();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();
		CommentDTO actualCommentDTO = objectMapper.readValue(result.getResponse().getContentAsByteArray(), CommentDTO.class);

		assertAll(
				() -> assertThat(actualStatus).isEqualTo(200),
				() -> assertThat(actualCommentDTO).isNotNull(),
				() -> assertThat(actualCommentDTO.getUserName()).isEqualTo("Karina")
			);
		
	}
	
	@Test
	@Transactional
	public void checkChangeCommentShouldReturnBadRequest() throws Exception {
			
		String changingInvalidCommentDTO = TestData.changingInvalidCommentDTO();
				
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(changingInvalidCommentDTO))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	@Transactional
	public void checkRemoveCommentShouldReturnOk() throws Exception {
		
		String remivongComment = TestData.removingEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(remivongComment))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(204);
		
	}
	
	@Test
	@Transactional
	public void checkRemoveCommentShouldReturnBadRequest() throws Exception {
		
		String remivongInvalidComment = TestData.removingInvalidEntity();
		
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.delete(REQUEST)
				.contentType(MediaType.APPLICATION_JSON)
				.characterEncoding(Charset.forName("UTF-8"))
				.content(remivongInvalidComment))
				.andReturn();

		int actualStatus = result.getResponse().getStatus();

		assertThat(actualStatus).isEqualTo(404);
		
	}
	
}
