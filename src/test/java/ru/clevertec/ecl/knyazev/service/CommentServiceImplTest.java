package ru.clevertec.ecl.knyazev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.testcontainers.shaded.com.google.common.collect.Lists;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
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
			
			private static final long serialVersionUID = -54555451L;
			
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
			
			private static final long serialVersionUID = 1245421548L;
			
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
		String inputSortOrder = "title,asc";
		
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
	
	@Test
	public void checkShowAllByNewsIdShouldReturnComments() throws ServiceException {
		
		List<Comment> expectedComments = new ArrayList<>() {
			
			private static final long serialVersionUID = -3555845157L;
			
		{
			add(Comment.builder()
					   .id(8L)
					   .news(News.builder()
							     .id(1L)
							     .title("Великое разочарование")
							     .text("Делали, делали и наконец разочаровались...")
							     .build())
					   .text("Еще раз все встало на свои места")
					   .userName("Sonya")
					   .time(LocalDateTime.now())
					   .build());
			add(Comment.builder()
					   .id(9L)
					   .news(News.builder()
							     .id(1L)
							     .title("Великое разочарование")
							     .text("Делали, делали и наконец разочаровались...")
							     .build())
					   .text("Стекло - будет служить")
					   .userName("Gena")
					   .time(LocalDateTime.now())
					   .build());
		}};
		
		Mockito.when(commentRepositoryMock.findAllByNewsId(Mockito.anyLong(), Mockito.any(Pageable.class)))
		       .thenReturn(expectedComments);
		
		Long inputNewsId = 1L;
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "time,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		
		List<Comment> actualComments = commentServiceImpl.showAllByNewsId(inputNewsId, inputPageable);
		
		assertAll(
					() -> assertThat(actualComments).isNotEmpty(),
					() -> assertThat(actualComments).hasSize(2)
				);
		
	}
	
	@Test
	public void checkShowAllByNewsIdShouldThrowServiceExceptionOnInvalidNewsId() {
		
		Long inputNewsId = null;
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "time,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.showAllByNewsId(inputNewsId, inputPageable));
	}
	
	@Test
	public void checkShowAllByNewsIdShouldThrowServiceExceptionWhenNewsNotFound() {
		
		Mockito.when(commentRepositoryMock.findAllByNewsId(Mockito.anyLong(), Mockito.any(Pageable.class)))
	           .thenReturn(new ArrayList<>());
		
		Long inputNewsId = 12558L;
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "time,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.showAllByNewsId(inputNewsId, inputPageable));
	}
	
	@Test
	public void checkAddShouldReturnCommentDTO() throws ServiceException {
		
		Long expectedCommentId = 8L;
		
		Comment expectedComment = Comment.builder()
								.id(expectedCommentId)
								.news(News.builder()
										  .id(5L)
										  .title("Тестирование")
										  .text("Тестирование кода избавляет нас ....")
										  .build())
								.text("Современные тенденции...")
								.time(LocalDateTime.now())
								.build();
		
		Mockito.when(commentRepositoryMock.save(Mockito.any(Comment.class)))
		       .thenReturn(expectedComment);
		
		CommentDTO inputComment = CommentDTO.builder()
											.newsDTO(NewsDTO.builder()
													  .id(5L)
													  .build())
											.text("Современные тенденции...")
											.build();
		
		CommentDTO savedCommentDTO = commentServiceImpl.add(inputComment);
		
		assertAll(
					() -> assertThat(savedCommentDTO).isNotNull(),
					() -> assertThat(savedCommentDTO.getId()).isEqualTo(expectedCommentId),
					() -> assertThat(savedCommentDTO.getCreateDate()).isNotNull()
				);
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidCommentDTOForAdding")
	public void checkAddShouldThrowServiceExceptioOnInvalidCommentDTO(CommentDTO invalidCommentDTO) {
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.add(invalidCommentDTO));
	}
	
	@Test
	public void checkAddShouldThrowServiceExceptioOnFailedSaving() {
		
		Mockito.when(commentRepositoryMock.save(Mockito.any(Comment.class)))
		       .thenThrow(new DataAccessException("Constraint on saving comment") {

				private static final long serialVersionUID = -24541544515578L;
			});
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									  .newsDTO(NewsDTO.builder()
											          .id(5L)
											          .build())
				                      .text("Сохрани нас правильно, пожалуйста...")
				                      .userName("Marina")
				                      .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.add(inputCommentDTO));
	}
	
	@Test
	public void checkChangeShouldReturnCommentDTO() throws ServiceException {
		
		Comment expectedChangedComment = Comment.builder()
										.id(7L)
										.news(News.builder()
												  .id(6L)
												  .build())
										.userName("Fernando")
										.text("Тяжеловато воспринимать такие предложения")
										.build();
						
		Mockito.when(commentRepositoryMock.save(Mockito.any(Comment.class)))
	       .thenReturn(expectedChangedComment);
		
		
		Optional<Comment> dbCommentWrap = Optional.of(
				Comment.builder()
				.id(7L)
				.news(News.builder()
						  .id(6L)
						  .build())
				.userName("Mario")
				.text("Тяжеловато воспринимать предложения не понимая сути")
				.build()
				);
			
		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(dbCommentWrap);
		
		CommentDTO inputChangingComment = CommentDTO.builder()
								.id(7L)
								.newsDTO(NewsDTO.builder()
										  .id(6L)
										  .build())
								.userName("Fernando")
								.text("Тяжеловато воспринимать такие предложения")
								.build();
		
		CommentDTO actualCommentDTO = commentServiceImpl.change(inputChangingComment);
		
		assertAll(
					() -> assertThat(actualCommentDTO.getText())
					      .isEqualTo(expectedChangedComment.getText()),
					() -> assertThat(actualCommentDTO.getUserName())
				          .isEqualTo(expectedChangedComment.getUserName())
				);
	}
	
	
	@ParameterizedTest
	@MethodSource("getInvalidCommentDTOForChanging")
	public void checkChangeShouldThrowServiceExceptionOnInvalidCommentDTO(CommentDTO invalidCommentDTO) {
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.change(invalidCommentDTO));
		
	}
	
	@Test
	public void checkChangeShouldThrowServiceExceptionWhenCommentNotFound() {
		
		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(Optional.empty());
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									 .id(25615L)
									 .newsDTO(NewsDTO.builder()
											         .build())
									 .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
														  commentServiceImpl.change(inputCommentDTO));
		
	}
	
	@Test
	public void checkChangeShouldThrowServiceExceptionOnFailedUpdating() {
		
		Optional<Comment> dbCommentWrap = Optional.of(Comment.builder()
									  .id(7L)
									  .news(News.builder() 
											  	.id(2L)
											  	.title("Важная новость")
											  	.text("Привет мир! ...")
											    .build())
									  .text("Наш знаменитый мыслитель ошибся")
									  .userName("Petya")
									  .build());

		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbCommentWrap);
		
		Mockito.when(commentRepositoryMock.save(Mockito.any(Comment.class)))
		       .thenThrow(new DataAccessException("Constraint on changing comment") {

				private static final long serialVersionUID = -15465546254628561L;
			});
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									  .id(7L)
									  .newsDTO(NewsDTO.builder()
											          .build())
				                      .text("Обнови нас, пожалуйста без констрейнта...")
				                      .userName("Vanya")
				                      .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.change(inputCommentDTO));
	}
	
	@Test
	public void checkRemoveShouldDoesntThrowAnyExceptions() {
		
		Optional<Comment> dbCommentWrap = Optional.of(Comment.builder()
				  .id(7L)
				  .news(News.builder()
						    .id(2L)
						    .title("Вот это новость")
						    .text("Делимся важными новостями с Вами...")
						    .build())
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .userName("Misha")
				  .build());

		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbCommentWrap);
		
		Mockito.doNothing().when(commentRepositoryMock).delete(Mockito.any(Comment.class));
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									  .id(7L)
									  .build();
		
		assertThatCode(() -> commentServiceImpl.remove(inputCommentDTO)).doesNotThrowAnyException();
		
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidCommentDTOForRemoving")
	public void checkRemoveShouldThrowServiceExceptionOnInvalidCommentDTO(CommentDTO invalidCommentDTO) {
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.remove(invalidCommentDTO));
		
	}
	
	@Test
	public void checkRemoveShouldThrowServiceExceptionWhenCommentNotFound() {
		
		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(Optional.empty());
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
									  .id(125896L)
									  .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.remove(inputCommentDTO));
		
	}
	
	@Test
	public void checkRemoveShouldThrowServiceExceptioOnFailedDeleting() {
		
		Optional<Comment> dbCommentWrap = Optional.of(Comment.builder()
				  .id(8L)
				  .news(News.builder()
						  .id(8L)
						  .title("О великих людях")
						  .text("Сегодня мы напишем о нелегкой судьбе человека ...")
						  .build())
				  .userName("Sasha")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен не победил")
				  .build());

		Mockito.when(commentRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbCommentWrap);
		
		Mockito.doThrow(new DataAccessException("Deleting comment constraint") {
			
			private static final long serialVersionUID = -215215554515255L;
			
			
		}).when(commentRepositoryMock).delete(Mockito.any(Comment.class));
		
		
		CommentDTO inputCommentDTO = CommentDTO.builder()
											   .id(7L)
						                       .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  commentServiceImpl.remove(inputCommentDTO));
	}
	
	private static Stream<CommentDTO> getInvalidCommentDTOForAdding() {
		
		return Stream.of(
					null,
					CommentDTO.builder()
						   .id(2L)
						   .build(),
				    CommentDTO.builder()
						   .id(null)
						   .newsDTO(null)
						   .build(),
					CommentDTO.builder()
						   .id(null)
						   .newsDTO(NewsDTO.builder()
								           .id(null)
								           .build())
						   .build(),
			        CommentDTO.builder()
						   .id(null)
						   .newsDTO(NewsDTO.builder()
								           .id(0L)
								           .build())
						   .build()
				);
		
	}
	
	private static Stream<CommentDTO> getInvalidCommentDTOForChanging() {
		
		return Stream.of(
				null,
				CommentDTO.builder()
					   .id(null)
					   .build(),
			    CommentDTO.builder()
					   .id(0L)
					   .build(),
				CommentDTO.builder()
					   .id(2L)
					   .newsDTO(null)
					   .build()
			);
		
	}
	
	private static Stream<CommentDTO> getInvalidCommentDTOForRemoving() {
		
		return Stream.of(
				null,
				CommentDTO.builder()
					   .id(null)
					   .build(),
			    CommentDTO.builder()
					   .id(0L)
					   .build()
			);
		
	}
	
}
