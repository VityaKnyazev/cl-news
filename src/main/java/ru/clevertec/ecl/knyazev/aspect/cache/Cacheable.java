package ru.clevertec.ecl.knyazev.aspect.cache;

import java.util.Objects;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 
 * Abstract class for realization caching mechanism using aspects.
 * 
 * @author Vitya Knyazev
 *
 * @param <T> caching object
 */

public abstract class Cacheable {
	
	static final String NEWS_KEY = "NEWS";
	static final String NEWS_LIST_KEY = "LIST_NEWS";
	
	static final String COMMENT_KEY = "COMMENT";
	static final String COMMENT_LIST_KEY = "COMMENTS";
	
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
	
	
		
}
