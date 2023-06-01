package ru.clevertec.ecl.knyazev.dto.mapper;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.dto.NewsDTO;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

@Mapper(componentModel = "spring")
public abstract class NewsMapper {
		
	@Mapping(source = "time", target = "createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	public abstract NewsDTO toNewsDTO(News news);
	
	@Named(value = "NewsDTOsWithoutComments")
	@Mapping(source = "time", target = "createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@Mapping(target = "comments", ignore = true)
	public abstract NewsDTO toNewsDTOWithoutComments(News news);
	
	@Mapping(source = "createDate", target = "time", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	public abstract News toNews(NewsDTO newsDTO);
	
	public abstract List<NewsDTO> toNewsDTOs(List<News> news);
	
	@IterableMapping(qualifiedByName = "NewsDTOsWithoutComments")
	public abstract List<NewsDTO> toNewsDTOsWithoutComments(List<News> news);
	
	public abstract List<News> toNewsList(List<NewsDTO> newsDTOs);		
	
	@Mapping(target = "newsDTO", ignore = true)
	@Mapping(source = "time", target = "createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	protected abstract CommentDTO toCommentDTO(Comment comment);
		
	@Mapping(target = "news", ignore = true)
	@Mapping(source = "createDate", target = "time", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	protected abstract Comment toComment(CommentDTO commentDTO);
	
}
