package ru.clevertec.ecl.knyazev.aspect.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.springframework.data.redis.core.RedisTemplate;

import jakarta.persistence.PersistenceContext;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

@Aspect
public class CommentRepositoryCacheRedisAspect extends Cacheable {
	
	@PersistenceContext
	private Session session;

	private RedisTemplate<String, Object> commentRedisTemplate;

	public CommentRepositoryCacheRedisAspect(RedisTemplate<String, Object> commentRedisTemplate) {
		this.commentRedisTemplate = commentRedisTemplate;
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

		if (commentRedisTemplate.opsForHash().hasKey(COMMENT_KEY, commentId)) {
			commentWrap = Optional.of((Comment) commentRedisTemplate.opsForHash().get(COMMENT_KEY, commentId));
		} else {

			@SuppressWarnings("unchecked")
			Optional<Comment> commentDBWrap = (Optional<Comment>) proceedingJoinPoint.proceed();

			if (commentDBWrap.isPresent()) {
				initLazyProperty(commentDBWrap.get());

				commentRedisTemplate.opsForHash().put(COMMENT_KEY, commentId, commentDBWrap.get());
				commentWrap = commentDBWrap;
			}

		}

		return commentWrap;
	}

	@SuppressWarnings("unchecked")
	@Around(value = "findAllMethod() || findAllByPartCommentTextMethod() || findAllByNewsIdMethod()")
	List<Comment> cacheAroundfindAllMethods(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		List<Comment> comments = new ArrayList<>();

		Integer cachingKey = calculateKey(proceedingJoinPoint);

		if (commentRedisTemplate.opsForHash().hasKey(COMMENT_LIST_KEY, cachingKey)) {
			comments = (List<Comment>) commentRedisTemplate.opsForHash().get(COMMENT_LIST_KEY, cachingKey);
		} else {
			comments = (List<Comment>) proceedingJoinPoint.proceed();

			if (!comments.isEmpty()) {
				initLazyProperty(comments.get(0));
				commentRedisTemplate.opsForHash().put(COMMENT_LIST_KEY, cachingKey, comments);
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

		commentRedisTemplate.opsForHash().put(COMMENT_KEY, savedComment.getId(), savedComment);

		return savedComment;
	}

	@Around(value = "deleteMethod()")
	void cacheAroundDeleteMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		Long deletingId = ((Comment) proceedingJoinPoint.getArgs()[0]).getId();

		proceedingJoinPoint.proceed();

		commentRedisTemplate.opsForHash().delete(COMMENT_KEY, deletingId);

		List<Integer> deletingListKeys = new ArrayList<>();

		commentRedisTemplate.opsForHash().entries(COMMENT_LIST_KEY).forEach((k, v) -> {
			@SuppressWarnings("unchecked")
			List<Comment> comments = (List<Comment>) v;

			if (comments.stream().anyMatch(c -> c.getId().equals(deletingId))) {
				deletingListKeys.add((Integer) k);
			}

		});

		deletingListKeys.stream().forEach(key -> commentRedisTemplate.opsForHash().delete(COMMENT_LIST_KEY, key));

	}

	private void initLazyProperty(Comment dbComment) {

		if (!Hibernate.isInitialized(dbComment.getNews())) {
			Hibernate.initialize(dbComment.getNews());
		}
		
	}

}
