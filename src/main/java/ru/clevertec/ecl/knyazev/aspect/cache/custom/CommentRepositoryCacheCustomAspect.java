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
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

@Aspect
public class CommentRepositoryCacheCustomAspect {
	
	@PersistenceContext
	Session session;
	
	private CustomCacheManager customCacheManager;
	
	public CommentRepositoryCacheCustomAspect(CustomCacheManager customCacheManager) {
		this.customCacheManager = customCacheManager;
	}
	
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.findById(..))")
	private void findByIdMethod() {}

	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.CommentRepository.save(*))")
	private void saveMethod() {}

	@Pointcut(value = "execution(public void ru.clevertec.ecl.knyazev.repository.CommentRepository.delete(*))")
	private void deleteMethod() {}

	@SuppressWarnings("unchecked")
	@Around(value = "findByIdMethod()")
	Optional<Comment> cacheAroundFindByIdMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Long commentId = (Long) proceedingJoinPoint.getArgs()[0];
		
		Optional<Comment> comment = Optional.ofNullable(customCacheManager.getComment(commentId));
		
		if (comment.isEmpty()) {
			comment = (Optional<Comment>) proceedingJoinPoint.proceed();
			
			if (comment.isPresent()) {
				customCacheManager.addComment(comment.get());
			}
		}
		
		return comment;
	}
	
	@Around(value = "saveMethod()")
	Comment cacheAroundSaveMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Comment savingOrUpdatingComment = ((Comment) proceedingJoinPoint.getArgs()[0]);
		Long savingOrUpdatingCommentId = savingOrUpdatingComment.getId();
		Long newsId = savingOrUpdatingComment.getNews().getId();
		
		Comment commentDB = (Comment) proceedingJoinPoint.proceed();
		
		if (savingOrUpdatingCommentId == null) {
			News dbNews = session.get(News.class, newsId);
			commentDB.setNews(dbNews);
		}
		
		customCacheManager.addComment(commentDB);		
		
		return commentDB;
	}
	
	@Around(value = "deleteMethod()")
	void cacheAroundDeleteMethod(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		
		Long deletingId = ((Comment) proceedingJoinPoint.getArgs()[0]).getId();
		
		Comment deletingCommentDB = session.get(Comment.class, deletingId);
		
		Long boundNewsId = deletingCommentDB.getNews().getId();
		
		proceedingJoinPoint.proceed();
		
		customCacheManager.remove(DestinationClass.COMMENT, deletingId, List.of(boundNewsId));
		
	}
	
}
