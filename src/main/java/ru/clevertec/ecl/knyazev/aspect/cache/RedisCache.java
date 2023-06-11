package ru.clevertec.ecl.knyazev.aspect.cache;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Abstract class for realization redis caching mechanism using aspects.
 * 
 * @author Vitya Knyazev
 *
 */

@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public abstract class RedisCache {
	
	RedisTemplate<String, Object> redisTemplate;
	
	static final String NEWS_KEY = "NEWS";
	static final String NEWS_LIST_KEY = "LIST_NEWS";
	static final String NEWS_PAGE_KEY = "PAGE_NEWS";
	
	static final String COMMENT_KEY = "COMMENT";
	static final String COMMENT_LIST_KEY = "LIST_COMMENTS";
	static final String COMMENT_PAGE_KEY = "PAGE_COMMENTS";
	
	/**
	 * 
	 * Calculate hash key on method args using proceedingJoinPoint
	 * for saving composite object (like List and etc) in cache store.
	 * 
	 * @param proceedingJoinPoint data
	 * @return Integer calculated key
	 * @throws IllegalArgumentException if calculation hash failed.
	 * 
	 */
	Integer calculateKey(ProceedingJoinPoint proceedingJoinPoint) {
		Object[] args = proceedingJoinPoint.getArgs();

		if (args == null || args.length == 0) {
			throw new IllegalArgumentException("Error. Can't calculate key for caching element");
		}

		return Objects.hash(args);
	}
	
	/**
	 * 
	 * Delete from redis cache composite object like List or Page by redis store key and object id.
	 * Searching in redis cache by redisStoreKey hash keys of composite objects by object id.
	 * Then deleted hash keys with composite objects by object id.
	 * 
	 * @param <T> object entity
	 * @param redisStoreKey key for storing composite objects Like List<T> entities or 
	 *        Page<T> entities.
	 * @param storedObjectDeletingId id of stored in redis cache entity.
	 */
	@SuppressWarnings("unchecked")
	<T> void deleteFromRedisCache(String redisStoreKey, Long storedObjectDeletingId) {
		
		List<Integer> deletingKeys = new ArrayList<>();

		redisTemplate.opsForHash().entries(redisStoreKey).forEach((k, v) -> {
		
		Stream<T> storedObjects = null;	
		
		if (v instanceof Page) {
			storedObjects = (Stream<T>) ((Page<T>) v).stream();
		} else if (v instanceof List) {
			storedObjects = (Stream<T>) ((List<T>) v).stream();
		} else {
			throw new ClassCastException("Error. Can't convert from redis stored value to Page or List");
		}

		if (storedObjects.anyMatch(entity -> {
			try {
				return entity.getClass().getDeclaredMethod("getId").invoke(entity).equals(storedObjectDeletingId);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException e) {
				log.error("Error when calling metod getById() on entity: {}", e.getMessage(), e);
				return false;
			}
			})) {
				deletingKeys.add((Integer) k);
			}
			
		});
		
		deletingKeys.stream().forEach(key -> redisTemplate.opsForHash().delete(redisStoreKey, key));
		
	}
	
		
}
