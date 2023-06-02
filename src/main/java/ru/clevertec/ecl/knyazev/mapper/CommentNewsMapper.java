package ru.clevertec.ecl.knyazev.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

@Mapper(componentModel = "spring")
public interface CommentNewsMapper {
	
	public default News toNews(List<Comment> comments) {
		
		if (comments == null || comments.isEmpty()) {
			return null;
		}
		
		News news = comments.get(0).getNews();
		
		comments.forEach(c -> c.setNews(null));
		
		news.setComments(comments);
		
		return news;
	}

}
