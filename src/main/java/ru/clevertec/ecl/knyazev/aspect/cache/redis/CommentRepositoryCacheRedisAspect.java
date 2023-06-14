package ru.clevertec.ecl.knyazev.aspect.cache.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.persistence.PersistenceContext;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

@Aspect
public class CommentRepositoryCacheRedisAspect extends RedisCache {
	
	@PersistenceContext
	private Session session;

	public CommentRepositoryCacheRedisAspect(RedisTemplate<String, Object> redisTemplate) {
		super(redisTemplate);
	}

	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.findById(..))")
	private void findByIdMethod() {}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.findAll(*))")
	private void findAllMethod() {}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.findAllByPartCommentText(..))")
	private void findAllByPartCommentTextMethod() {}

	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.findAllByNewsId(..))")
	private void findAllByNewsIdMethod() {}

	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.save(*))")
	private void saveMethod() {}

	@Pointcut(value = "execution(public void ru.clevertec.ecl.knyazev.repository.CommentRepository.delete(*))")
	private void deleteMethod() {}

	@Around(value = "findByIdMethod()")
	Optional<Comment> cacheAroundFindByIdMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		Optional<Comment> commentWrap = Optional.empty();

		Long commentId = (long) proceedingJoinPoint.getArgs()[0];

		if (redisTemplate.opsForHash().hasKey(COMMENT_KEY, commentId)) {
			commentWrap = Optional.of((Comment) redisTemplate.opsForHash().get(COMMENT_KEY, commentId));
		} else {

			@SuppressWarnings("unchecked")
			Optional<Comment> commentDBWrap = (Optional<Comment>) proceedingJoinPoint.proceed();

			if (commentDBWrap.isPresent()) {
				initLazyProperty(commentDBWrap.get());

				redisTemplate.opsForHash().put(COMMENT_KEY, commentId, commentDBWrap.get());
				commentWrap = commentDBWrap;
			}

		}

		return commentWrap;
	}
	
	@SuppressWarnings("unchecked")
	@Around(value = "findAllMethod()")
		
		Page<Comment> cacheAroundfindAllMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Page<Comment> comments = Page.empty();

		Integer cachingKey = calculateKey(proceedingJoinPoint);

		if (redisTemplate.opsForHash().hasKey(COMMENT_PAGE_KEY, cachingKey)) {
			comments = (Page<Comment>) redisTemplate.opsForHash().get(COMMENT_PAGE_KEY, cachingKey);
		} else {
			comments = (Page<Comment>) proceedingJoinPoint.proceed();

			if (!comments.isEmpty()) {
				comments.stream().forEach(comment -> initLazyProperty(comment));
				redisTemplate.opsForHash().put(COMMENT_PAGE_KEY, cachingKey, comments);
			}
		}

		return comments;
	}

	@SuppressWarnings("unchecked")
	@Around(value = "findAllByPartCommentTextMethod() || findAllByNewsIdMethod()")
	List<Comment> cacheAroundfindAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		List<Comment> comments = new ArrayList<>();

		Integer cachingKey = calculateKey(proceedingJoinPoint);

		if (redisTemplate.opsForHash().hasKey(COMMENT_LIST_KEY, cachingKey)) {
			comments = (List<Comment>) redisTemplate.opsForHash().get(COMMENT_LIST_KEY, cachingKey);
		} else {
			comments = (List<Comment>) proceedingJoinPoint.proceed();

			if (!comments.isEmpty()) {				
				comments.stream().forEach(comment -> initLazyProperty(comment));				
				redisTemplate.opsForHash().put(COMMENT_LIST_KEY, cachingKey, comments);
			}
		}

		return comments;

	}

	@Around(value = "saveMethod()")
	Comment cacheAroundSaveMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Long savingId = ((Comment) proceedingJoinPoint.getArgs()[0]).getId();
		
		Comment savedComment = (Comment) proceedingJoinPoint.proceed();
		
		if (savingId == null) {
			News newsDB = session.get(News.class, savedComment.getNews().getId());
			savedComment.setNews(newsDB);
		} else {
			initLazyProperty(savedComment);
		}		

		redisTemplate.opsForHash().put(COMMENT_KEY, savedComment.getId(), savedComment);

		return savedComment;
	}

	@Around(value = "deleteMethod()")
	void cacheAroundDeleteMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		Long deletingId = ((Comment) proceedingJoinPoint.getArgs()[0]).getId();
		
		Comment commentDB = session.get(Comment.class, deletingId);
		
		Long newsDeletingId = commentDB.getNews().getId();

		proceedingJoinPoint.proceed();

		//Deleting comment from cache
		redisTemplate.opsForHash().delete(COMMENT_KEY, deletingId);
		deleteFromRedisCache(COMMENT_PAGE_KEY, deletingId);
		deleteFromRedisCache(COMMENT_LIST_KEY, deletingId);
		
		//deleting bound news from cache
		redisTemplate.opsForHash().delete(NEWS_KEY, newsDeletingId);
		deleteFromRedisCache(NEWS_PAGE_KEY, newsDeletingId);
		deleteFromRedisCache(NEWS_LIST_KEY, newsDeletingId);
	}

	private void initLazyProperty(Comment dbComment) {

		if (!Hibernate.isInitialized(dbComment.getNews())) {
			Hibernate.initialize(dbComment.getNews());
		}
		
	}

}
