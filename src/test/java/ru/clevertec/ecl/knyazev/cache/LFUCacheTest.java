package ru.clevertec.ecl.knyazev.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.clevertec.ecl.knyazev.entity.Comment;

@ExtendWith(MockitoExtension.class)
public class LFUCacheTest {
	
	private static final String CACHE_TYPE = "LFU";
	private static final Integer CACHE_SIZE = 3;
	
	private CacheFactory<Long, Comment> cacheFactory = new CacheFactory<>();
	
	private Cache<Long, Comment> lfuCache;
	
	@BeforeEach
	public void setUp() {
		lfuCache = cacheFactory.initCache(CACHE_TYPE, CACHE_SIZE);
	}
	
	@Test
	public void checkPutShouldAddToCache() {
		Long inputId = 1L;
		
		Comment cachingComment = Comment.builder()
										.id(inputId)
										.text("Добавь это в кеш")
										.build();
		
		assertAll(
				() -> assertThatCode(() -> lfuCache.put(inputId, cachingComment)).doesNotThrowAnyException(),
				() -> assertThat(lfuCache.size()).isEqualTo(1),
				() -> assertThat(lfuCache.contains(inputId))
		);
		
	}
	
	@Test
	public void checkPutShouldReplaceValueOnExistingKey() {	
		
		Comment cachingComment1 = Comment.builder()
				.id(1L)
				.text("First cashed element")
				.build();
		
		Comment cachingComment2 = Comment.builder()
				.id(2L)
				.text("Second cashed element")
				.build();
		
		Comment cachingComment3 = Comment.builder()
				.id(3L)
				.text("Third cashed element")
				.build();
		
		lfuCache.put(1L, cachingComment1);
		lfuCache.put(2L, cachingComment2);
		lfuCache.put(3L, cachingComment3);
		
		Long inputId = 2L;
		Comment cachingComment4 = Comment.builder()
				.id(inputId)
				.text("Fours cashed element")
				.build();
		
		assertAll(
				() -> assertThatCode(() -> lfuCache.put(inputId, cachingComment4)).doesNotThrowAnyException(),
				() -> assertThat(lfuCache.size()).isEqualTo(3),
				() -> assertThat(lfuCache.contains(inputId)).isTrue()
		);
		
	}
	
	@Test
	public void checkPutShouldAddValueOnMaxCacheSize() {	
		
		Comment cachingComment1 = Comment.builder()
				.id(1L)
				.text("First cashed element")
				.build();
		
		Comment cachingComment2 = Comment.builder()
				.id(2L)
				.text("Second cashed element")
				.build();
		
		Comment cachingComment3 = Comment.builder()
				.id(3L)
				.text("Third cashed element")
				.build();
		
		lfuCache.put(1L, cachingComment1);
		lfuCache.put(2L, cachingComment2);
		lfuCache.put(3L, cachingComment3);
		
		Long inputId = 4L;
		Comment cachingComment4 = Comment.builder()
				.id(inputId)
				.text("Fours cashed element")
				.build();
		
		assertAll(
				() -> assertThatCode(() -> lfuCache.put(inputId, cachingComment4)).doesNotThrowAnyException(),
				() -> assertThat(lfuCache.size()).isEqualTo(3),
				() -> assertThat(lfuCache.contains(inputId)).isTrue(),
				() -> assertThat(lfuCache.contains(3L)).isFalse()
		);
		
	}
	
	@Test
	public void checkGetShouldReturnValueOnKey() {
		
		Comment cachingComment1 = Comment.builder()
				.id(1L)
				.text("First cashed element")
				.build();
		
		Comment cachingComment2 = Comment.builder()
				.id(2L)
				.text("Second cashed element")
				.build();
		
		Comment cachingComment3 = Comment.builder()
				.id(3L)
				.text("Third cashed element")
				.build();
		
		lfuCache.put(1L, cachingComment1);
		lfuCache.put(2L, cachingComment2);
		lfuCache.put(3L, cachingComment3);
		
		Long inputKey = 2L;
		Comment expectedCachingComment = Comment.builder()
								.id(2L)
								.text("Second cashed element")
								.build();
		
		Comment actualCachingComment = lfuCache.get(inputKey);
		
		assertThat(actualCachingComment).isEqualTo(expectedCachingComment);
		
	}
	
	@Test
	public void checkGetShouldReturnNullOnNonExistingKey() {
		Comment cachingComment1 = Comment.builder()
				.id(1L)
				.text("First cashed element")
				.build();
		
		Comment cachingComment2 = Comment.builder()
				.id(2L)
				.text("Second cashed element")
				.build();
		
		lfuCache.put(1L, cachingComment1);
		lfuCache.put(2L, cachingComment2);
		
		Long inputKey = 5L;
		
		Comment actualComment = lfuCache.get(inputKey);
		
		assertThat(actualComment).isNull();
	}
	
	@Test
	public void checkRemoveShouldRemoveFromeCache() {
		Comment cachingComment1 = Comment.builder()
				.id(1L)
				.text("First cashed element")
				.build();
		
		Comment cachingComment2 = Comment.builder()
				.id(2L)
				.text("Second cashed element")
				.build();
		
		Comment cachingComment3 = Comment.builder()
				.id(3L)
				.text("Third cashed element")
				.build();
		
		lfuCache.put(1L, cachingComment1);
		lfuCache.put(2L, cachingComment2);
		lfuCache.put(3L, cachingComment3);
		assertAll(
				() -> assertThatCode(() -> lfuCache.remove(3L)).doesNotThrowAnyException(),
				() -> assertThat(lfuCache.size()).isEqualTo(2)
		);
		
	}
	
}
