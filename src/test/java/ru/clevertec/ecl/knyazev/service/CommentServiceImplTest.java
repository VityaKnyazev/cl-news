package ru.clevertec.ecl.knyazev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.dto.mapper.CommentMapper;
import ru.clevertec.ecl.knyazev.dto.mapper.CommentMapperImpl;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;
import ru.clevertec.ecl.knyazev.repository.CommentRepository;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class CommentServiceImplTest {
	
	@Spy
	private CommentMapper commentMapperImpl = new CommentMapperImpl();
	
	@Mock
	private CommentRepository commentRepositoryMock;
	
	@InjectMocks
	private CommentServiceImpl commentServiceImpl;
	
	@Test
	public void checkShowShouldReturnCommentDTO() throws ServiceException {
		
		Long expectedCommentId = 2L;
		
		Optional<Comment> expectedComment = Optional.of(
				Comment.builder()
				   .id(expectedCommentId)
				   .news(News.builder()
						     .id(1L)
						     .title("О закрытии границы")
						     .text("Репортаж с места событий...")
						     .build())
				   .text("Вот это поворот")
				   .userName("Svetlana")
				   .build()
				);
		
		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(expectedComment);
		
		Long inputCommentId = 2L;
		
		CommentDTO actualCommentDTO = commentServiceImpl.show(inputCommentId);
		
		assertAll(
					() -> assertThat(actualCommentDTO).isNotNull(),
					() -> assertThat(actualCommentDTO.getId()).isEqualTo(expectedCommentId)
				);
	}
	
	@Test
	public void checkShowShouldThrowServiceExceptionOnNullId() {
		Long invalidCommentId = null;
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
									   commentServiceImpl.show(invalidCommentId));
	}
	
	@Test
	public void checkShowShouldThrowServiceExceptionWhenCommentNotFound() {
		Long inputCommentId = 35489L;
		
		Optional<Comment> expectedComment = Optional.empty();
		
		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
	       	   .thenReturn(expectedComment);
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
									   commentServiceImpl.show(inputCommentId));
	}
	
	@Test
	public void checkShowAllShouldReturnComentsDTO() throws ServiceException {
		
		List<Comment> expectedComments = new ArrayList<>() {
			
			private static final long serialVersionUID = -2454121545589L;
			
		{
			add(Comment.builder()
					   .id(8L)
					   .news(News.builder()
							     .id(1L)
							     .title("Великая стройка")
							     .text("Строили, строили и наконец построили...")
							     .build())
					   .text("Еще раз сделали")
					   .userName("Sanya")
					   .time(LocalDateTime.now())
					   .build());
			add(Comment.builder()
					   .id(9L)
					   .news(News.builder()
							     .id(1L)
							     .title("Грандиозный прорыв")
							     .text("Прорвало...")
							     .build())
					   .text("Сталин - будет жить")
					   .userName("Genya")
					   .time(LocalDateTime.now())
					   .build());
		}};
			
		
		Page<Comment> pageComments = new PageImpl<>(expectedComments);
		
		Mockito.when(commentRepositoryMock.findAll(Mockito.any(Pageable.class)))
		       .thenReturn(pageComments);
		
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "time,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		List<CommentDTO> actualCommentsDTO = commentServiceImpl.showAll(inputPageable);
		
		assertAll(
				() -> assertThat(actualCommentsDTO).isNotEmpty(),
				() -> assertThat(actualCommentsDTO).hasSize(2)
			);
	}
	
	@Test
	public void checkShowAllShouldThrowServiceExceptionWhenNotFound() {
		
		Mockito.when(commentRepositoryMock.findAll(Mockito.any(Pageable.class)))
	       .thenReturn(new PageImpl<>(new ArrayList<>()));
		
		int inputPage = 25;
		int inputPageSize = 3;
		String inputSortOrder = "time,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> commentServiceImpl.showAll(inputPageable));
	}
	
	@Test
	public void checkShowAllByTextPartShouldReturnCommentsDTO() throws ServiceException {
		
		List<Comment> expectedComments = new ArrayList<>() {
			
			private static final long serialVersionUID = -2454121545589L;
			
		{
			add(Comment.builder()
					   .id(8L)
					   .news(News.builder()
							     .id(1L)
							     .title("Великая стройка")
							     .text("Строили, строили и наконец построили...")
							     .build())
					   .text("Еще раз сделали")
					   .userName("Sanya")
					   .time(LocalDateTime.now())
					   .build());
			add(Comment.builder()
					   .id(9L)
					   .news(News.builder()
							     .id(1L)
							     .title("Грандиозный прорыв")
							     .text("Прорвало...")
							     .build())
					   .text("Сталин - будет раз жить")
					   .userName("Genya")
					   .time(LocalDateTime.now())
					   .build());
		}};
		
		Mockito.when(commentRepositoryMock.findAllByPartCommentText(Mockito.anyString(), Mockito.any(Pageable.class)))
		       .thenReturn(expectedComments);
		
		String inputTextPart = "раз";
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "time,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		List<CommentDTO> actualCommentsDTO = commentServiceImpl.showAllByTextPart(inputTextPart, inputPageable);
		
		assertAll(
				() -> assertThat(actualCommentsDTO).isNotEmpty(),
				() -> assertThat(actualCommentsDTO).hasSize(2)
			);
	}
	
	@ParameterizedTest
	@NullSource
	@EmptySource
	@ValueSource(strings = { "", " ", "  ", "   " })
	public void checkShowAllByTextPartShouldReturnShowAllOnInvalidCommentTextPart(String invalidTextPart) throws ServiceException {
		
		List<Comment> expectedComments = new ArrayList<>() {
			
			private static final long serialVersionUID = -2454121545589L;
			
		{
			add(Comment.builder()
					   .id(8L)
					   .news(News.builder()
							     .id(1L)
							     .title("Великая стройка")
							     .text("Строили, строили и наконец построили...")
							     .build())
					   .text("Еще раз сделали")
					   .userName("Sanya")
					   .time(LocalDateTime.now())
					   .build());
			add(Comment.builder()
					   .id(9L)
					   .news(News.builder()
							     .id(1L)
							     .title("Грандиозный прорыв")
							     .text("Прорвало...")
							     .build())
					   .text("Сталин - будет раз жить")
					   .userName("Genya")
					   .time(LocalDateTime.now())
					   .build());
		}};
		
		Page<Comment> pageComments = new PageImpl<>(expectedComments);
		
		Mockito.when(commentRepositoryMock.findAll(Mockito.any(Pageable.class)))
		       .thenReturn(pageComments);
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "time,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		List<CommentDTO> actualCommentsDTO = commentServiceImpl.showAllByTextPart(invalidTextPart, inputPageable);		
		
		Mockito.verify(commentRepositoryMock).findAll(inputPageable);		
		assertThat(actualCommentsDTO).isNotEmpty();		
	}
	
	@Test
	public void checkShowAllByTextPartShouldThrowServiceExceptionWhenNotFoundOnCommentTextPart() throws ServiceException {
		
		Mockito.when(commentRepositoryMock.findAllByPartCommentText(Mockito.anyString(), 
				                                           Mockito.any(Pageable.class)))
			   .thenReturn(Lists.newArrayList());
		
		String inputTextPart = "всем привет";
		
		int inputPage = 19;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                 commentServiceImpl.showAllByTextPart(inputTextPart, inputPageable));
	}
	
	//TODO checkShowAllByNewsId
	
}
