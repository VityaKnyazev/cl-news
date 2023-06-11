package ru.clevertec.ecl.knyazev.aspect.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.persistence.PersistenceContext;
import ru.clevertec.ecl.knyazev.entity.News;

@Aspect
public class NewsRepositoryCacheRedisAspect extends RedisCache {
	
	@PersistenceContext
	private Session session;
		
	public NewsRepositoryCacheRedisAspect(RedisTemplate<String, Object> redisTemplate) {
		super(redisTemplate);
	}

	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.findById(..))")
	private void findByIdMethod() {}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.findAll(*))")
	private void findAllMethod() {}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.findAllByPartNewsText(..))")
	private void findAllByPartNewsTextMethod() {}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.save(*))")
	private void saveMethod() {}
	
	@Pointcut(value = "execution(public void ru.clevertec.ecl.knyazev.repository.NewsRepository.delete(*))")
	private void deleteMethod() {}

	@Around(value = "findByIdMethod()")
	Optional<News> cacheAroundFindByIdMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		Optional<News> newsWrap = Optional.empty();

		Long newsId = (long) proceedingJoinPoint.getArgs()[0];

		if (redisTemplate.opsForHash().hasKey(NEWS_KEY, newsId)) {
			newsWrap = Optional.of((News) redisTemplate.opsForHash().get(NEWS_KEY, newsId));
		} else {

			@SuppressWarnings("unchecked")
			Optional<News> newsDBWrap = (Optional<News>) proceedingJoinPoint.proceed();

			if (newsDBWrap.isPresent()) {
				redisTemplate.opsForHash().put(NEWS_KEY, newsId, newsDBWrap.get());
				newsWrap = newsDBWrap;
			}

		}

		return newsWrap;
	}
	
	@SuppressWarnings("unchecked")
	@Around(value = "findAllMethod()")
	Page<News> cacheAroundfindAllMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Page<News> news = Page.empty();

		Integer cachingKey = calculateKey(proceedingJoinPoint);

		if (redisTemplate.opsForHash().hasKey(NEWS_PAGE_KEY, cachingKey)) {
			news = (Page<News>) redisTemplate.opsForHash().get(NEWS_PAGE_KEY, cachingKey);
		} else {
			news = (Page<News>) proceedingJoinPoint.proceed();

			if (!news.isEmpty()) {
				redisTemplate.opsForHash().put(NEWS_PAGE_KEY, cachingKey, news);
			}
		}

		return news;
	}
	
	@SuppressWarnings("unchecked")
	@Around(value = "findAllByPartNewsTextMethod()")
	List<News> cacheAroundFindAllByPartNewsTextMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		List<News> news = new ArrayList<>();

		Integer cachingKey = calculateKey(proceedingJoinPoint);

		if (redisTemplate.opsForHash().hasKey(NEWS_LIST_KEY, cachingKey)) {
			news = (List<News>) redisTemplate.opsForHash().get(NEWS_LIST_KEY, cachingKey);
		} else {
			news = (List<News>) proceedingJoinPoint.proceed();

			if (!news.isEmpty()) {
				redisTemplate.opsForHash().put(NEWS_LIST_KEY, cachingKey, news);
			}
		}

		return news;
	}
	
	@Around(value = "saveMethod()")
	News cacheAroundSaveMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		News savedNews = (News) proceedingJoinPoint.proceed();		

		redisTemplate.opsForHash().put(NEWS_KEY, savedNews.getId(), savedNews);

		return savedNews;
	}

	@Around(value = "deleteMethod()")
	void cacheAroundDeleteMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		Long newsDeletingId = ((News) proceedingJoinPoint.getArgs()[0]).getId();
		
		News newsDB = session.get(News.class, newsDeletingId);
		
		List<Long> commentDeletingIds = newsDB.getComments().stream().map(c -> c.getId()).toList();

		proceedingJoinPoint.proceed();

		redisTemplate.opsForHash().delete(NEWS_KEY, newsDeletingId);
		deleteFromRedisCache(NEWS_PAGE_KEY, newsDeletingId);
		deleteFromRedisCache(NEWS_LIST_KEY, newsDeletingId);
		
		commentDeletingIds.stream().forEach(commentDelId -> {
			redisTemplate.opsForHash().delete(COMMENT_KEY, commentDelId);
			deleteFromRedisCache(COMMENT_PAGE_KEY, commentDelId);
			deleteFromRedisCache(COMMENT_LIST_KEY, commentDelId);
		});
	
	}

}
