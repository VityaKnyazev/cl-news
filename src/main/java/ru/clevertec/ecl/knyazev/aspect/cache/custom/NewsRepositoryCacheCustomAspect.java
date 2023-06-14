package ru.clevertec.ecl.knyazev.aspect.cache.custom;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;

import jakarta.persistence.PersistenceContext;
import ru.clevertec.ecl.knyazev.aspect.cache.custom.CustomCacheManager.DestinationClass;
import ru.clevertec.ecl.knyazev.entity.News;

@Aspect
public class NewsRepositoryCacheCustomAspect {
	
	@PersistenceContext
	Session session;
	
	private CustomCacheManager customCacheManager;
	
	public NewsRepositoryCacheCustomAspect(CustomCacheManager customCacheManager) {
		this.customCacheManager = customCacheManager;
	}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.findById(..))")
	private void findByIdMethod() {}

	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.save(*))")
	private void saveMethod() {}

	@Pointcut(value = "execution(public void ru.clevertec.ecl.knyazev.repository.NewsRepository.delete(*))")
	private void deleteMethod() {}

	@SuppressWarnings("unchecked")
	@Around(value = "findByIdMethod()")
	Optional<News> cacheAroundFindByIdMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Long newsId = (Long) proceedingJoinPoint.getArgs()[0];
		
		Optional<News> news = Optional.ofNullable(customCacheManager.getNews(newsId));
		
		if (news.isEmpty()) {
			news = (Optional<News>) proceedingJoinPoint.proceed();
			
			if (news.isPresent()) {
				customCacheManager.addNews(news.get());
			}
		}
		
		return news;
	}
	
	@Around(value = "saveMethod()")
	News cacheAroundSaveMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		News newsDB = (News) proceedingJoinPoint.proceed();
		
		customCacheManager.addNews(newsDB);		
		
		return newsDB;
	}
	
	@Around(value = "deleteMethod()")
	void cacheAroundDeleteMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Long deletingId = ((News) proceedingJoinPoint.getArgs()[0]).getId();
		
		News deletingNewsDB = session.get(News.class, deletingId);
		
		List<Long> boundCommentsId = deletingNewsDB.getComments().stream()
				                                                 .map(comment -> comment.getId())
				                                                 .toList();
		
		proceedingJoinPoint.proceed();
		
		customCacheManager.remove(DestinationClass.NEWS, deletingId, boundCommentsId);
		
	}
	
}
