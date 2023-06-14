package ru.clevertec.ecl.knyazev.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.clevertec.ecl.knyazev.entity.Comment;

public class LRUCacheTest {
	
	private static final String CACHE_TYPE = "LRU";
	private static final Integer CACHE_SIZE = 3;
	
	private CacheFactory cacheFactory = new CacheFactory();
	
	private Cache<Long, Comment> lruCache;
	
	@BeforeEach
	public void setUp() {
		lruCache = cacheFactory.initCache(CACHE_TYPE, CACHE_SIZE);
	}
	
	
	@Test
	public void checkPutShouldAddToCache() {
		Long inputId = 1L;
		
		Comment cachingComment = Comment.builder()
										.id(inputId)
										.text("Добавь это в кеш")
										.build();
		
		assertAll(
				() -> assertThatCode(() -> lruCache.put(inputId, cachingComment)).doesNotThrowAnyException(),
				() -> assertThat(lruCache.size()).isEqualTo(1),
				() -> assertThat(lruCache.contains(inputId))
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
		
		lruCache.put(1L, cachingComment1);
		lruCache.put(2L, cachingComment2);
		lruCache.put(3L, cachingComment3);
		
		Long inputId = 2L;
		Comment cachingComment4 = Comment.builder()
				.id(inputId)
				.text("Fours cashed element")
				.build();
		
		assertAll(
				() -> assertThatCode(() -> lruCache.put(inputId, cachingComment4)).doesNotThrowAnyException(),
				() -> assertThat(lruCache.size()).isEqualTo(3),
				() -> assertThat(lruCache.contains(inputId)).isTrue()
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
		
		lruCache.put(1L, cachingComment1);
		lruCache.put(2L, cachingComment2);
		lruCache.put(3L, cachingComment3);
		
		Long inputId = 4L;
		Comment cachingComment4 = Comment.builder()
				.id(inputId)
				.text("Fours cashed element")
				.build();
		
		assertAll(
				() -> assertThatCode(() -> lruCache.put(inputId, cachingComment4)).doesNotThrowAnyException(),
				() -> assertThat(lruCache.size()).isEqualTo(3),
				() -> assertThat(lruCache.contains(inputId)).isTrue(),
				() -> assertThat(lruCache.contains(1L)).isFalse()
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
		
		lruCache.put(1L, cachingComment1);
		lruCache.put(2L, cachingComment2);
		lruCache.put(3L, cachingComment3);
		
		Long inputKey = 2L;
		Comment expectedCachingComment = Comment.builder()
								.id(2L)
								.text("Second cashed element")
								.build();
		
		Comment actualCachingComment = lruCache.get(inputKey);
		
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
		
		lruCache.put(1L, cachingComment1);
		lruCache.put(2L, cachingComment2);
		
		Long inputKey = 5L;
		
		Comment actualComment = lruCache.get(inputKey);
		
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
		
		lruCache.put(1L, cachingComment1);
		lruCache.put(2L, cachingComment2);
		lruCache.put(3L, cachingComment3);
		assertAll(
				() -> assertThatCode(() -> lruCache.remove(3L)).doesNotThrowAnyException(),
				() -> assertThat(lruCache.size()).isEqualTo(2)
		);
		
	}
	
}
