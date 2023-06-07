package ru.clevertec.ecl.knyazev.aspect.cache;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.hibernate.Hibernate;
import org.springframework.data.redis.core.RedisTemplate;

import ru.clevertec.ecl.knyazev.entity.Comment;

@Aspect
public class CommentRepositoryCacheRedisAspect extends RepositoryCacheAspect<Comment> {
	
	private static final String HASH_KEY = "COMMENT";
	
	private RedisTemplate<Long, Comment> commentRedisTemplate;
	
	public CommentRepositoryCacheRedisAspect(RedisTemplate<Long, Comment> commentRedisTemplate) {
		this.commentRedisTemplate = commentRedisTemplate;
	}
	
	@Override
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.findById(..))")
	void searchRepositoryMethods() {}

	@Override
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.save(*))")
	void saveOrUpdateRepositoryMethods() {}

	@Override
	@Pointcut(value = "execution(public void ru.clevertec.ecl.knyazev.repository.CommentRepository.delete(*))")
	void deleteRepositoryMethods() {}

	@Override
	Optional<Comment> cacheSearch(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Optional<Comment> commentWrap = Optional.empty();
		
		Long commentId = (long) proceedingJoinPoint.getArgs()[0];
	
		
		if (commentRedisTemplate.opsForHash().hasKey(commentId, HASH_KEY)) {
			commentWrap =  Optional.of((Comment) commentRedisTemplate.opsForHash().get(commentId, HASH_KEY));
		} else {
			
			@SuppressWarnings("unchecked")
			Optional<Comment> commentDBWrap = (Optional<Comment>) proceedingJoinPoint.proceed();
						
			if (commentDBWrap.isPresent()) {
				Hibernate.initialize(commentDBWrap.get().getNews());
				
				commentRedisTemplate.opsForHash().put(commentId, HASH_KEY, commentDBWrap.get());
				commentWrap = commentDBWrap;
			}
			
		}

		return commentWrap;
	}

	@Override
	Comment cacheSaveOrUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

		Comment savedComment = (Comment) proceedingJoinPoint.proceed();
		Long savedCommentId = savedComment.getId();
		
		commentRedisTemplate.opsForHash().put(savedCommentId, HASH_KEY, savedComment);
		
		return savedComment;		
	}


	@Override
	void cacheDelete(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Long deletingId = ((Comment) proceedingJoinPoint.getArgs()[0]).getId();
		
		proceedingJoinPoint.proceed();
		
		commentRedisTemplate.opsForHash().delete(deletingId, HASH_KEY);
	}

}
