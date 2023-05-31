package ru.clevertec.ecl.knyazev.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.service.CommentService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class CommentControllerTest {
	
	@Mock
	private CommentService commentServiceImplMock; 
	
	@InjectMocks
	private CommentController commentController;
	
	private static final String REQUEST = "/comments";
	
	private MockMvc mockMVC;
	
	private ObjectMapper objectMapper;
	
	@BeforeEach
	public void setUp() {
		mockMVC = MockMvcBuilders.standaloneSetup(commentController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.defaultRequest(MockMvcRequestBuilders.get("/"))
				.build();
		
		objectMapper = new Jackson2ObjectMapperBuilder().build();
	}
	
	@Test
	public void checkGetCommentShouldReturnOk() throws Exception {
		
		CommentDTO expectedCommentDTO = CommentDTO.builder()
				                         .id(1L)
				                         .text("Начнем с хороших новостей")
				                         .userName("Alexandr")
				                         .build();
		
		Mockito.when(commentServiceImplMock.show(Mockito.anyLong()))
		       .thenReturn(expectedCommentDTO);
		
		String commentIdRequest= REQUEST + "/1";
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(commentIdRequest))
		                              .andReturn();
		
		Integer actualStatus = mvcResult.getResponse().getStatus();
		CommentDTO actualCommentDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), CommentDTO.class);
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(actualCommentDTO).isEqualTo(expectedCommentDTO)
				);
		
	}
	
	@Test
	public void checkGetCommentShouldReturnBadRequest() throws Exception {
		
		Mockito.when(commentServiceImplMock.show(Mockito.anyLong()))
	           .thenThrow(ServiceException.class);
		
		String commentIdRequest= REQUEST + "/1";
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(commentIdRequest))
				                      .andReturn();

		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkGetAllCommentsShouldReturnOk() throws Exception {
		
		List<CommentDTO> expectedCommentsDTO = new ArrayList<>() {
			
			private static final long serialVersionUID = -8306939700786462237L;

		{
			add(CommentDTO.builder()
					   .id(1L)
					   .text("Скидки на все виды товаров прекрасная возможность")
					   .userName("Ivanka")
					   .build());
			
			add(CommentDTO.builder()
			         .id(2L)
			         .text("Скидки - это отличная новость")
			         .userName("Ivanka")
			         .build());
		}};
		
		
		Mockito.when(commentServiceImplMock.showAllOrByTextPart(Mockito.any(), Mockito.any(Pageable.class)))
	       .thenReturn(expectedCommentsDTO);
	
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(REQUEST))
	                               .andReturn();
		
		CollectionType commentsDTOType = objectMapper.getTypeFactory().constructCollectionType(List.class, CommentDTO.class);
		
		Integer actualStatus = mvcResult.getResponse().getStatus();
		List<CommentDTO> actualCommentsDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), commentsDTOType);
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(actualCommentsDTO).isEqualTo(expectedCommentsDTO)
				);
		
	}
	
	@Test
	public void checkGetAllCommentsShouldReturnBadRequest() throws Exception {
		
		Mockito.when(commentServiceImplMock.showAllOrByTextPart(Mockito.any(), Mockito.any(Pageable.class)))
	           .thenThrow(ServiceException.class);
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(REQUEST))
				                      .andReturn();

		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkAddCommentShouldReturnCreated() throws Exception {
		
		CommentDTO expectedCommentDTO = CommentDTO.builder()
                .id(1L)
                .text("Нанем с важных новостей")
                .userName("Vasya")
                .build();

		Mockito.when(commentServiceImplMock.add(Mockito.any(CommentDTO.class)))
			   .thenReturn(expectedCommentDTO);
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
						                .text("Нанем с важных новостей")
						                .userName("Vasya")
						                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputCommentDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		CommentDTO actualCommentDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), CommentDTO.class);
		
		assertAll(
			() -> assertThat(actualStatus).isEqualTo(201),
			() -> assertThat(actualCommentDTO).isEqualTo(expectedCommentDTO)
		);
		
	}
	
	@Test
	public void checkAddCommentShouldReturnBadRequest() throws Exception {

		Mockito.when(commentServiceImplMock.add(Mockito.any(CommentDTO.class)))
			   .thenThrow(ServiceException.class);
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
										.id(2L)
										.text("Эта новость не сохранится")
						                .userName("Alena")
						                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputCommentDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkChangeNewsShouldReturnOk() throws Exception {
		
		CommentDTO expectedCommentDTO = CommentDTO.builder()
							                .id(2L)
							                .createDate("2023-05-29T16:54:26.548")
							                .text("Не нравится, спи, моя красавица")
							                .userName("Avtondil")
							                .build();

		Mockito.when(commentServiceImplMock.change(Mockito.any(CommentDTO.class)))
			   .thenReturn(expectedCommentDTO);
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
										.id(2L)
						                .text("Не нравится, спи, моя красавица")
						                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.put(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputCommentDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		CommentDTO actualCommentDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), CommentDTO.class);
		
		assertAll(
			() -> assertThat(actualStatus).isEqualTo(200),
			() -> assertThat(actualCommentDTO).isEqualTo(expectedCommentDTO)
		);
		
	}
	
	@Test
	public void checkChangeCommentShouldReturnBadRequest() throws Exception {

		Mockito.when(commentServiceImplMock.change(Mockito.any(CommentDTO.class)))
			   .thenThrow(ServiceException.class);
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									.id(1L)
					                .text("Пытаемся поменять, но не выходит")
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.put(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputCommentDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkRemoveCommentShouldReturnNoContent() throws Exception {		

		Mockito.doNothing().when(commentServiceImplMock).remove(Mockito.any(CommentDTO.class));
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									.id(8L)
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.delete(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputCommentDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(204);
		
	}
	
	@Test
	public void checkRemoveCommentShouldReturnNotFound() throws Exception {		

		Mockito.doThrow(ServiceException.class).when(commentServiceImplMock).remove(Mockito.any(CommentDTO.class));
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									.id(125647L)
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.delete(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputCommentDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(404);
		
	}
	
}
