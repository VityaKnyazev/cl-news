package ru.clevertec.ecl.knyazev.aspect.cache;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * 
 * Abstract class for realization AOP caching strategy on repositories methods.
 * 
 * @author Vitya Knyazev
 *
 * @param <T> caching object
 */

@Aspect
public abstract class RepositoryCacheAspect<T> {
	
	private static final String CACHING_METHOD_NOT_FOUND = "Error. Caching method not found!";
	
	
	@Around(value = "searchRepositoryMethods() || saveOrUpdateRepositoryMethods() || "
			  + "deleteRepositoryMethods()")
	public Object cacheAroundMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
	
	String methodName = proceedingJoinPoint.getSignature().getName();
			
	return switch(methodName) {
		case "findById" -> cacheSearch(proceedingJoinPoint);
		case "save" -> cacheSaveOrUpdate(proceedingJoinPoint);
		case "delete" -> {
			cacheDelete(proceedingJoinPoint);
			yield null;
		}
		default -> throw new IllegalArgumentException(CACHING_METHOD_NOT_FOUND);
	};
	
	}
	
	/**
	 * 
	 * Pointcut for searching entity methods.
	 * Use @Pointcut on overriding method to declare
	 * expression.
	 * 
	 */
	abstract void searchRepositoryMethods();
	
	/**
	 * 
	 * Pointcut for saving or updating entity methods.
	 * Use @Pointcut on overriding method to declare
	 * expression.
	 * 
	 */
	abstract void saveOrUpdateRepositoryMethods();
	
	
	/**
	 * 
	 * Pointcut for deleting entity methods.
	 * Use @Pointcut on overriding method to declare
	 * expression.
	 * 
	 */
	abstract void deleteRepositoryMethods();
	
	/**
	 * Advice with caching logic for searching entity methods.
	 * Search entity in cache or database.
	 *  
	 * @param proceedingJoinPoint expose proceed() method to run around advice
	 * @return Optional<T> entity wrap form cache or database or Optional empty
	 * @throws Throwable
	 */
	abstract Optional<T> cacheSearch(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;
	
	
	/**
	 * 
	 * Advice with caching logic for saving or updating entity methods.
	 * Save or update entity in cache and database.
	 * 
	 * @param proceedingJoinPoint
	 * @return T saved or updated entity from cache or from database
	 * @throws Throwable if caching on saving or updating methods failed 
	 */
	abstract T cacheSaveOrUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;
		
	
	/**
	 * 
	 * Advice with caching logic for deleting entity methods. 
	 * Delete entity from cache and database.
	 * 
	 * @param proceedingJoinPoint expose proceed() method to run around advice
	 * @throws Throwable if caching on deleting methods failed 
	 */
	abstract void cacheDelete(ProceedingJoinPoint proceedingJoinPoint) throws Throwable;	
}
