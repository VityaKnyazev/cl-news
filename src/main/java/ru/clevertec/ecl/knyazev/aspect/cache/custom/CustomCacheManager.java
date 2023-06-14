package ru.clevertec.ecl.knyazev.aspect.cache.custom;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ru.clevertec.ecl.knyazev.cache.Cache;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

/**
 * 
 * Custom cache manager for working with bound entities that has to be cached.
 * Support cache operations for bound entities like add to cache, get from cahce,
 * remove from cache. 
 * Class was simplified for caching bound News and Comment entities.
 * 
 * @author Vitya Knyazev
 *
 */
@NoArgsConstructor
@AllArgsConstructor
public class CustomCacheManager {

	private Cache<Long, Comment> commentCache;

	private Cache<Long, News> newsCache;
	
	public void addComment(Comment comment) {
		commentCache.put(comment.getId(), comment);
	}
	
	public void addNews(News news) {
		newsCache.put(news.getId(), news);
	}
	
	public Comment getComment(Long commentId) {
		
		Comment cachedComment = null;
		
		if (commentCache.contains(commentId)) {
			cachedComment = commentCache.get(commentId);
		}
		
		return cachedComment;
	}
	
	public News getNews(Long newsId) {
		
		News cachedNews = null;
		
		if (newsCache.contains(newsId)) {
			cachedNews = newsCache.get(newsId);
		}
		
		return cachedNews;
	}
	
	/**
	 * 
	 * Remove entity and its's bound entity from cache 
	 * 
	 * @param destinationClass class which entity have to be removed from cache
	 * @param removingId id of removing from cache entity
	 * @param boundIds bound entity id's that also should be removed from cache
	 */
	public void remove(DestinationClass destinationClass, Long removingId, List<Long> boundIds) {
		
		if (destinationClass.equals(DestinationClass.COMMENT)) {
			commentCache.remove(removingId);
			boundIds.forEach(id -> newsCache.remove(id));
		} else if (destinationClass.equals(DestinationClass.NEWS)) {
			newsCache.remove(removingId);
			boundIds.forEach(id -> commentCache.remove(id));
		}
		
	}	


	public static enum DestinationClass {
		NEWS, COMMENT
	}

}
