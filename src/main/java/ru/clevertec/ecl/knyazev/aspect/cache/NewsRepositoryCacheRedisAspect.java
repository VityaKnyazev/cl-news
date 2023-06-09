package ru.clevertec.ecl.knyazev.aspect.cache;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.persistence.PersistenceContext;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

@Aspect
public class NewsRepositoryCacheRedisAspect extends Cacheable {
	
	@PersistenceContext
	private Session session;
	
	private RedisTemplate<String, Object> redisTemplate;
	
	public NewsRepositoryCacheRedisAspect(RedisTemplate<String, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.findAll(*))")
	private void findAllMethod() {}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.findAllByPartNewsText(..))")
	private void findAllByPartNewsTextMethod() {}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.save(*))")
	private void saveMethod() {}
	
	@Pointcut(value = "execution(public void ru.clevertec.ecl.knyazev.repository.NewsRepository.delete(*))")
	private void deleteMethod() {}

	
	@SuppressWarnings("unchecked")
	@Around(value = "findAllMethod()")
	Page<News> cacheAroundfindAllMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Page<News> news = Page.empty();

		Integer cachingKey = calculateKey(proceedingJoinPoint);

		if (redisTemplate.opsForHash().hasKey(NEWS_LIST_KEY, cachingKey)) {
			news = (Page<News>) redisTemplate.opsForHash().get(NEWS_LIST_KEY, cachingKey);
		} else {
			news = (Page<News>) proceedingJoinPoint.proceed();

			if (!news.isEmpty()) {
				redisTemplate.opsForHash().put(NEWS_LIST_KEY, cachingKey, news);
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

		//deleting news from cache
		redisTemplate.opsForHash().delete(NEWS_KEY, newsDeletingId);

		List<Integer> newsDeletingListKeys = new ArrayList<>();

		redisTemplate.opsForHash().entries(NEWS_LIST_KEY).forEach((k, v) -> {
			@SuppressWarnings("unchecked")
			List<News> news = (List<News>) v;

			if (news.stream().anyMatch(c -> c.getId().equals(newsDeletingId))) {
				newsDeletingListKeys.add((Integer) k);
			}

		});

		newsDeletingListKeys.stream().forEach(key -> redisTemplate.opsForHash().delete(NEWS_LIST_KEY, key));
		
		//deleting comments from cache	
		if (!commentDeletingIds.isEmpty()) {
			List<Integer> commentsDeletingListKeys = new ArrayList<>();
			
			commentDeletingIds.forEach(id -> redisTemplate.opsForHash().delete(COMMENT_KEY, id));
			
			redisTemplate.opsForHash().entries(COMMENT_LIST_KEY).forEach((k, v) -> {
				@SuppressWarnings("unchecked")
				List<Comment> comments = (List<Comment>) v;

				if (comments.stream().anyMatch(c -> commentDeletingIds.contains(c.getId()))) {
					commentsDeletingListKeys.add((Integer) k);
				}

			});
			
			commentsDeletingListKeys.stream().forEach(key -> redisTemplate.opsForHash().delete(COMMENT_LIST_KEY, key));
		}
	
	}

}
