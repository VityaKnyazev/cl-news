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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
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
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;
import ru.clevertec.ecl.knyazev.mapper.NewsMapper;
import ru.clevertec.ecl.knyazev.mapper.NewsMapperImpl;
import ru.clevertec.ecl.knyazev.repository.NewsRepository;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class NewsServiceImplTest {
	
	private MockedStatic<SecurityUserService> securityUserServiceMock;
	
	@Mock
	private NewsRepository newsRepositoryMock;
	
	@Spy
	private NewsMapper newsMapperImpl = new NewsMapperImpl();
	
	@Mock
	private CommentService commentServiceImplMock;

	@InjectMocks
	private NewsServiceImpl newsServiceImpl;
	
	@BeforeEach
	public void setup() {
		securityUserServiceMock = Mockito.mockStatic(SecurityUserService.class);		
	}
	
	@AfterEach
	public void postExecute() {
		
		if (!securityUserServiceMock.isClosed()) {
			securityUserServiceMock.close();
		}
		
	}
	
	@Test
	public void checkShowByIdShouldReturnNewsDTO() throws ServiceException {
		
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
		
		NewsDTO actualNewsDTO = newsServiceImpl.showById(inputNewsId);
		
		assertAll(
					() -> assertThat(actualNewsDTO).isNotNull(),
					() -> assertThat(actualNewsDTO.getComments()).isNullOrEmpty()
				);
	}
	
	@Disabled(value = "Null validation added to controller")
	@Test
	public void checkShowByIdShouldThrowServiceExceptionOnNullId() {
		Long invalidNewsId = null;
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.showById(invalidNewsId));
	}
	
	@Test
	public void checkShowByIdShouldThrowServiceExceptionWhenNewsNotFound() {
		Long inputNewsId = 26885L;
		
		Optional<News> expectedNews = Optional.empty();
		
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
	       	   .thenReturn(expectedNews);
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> newsServiceImpl.showById(inputNewsId));
	}
	
	@Test
	public void checkShowByIdShouldReturnNewsDTOWithCommentsPagination() throws ServiceException {
		
		Optional<News> expectedNewsWrap = Optional.of(News.builder()
									     .id(1L)
									     .title("Об открытии двери")
									     .text("Научное обоснование метода открытия двери...")
									     .build());
		
		List<CommentDTO> expectedCommentsDTO = List.of(
					CommentDTO.builder()
						   .id(2L)
						   .text("Прекрасное обоснование непонятного.")
						   .userName("Vova")
						   .build(),
				    CommentDTO.builder()
						   .id(5L)
						   .text("Непонятная интерпретация явления дождя.")
						   .userName("Kolya")
						   .build());
		
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(expectedNewsWrap);
		
		Mockito.when(commentServiceImplMock.showAllByNewsId(Mockito.anyLong(), 
															Mockito.any(Pageable.class)))
			   .thenReturn(expectedCommentsDTO);
		
		Long inputNewsId = 3L;
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputCommentsSortOrder = "time,asc";
		
		Pageable commentsPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputCommentsSortOrder));
		
		NewsDTO actualNewsDTO = newsServiceImpl.showById(inputNewsId, commentsPageable);
		
		assertThat(actualNewsDTO.getComments()).hasSize(2);
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
	public void checkShowAllOrByTextPartShouldReturnNewsDTOs() throws ServiceException {
		
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
		
		Mockito.when(newsRepositoryMock.findAllByPartNewsText(Mockito.anyString(), Mockito.any(Pageable.class)))
		       .thenReturn(expectedNewsList);
		
		String inputTextPart = "Павлович летит";
		
		int inputPage = 1;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		List<NewsDTO> actualNewsDTOs = newsServiceImpl.showAllOrByTextPart(inputTextPart, inputPageable);
		
		assertAll(
				() -> assertThat(actualNewsDTOs).isNotEmpty(),
				() -> assertThat(actualNewsDTOs).hasSize(1)
			);
	}
	
	@ParameterizedTest
	@NullSource
	@EmptySource
	@ValueSource(strings = { "", " ", "  ", "   " })
	public void checkShowAllOrByTextPartShouldReturnShowAllOnInvalidNewsTextPart(String invalidTextPart) throws ServiceException {
		
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
		
		List<NewsDTO> actualNewsDTOs = newsServiceImpl.showAllOrByTextPart(invalidTextPart, inputPageable);		
		
		Mockito.verify(newsRepositoryMock).findAll(inputPageable);		
		assertThat(actualNewsDTOs).isNotEmpty();		
	}
	
	@Test
	public void checkShowAllOrByTextPartShouldThrowServiceExceptionWhenNotFoundOnNewsTextPart() throws ServiceException {
		
		Mockito.when(newsRepositoryMock.findAllByPartNewsText(Mockito.anyString(), 
				                                           Mockito.any(Pageable.class)))
			   .thenReturn(Lists.newArrayList());
		
		String inputTextPart = "всем привет";
		
		int inputPage = 19;
		int inputPageSize = 3;
		String inputSortOrder = "title,asc";
		
		Pageable inputPageable = PageRequest.of(inputPage, inputPageSize, Sort.by(inputSortOrder));
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                 newsServiceImpl.showAllOrByTextPart(inputTextPart, inputPageable));
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
		
		String expectedSecurityUserName = "Valik";
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
							   .thenReturn(expectedSecurityUserName);
		
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
		
		String expectedSecurityUserName = "Valik";
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
							   .thenReturn(expectedSecurityUserName);
		
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
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(7L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .authorName("Valik")
				  .build());
		
		News expectedChangedNews = News.builder()
								.id(7L)
								.title("Наш современник")
								.text("Первый просветитель, поставивший точки над \"ы\"...")
								.authorName("Valik")
								.build();
		
		String expectedSecurityUserName = "Valik";
		
		List<String> expectedUserRoles = new ArrayList<>() {
			
			private static final long serialVersionUID = -2985849103948103580L;

		{
			add("ROLE_JOURNALIST");	
		}};
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
							   .thenReturn(expectedSecurityUserName);
		securityUserServiceMock.when(SecurityUserService::getSecurityUserRoles)
		   					   .thenReturn(expectedUserRoles);
					
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
	       .thenReturn(dbNewsWrap);
		
		Mockito.when(newsRepositoryMock.save(Mockito.any(News.class)))
	       .thenReturn(expectedChangedNews);		
		
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
	public void checkChangeShouldThrowServiceExceptionOnInvalidNewsDTO(NewsDTO invalidNewsDTO) {
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.change(invalidNewsDTO));
		
	}
	
	@Test
	public void checkChangeShouldThrowServiceExceptionWhenNewsNotFound() {
		
		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
		       .thenReturn(Optional.empty());
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
									 .id(25615L)
									 .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
		                                                  newsServiceImpl.change(inputNewsDTO));
		
	}
	
	@Test
	public void checkChangeShouldThrowServiceExceptionWhenUserChangingForeignNews() {
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(7L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .authorName("Valik")
				  .build());
		
		String expectedSecurityUserName = "Anton";
		
		List<String> expectedUserRoles = new ArrayList<>() {
			
			private static final long serialVersionUID = -2985849103948103580L;

		{
			add("ROLE_JOURNALIST");	
		}};

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
		   .thenReturn(expectedSecurityUserName);
		securityUserServiceMock.when(SecurityUserService::getSecurityUserRoles)
		   .thenReturn(expectedUserRoles);
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
				  .id(7L)
                  .title("Обнови нас, пожалуйста без констрейнта...")
                  .text("Должен выбросить исключение при обновлении меня в базе..")
                  .build();

		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
                                    newsServiceImpl.change(inputNewsDTO));
	}
	
	@Test
	public void checkChangeShouldThrowServiceExceptionOnFailedUpdating() {
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(7L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .authorName("Valik")
				  .build());
		
		String expectedSecurityUserName = "Valik";
		
		List<String> expectedUserRoles = new ArrayList<>() {
			
			private static final long serialVersionUID = -2985849103948103580L;

		{
			add("ROLE_JOURNALIST");	
		}};

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
		   .thenReturn(expectedSecurityUserName);
		securityUserServiceMock.when(SecurityUserService::getSecurityUserRoles)
		   .thenReturn(expectedUserRoles);
		
		Mockito.when(newsRepositoryMock.save(Mockito.any(News.class)))
		       .thenThrow(new DataAccessException("Constraint on changing news") {

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
				  .authorName("Valik")
				  .build());
		
		String expectedSecurityUserName = "Valik";
		
		List<String> expectedUserRoles = new ArrayList<>() {
			
			private static final long serialVersionUID = -2985849103948103580L;

		{
			add("ROLE_JOURNALIST");	
		}};

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
		   .thenReturn(expectedSecurityUserName);
		securityUserServiceMock.when(SecurityUserService::getSecurityUserRoles)
		   .thenReturn(expectedUserRoles);
		
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
	public void checkRemoveShouldThrowServiceExceptionWhenUserRemovingForeignNews() {
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(8L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .authorName("Anton")
				  .build());
		
		String expectedSecurityUserName = "Mark";
		
		List<String> expectedUserRoles = new ArrayList<>() {
			
			private static final long serialVersionUID = -2985849103948103580L;

		{
			add("ROLE_JOURNALIST");	
		}};

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
		   .thenReturn(expectedSecurityUserName);
		securityUserServiceMock.when(SecurityUserService::getSecurityUserRoles)
		   .thenReturn(expectedUserRoles);
		
		NewsDTO inputNewsDTO = NewsDTO.builder()
				  .id(8L)
				  .build();
		
		assertThatExceptionOfType(ServiceException.class).isThrownBy(() -> 
        						  newsServiceImpl.remove(inputNewsDTO));
	}
	
	
	@Test
	public void checkRemoveShouldThrowServiceExceptioOnFailedDeleting() {
		
		Optional<News> dbNewsWrap = Optional.of(News.builder()
				  .id(7L)
				  .title("Первый в мире и стране...")
				  .text("Наш знаменитый мыслитель, ученый и бывший спортсмен...")
				  .authorName("Anton")
				  .build());
		
		String expectedSecurityUserName = "Anton";
		
		List<String> expectedUserRoles = new ArrayList<>() {
			
			private static final long serialVersionUID = -2985849103948103580L;

		{
			add("ROLE_JOURNALIST");	
		}};

		Mockito.when(newsRepositoryMock.findById(Mockito.anyLong()))
			   .thenReturn(dbNewsWrap);
		
		securityUserServiceMock.when(SecurityUserService::getSecurityUserName)
		   .thenReturn(expectedSecurityUserName);
		securityUserServiceMock.when(SecurityUserService::getSecurityUserRoles)
		   .thenReturn(expectedUserRoles);
		
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
