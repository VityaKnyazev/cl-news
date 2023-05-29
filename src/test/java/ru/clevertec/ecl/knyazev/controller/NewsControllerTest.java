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
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.service.NewsService;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class NewsControllerTest {
	
	@Mock
	private NewsService newsServiceImplMock; 
	
	@InjectMocks
	private NewsController newsController;
	
	private static final String REQUEST = "/news";
	
	private MockMvc mockMVC;
	
	private ObjectMapper objectMapper;
	
	@BeforeEach
	public void setUp() {
		mockMVC = MockMvcBuilders.standaloneSetup(newsController)
				.setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
				.defaultRequest(MockMvcRequestBuilders.get("/"))
				.build();
		
		objectMapper = new Jackson2ObjectMapperBuilder().build();
	}
	
	@Test
	public void checkGetNewsShouldReturnOk() throws Exception {
		
		NewsDTO expectedNewsDTO = NewsDTO.builder()
				                         .id(1L)
				                         .title("Важная новость")
				                         .text("Нанем с важных новостей")
				                         .build();
		
		Mockito.when(newsServiceImplMock.show(Mockito.anyLong(), Mockito.any(Pageable.class)))
		       .thenReturn(expectedNewsDTO);
		
		String newsIdRequest= REQUEST + "/1";
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(newsIdRequest))
		                              .andReturn();
		
		Integer actualStatus = mvcResult.getResponse().getStatus();
		NewsDTO actualNewsDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), NewsDTO.class);
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(actualNewsDTO).isEqualTo(expectedNewsDTO)
				);
		
	} 
	
	@Test
	public void checkGetNewsShouldReturnBadRequest() throws Exception {
		
		Mockito.when(newsServiceImplMock.show(Mockito.anyLong(), Mockito.any(Pageable.class)))
	           .thenThrow(ServiceException.class);
		
		String newsIdRequest= REQUEST + "/1";
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(newsIdRequest))
				                      .andReturn();

		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkGetAllNewsShouldReturnOk() throws Exception {
		
		List<NewsDTO> expectedNewsDTOs = new ArrayList<>() {
			
			private static final long serialVersionUID = -8306939700786462237L;

		{
			add(NewsDTO.builder()
					   .id(1L)
					   .title("Отличная новость")
					   .text("Скидки на все виды новотей")
					   .comments(new ArrayList<>() {
						   
						private static final long serialVersionUID = 3522277294071814840L;

					   {
						   add(CommentDTO.builder()
								         .id(2L)
								         .userName("Ivanka")
								         .text("Скидки - это отличная новость")
								         .build());
					   }})
					   .build()
			  );
		}};
		
		Mockito.when(newsServiceImplMock.showAllOrByTextPart(Mockito.any(), Mockito.any(Pageable.class)))
		       .thenReturn(expectedNewsDTOs);
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(REQUEST))
                                      .andReturn();
		
		CollectionType newsDTOListType = objectMapper.getTypeFactory().constructCollectionType(List.class, NewsDTO.class);
		
		Integer actualStatus = mvcResult.getResponse().getStatus();
		List<NewsDTO> actualNewsDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), newsDTOListType);
		
		assertAll(
					() -> assertThat(actualStatus).isEqualTo(200),
					() -> assertThat(actualNewsDTO).isEqualTo(expectedNewsDTOs)
				);
		
	}
	
	@Test
	public void checkGetAllNewsShouldReturnBadRequest() throws Exception {
		
		Mockito.when(newsServiceImplMock.showAllOrByTextPart(Mockito.any(), Mockito.any(Pageable.class)))
	           .thenThrow(ServiceException.class);
		
		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.get(REQUEST))
				                      .andReturn();

		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkAddNewsShouldReturnOk() throws Exception {
		
		NewsDTO expectedNewsDTO = NewsDTO.builder()
                .id(1L)
                .title("Важная новость")
                .createDate("2023-05-29T16:54:26.548")
                .text("Нанем с важных новостей")
                .build();

		Mockito.when(newsServiceImplMock.add(Mockito.any(NewsDTO.class)))
			   .thenReturn(expectedNewsDTO);
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
					                .title("Важная новость")
					                .text("Нанем с важных новостей")
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputNewsDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		NewsDTO actualNewsDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), NewsDTO.class);
		
		assertAll(
			() -> assertThat(actualStatus).isEqualTo(201),
			() -> assertThat(actualNewsDTO).isEqualTo(expectedNewsDTO)
		);
		
	}
	
	@Test
	public void checkAddNewsShouldReturnBadRequest() throws Exception {

		Mockito.when(newsServiceImplMock.add(Mockito.any(NewsDTO.class)))
			   .thenThrow(ServiceException.class);
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									.id(2L)
					                .title("Важная новость")
					                .text("Нанем с важных новостей")
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.post(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputNewsDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkChangeNewsShouldReturnOk() throws Exception {
		
		NewsDTO expectedNewsDTO = NewsDTO.builder()
                .id(1L)
                .title("Важная новость")
                .createDate("2023-05-29T16:54:26.548")
                .text("Нанем с важных новостей")
                .build();

		Mockito.when(newsServiceImplMock.change(Mockito.any(NewsDTO.class)))
			   .thenReturn(expectedNewsDTO);
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									.id(1L)
					                .title("Важная новость")
					                .createDate("2023-05-29T16:54:26.548")
					                .text("Нанем с важных новостей")
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.put(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputNewsDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		NewsDTO actualNewsDTO = objectMapper.readValue(mvcResult.getResponse().getContentAsByteArray(), NewsDTO.class);
		
		assertAll(
			() -> assertThat(actualStatus).isEqualTo(200),
			() -> assertThat(actualNewsDTO).isEqualTo(expectedNewsDTO)
		);
		
	}
	
	@Test
	public void checkChangeNewsShouldReturnBadRequest() throws Exception {

		Mockito.when(newsServiceImplMock.change(Mockito.any(NewsDTO.class)))
			   .thenThrow(ServiceException.class);
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									.id(1L)
					                .title("Важная новость")
					                .createDate("2023-05-29T16:54:26.548")
					                .text("Нанем с важных новостей")
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.put(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputNewsDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(400);
		
	}
	
	@Test
	public void checkRemoveNewsShouldReturnOk() throws Exception {		

		Mockito.doNothing().when(newsServiceImplMock).remove(Mockito.any(NewsDTO.class));
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									.id(1L)
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.delete(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputNewsDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(204);
		
	}
	
	@Test
	public void checkRemoveNewsShouldReturnNotFound() throws Exception {		

		Mockito.doThrow(ServiceException.class).when(newsServiceImplMock).remove(Mockito.any(NewsDTO.class));
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									.id(125647L)
					                .build();

		MvcResult mvcResult =  mockMVC.perform(MockMvcRequestBuilders.delete(REQUEST)
									  .contentType(MediaType.APPLICATION_JSON)
									  .characterEncoding(Charset.forName("UTF-8"))
									  .content(objectMapper.writeValueAsBytes(inputNewsDTO)))
									  .andReturn();
			
		Integer actualStatus = mvcResult.getResponse().getStatus();
		
		assertThat(actualStatus).isEqualTo(404);
		
	}
	
}
