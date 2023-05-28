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

import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.dto.mapper.NewsMapper;
import ru.clevertec.ecl.knyazev.dto.mapper.NewsMapperImpl;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;
import ru.clevertec.ecl.knyazev.repository.NewsRepository;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class NewsServiceImplTest {
	
	@Mock
	private NewsRepository newsRepositoryMock;
	
	@Spy
	private NewsMapper newsMapperImpl = new NewsMapperImpl();

	@InjectMocks
	private NewsServiceImpl newsServiceImpl;
	
	@Test
	public void checkShowShouldReturnNewsDTO() throws ServiceException {
		
		Optional<News> expectedNews = Optional.of(
						News.builder()
							.id(2L)
							.title("Some news title")
							.text("Some news text")
							.time(LocalDateTime.now())
							.comments(new ArrayList<>() {
								
								private static final long serialVersionUID = 1L;

							{
								add(Comment.builder()
										   .id(8L)
										   .text("Some comment text")
										   .userName("Sanya")
										   .time(LocalDateTime.now())
										   .build());
							}})
							.build()
				);
		
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(expectedNews);
		
		Long inputNewsId = 2L;
		
		NewsDTO actualNewsDTO = newsServiceImpl.show(inputNewsId);
		
		assertAll(
					() -> assertThat(actualNewsDTO).isNotNull(),
					() -> assertThat(actualNewsDTO.getComments()).hasSize(1)
				);
	}
	
	@Test
	public void checkShowShouldThrowServiceExceptionOnNullId() {
		Long invalidNewsId = null;
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.show(invalidNewsId));
	}
	
	@Test
	public void checkShowShouldThrowServiceExceptionWhenNewsNotFound() {
		Long inputNewsId = 26885L;
		
		Optional<News> expectedNews = Optional.empty();
		
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
	       	   .thenReturn(expectedNews);
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> newsServiceImpl.show(inputNewsId));
	}
	
	@Test
	public void checkShowAllShouldReturnNewsDTOs() throws ServiceException {
		List<News> expectedNewsList = new ArrayList<>() {
			
			private static final long serialVersionUID = -213528847612888926L;
			
		{
			add(News.builder()
			.id(2L)
			.title("Первое изобретение..")
			.text("Ученые изобрели...")
			.time(LocalDateTime.now())
			.comments(new ArrayList<>() {
				
				private static final long serialVersionUID = 1L;

			{
				add(Comment.builder()
						   .id(8L)
						   .text("Еще раз сделали")
						   .userName("Sanya")
						   .time(LocalDateTime.now())
						   .build());
			}})
			.build());
			
			add(News.builder()
					.id(2L)
					.title("Привет из прошлого")
					.text("Нашли капсулу времени...")
					.time(LocalDateTime.now())
					.comments(new ArrayList<>() {
						
						private static final long serialVersionUID = 1L;

					{
						add(Comment.builder()
								   .id(8L)
								   .text("Сталин - жив")
								   .userName("Vanya")
								   .time(LocalDateTime.now())
								   .build());
					}})
					.build());			
		}};
		
		Page<News> pageNews = new PageImpl<>(expectedNewsList);
		
		Mockito.when(newsRepositoryMock.findAll(Mockito.any(Pageable.class)))
		       .thenReturn(pageNews);
		
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		List<NewsDTO> actualNewsDTOs = newsServiceImpl.showAll(inputPageable);
		
		assertAll(
				() -> assertThat(actualNewsDTOs).isNotEmpty(),
				() -> assertThat(actualNewsDTOs).hasSize(2)
			);
	}
	
	@Test
	public void checkShowAllShouldThrowServiceExceptionWhenNotFound() {
		
		Mockito.when(newsRepositoryMock.findAll(Mockito.any(Pageable.class)))
	       .thenReturn(new PageImpl<>(new ArrayList<>()));
		
		int inputPage = 25;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> newsServiceImpl.showAll(inputPageable));
	}
	
	@Test
	public void checkShowAllByTextPartShouldReturnNewsDTOsOnNews() throws ServiceException {
		
		List<News> expectedNewsList = new ArrayList<>() {
			
			private static final long serialVersionUID = -213528847612888926L;
			
		{
			add(News.builder()
			.id(2L)
			.title("Антон Павлович - космонавт")
			.text("Наш Павлович летит в космос...")
			.time(LocalDateTime.now())
			.comments(new ArrayList<>() {
				
				private static final long serialVersionUID = 1L;

			{
				add(Comment.builder()
						   .id(8L)
						   .text("Еще раз один")
						   .userName("Sanya")
						   .time(LocalDateTime.now())
						   .build());
			}})
			.build());		
		}};
		
		Mockito.when(newsRepositoryMock.findByPartNewsText(Mockito.anyString(), Mockito.any(Pageable.class)))
		       .thenReturn(expectedNewsList);
		
		String inputTextPart = "Павлович летит";
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		List<NewsDTO> actualNewsDTOs = newsServiceImpl.showAllByTextPart(inputTextPart, inputPageable);
		
		assertAll(
				() -> assertThat(actualNewsDTOs).isNotEmpty(),
				() -> assertThat(actualNewsDTOs).hasSize(1)
			);
	}
	
	@ParameterizedTest
	@NullSource
	@EmptySource
	@ValueSource(strings = { "", " ", "  ", "   " })
	public void checkShowAllByTextPartShouldReturnShowAllOnInvalidNewsTextPart(String invalidTextPart) throws ServiceException {
		
		List<News> expectedNewsList = new ArrayList<>() {
			
			private static final long serialVersionUID = -213528847612888926L;
			
		{
			add(News.builder()
			.id(2L)
			.title("Антон Павлович - космонавт")
			.text("Наш Павлович летит в космос...")
			.time(LocalDateTime.now())
			.comments(new ArrayList<>() {
				
				private static final long serialVersionUID = 1L;

			{
				add(Comment.builder()
						   .id(8L)
						   .text("Еще раз один")
						   .userName("Sanya")
						   .time(LocalDateTime.now())
						   .build());
			}})
			.build());		
		}};		
		
		Page<News> pageNews = new PageImpl<>(expectedNewsList);
		
		Mockito.when(newsRepositoryMock.findAll(Mockito.any(Pageable.class)))
		       .thenReturn(pageNews);
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		List<NewsDTO> actualNewsDTOs = newsServiceImpl.showAllByTextPart(invalidTextPart, inputPageable);		
		
		Mockito.verify(newsRepositoryMock).findAll(inputPageable);		
		assertThat(actualNewsDTOs).isNotEmpty();		
	}
	
	@Test
	public void checkShowAllByTextPartShouldThrowServiceExceptionWhenNotFoundOnNewsTextPart() throws ServiceException {
		
		Mockito.when(newsRepositoryMock.findByPartNewsText(Mockito.anyString(), 
				                                           Mockito.any(Pageable.class)))
			   .thenReturn(Lists.newArrayList());
		
		String inputTextPart = "всем привет";
		
		int inputPage = 19;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                 newsServiceImpl.showAllByTextPart(inputTextPart, inputPageable));
	}
	
	@Test
	public void checkAddShouldReturnNewsDTO() throws ServiceException {
		
		Long expectedNewsId = 8L;
		
		News expectedNews = News.builder()
								.id(expectedNewsId)
								.title("Научпоп")
								.text("Современные тенденции...")
								.time(LocalDateTime.now())
								.build();
		
		Mockito.when(newsRepositoryMock.save(Mockito.any(News.class)))
		       .thenReturn(expectedNews);
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									  .title("Научпоп")
									  .text("Современные тенденции...")
									  .build();
		
		NewsDTO savedNewsDTO = newsServiceImpl.add(inputNewsDTO);
		
		assertAll(
					() -> assertThat(savedNewsDTO).isNotNull(),
					() -> assertThat(savedNewsDTO.getId()).isEqualTo(expectedNewsId),
					() -> assertThat(savedNewsDTO.getCreateDate()).isNotNull()
				);
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidNewsDTOForAdding")
	public void checkAddShouldThrowServiceExceptioOnInvalidNewsDTO(NewsDTO invalidNewsDTO) {
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.add(invalidNewsDTO));
	}
	
	@Test
	public void checkAddShouldThrowServiceExceptioOnFailedSaving() {
		
		Mockito.when(newsRepositoryMock.save(Mockito.any(News.class)))
		       .thenThrow(new DataAccessException("Constraint on saving news") {

				private static final long serialVersionUID = -1469072966712485906L;
			});
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
				                      .title("Сохрани нас правильно, пожалуйста...")
				                      .text("Должен выбросить исключение при сохранении меня в базу..")
				                      .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.add(inputNewsDTO));
	}
	
	@Test
	public void checkChangeShouldReturnNewsDTO() throws ServiceException {
		
		News expectedChangedNews = News.builder()
								.id(7L)
								.title("Наш современник")
								.text("Первый просветитель, поставивший точки над \"ы\"...")
								.build();
						
		Mockito.when(newsRepositoryMock.save(Mockito.any(News.class)))
	       .thenReturn(expectedChangedNews);
		
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
									  .id(7L)
									  .title("Первый в мире и стране...")
									  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
									  .build());
			
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(dbNewsWrap);
		
		NewsDTO inputChangingNews = NewsDTO.builder()
										.id(7L)
										.title("Наш современник")
										.text("Первый правитель, поставивший точки над \"ы\"...")
										.build();
		
		NewsDTO actualNewsDTO = newsServiceImpl.change(inputChangingNews);
		
		assertAll(
					() -> assertThat(actualNewsDTO.getTitle())
					      .isEqualTo(expectedChangedNews.getTitle()),
					() -> assertThat(actualNewsDTO.getText())
				          .isEqualTo(expectedChangedNews.getText())
				);
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidNewsDTOForChanging")
	public void checkChangeShouldThrowServiceExceptionOnInvalidNewsDTO(NewsDTO invalidNewDTO) {
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.change(invalidNewDTO));
		
	}
	
	@Test
	public void checkChangeShouldThrowServiceExceptionWhenNewsNotFound() {
		
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(Optional.empty());
		
		NewsDTO inputNewDTO = NewsDTO.builder()
									 .id(25615L)
									 .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.change(inputNewDTO));
		
	}
	
	@Test
	public void checkChangeShouldThrowServiceExceptionOnFailedUpdating() {
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(7L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .build());

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		Mockito.when(newsRepositoryMock.save(Mockito.any(News.class)))
		       .thenThrow(new DataAccessException("Constraint on saving news") {

				private static final long serialVersionUID = -1469072966712485906L;
			});
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									  .id(7L)
				                      .title("Обнови нас, пожалуйста без констрейнта...")
				                      .text("Должен выбросить исключение при обновлении меня в базе..")
				                      .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.change(inputNewsDTO));
	}
	
	@Test
	public void checkRemoveShouldDoesntThrowAnyExceptions() {
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(7L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .build());

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		Mockito.doNothing().when(newsRepositoryMock).delete(Mockito.any(News.class));
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									  .id(7L)
									  .build();
		
		assertThatCode(() -> newsServiceImpl.remove(inputNewsDTO)).doesNotThrowAnyException();
		
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidNewsDTOForRemoving")
	public void checkRemoveShouldThrowServiceExceptionOnInvalidNewsDTO(NewsDTO invalidNewDTO) {
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.remove(invalidNewDTO));
		
	}
	
	@Test
	public void checkRemoveShouldThrowServiceExceptionWhenNewsNotFound() {
		
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(Optional.empty());
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									  .id(7L)
									  .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.remove(inputNewsDTO));
		
	}
	
	@Test
	public void checkRemoveShouldThrowServiceExceptioOnFailedDeleting() {
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(8L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .build());

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		Mockito.doThrow(new DataAccessException("Deleting news constraint") {
			
			private static final long serialVersionUID = -2968457050893031957L;
			
			
		}).when(newsRepositoryMock).delete(Mockito.any(News.class));
		
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									  .id(7L)
				                      .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.remove(inputNewsDTO));
	}
	
	private static Stream<NewsDTO> getInvalidNewsDTOForAdding() {
		return Stream.of(
					null,
					NewsDTO.builder()
						   .id(3L)
						   .build()
				);
	}
	
	private static Stream<NewsDTO> getInvalidNewsDTOForChanging() {
		return Stream.of(
					null,
					NewsDTO.builder()
						   .id(null)
						   .build(),
					NewsDTO.builder()
						   .id(0L)
						   .build()
				);
	}
	
	private static Stream<NewsDTO> getInvalidNewsDTOForRemoving() {
		return Stream.of(
					null,
					NewsDTO.builder()
						   .id(null)
						   .build(),
					NewsDTO.builder()
						   .id(0L)
						   .build()
				);
	}
	
}
