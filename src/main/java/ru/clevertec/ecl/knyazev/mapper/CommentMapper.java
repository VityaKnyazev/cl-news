package ru.clevertec.ecl.knyazev.mapper;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
	
	@Named(value = "CommentDTOWithNews")
	@Mapping(target = "newsDTO.comments", ignore = true)
	@Mapping(source = "news", target = "newsDTO")
	@Mapping(source = "news.time", target = "newsDTO.createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@Mapping(source = "time", target = "createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	CommentDTO toCommentDTO(Comment comment);
	
	@Named(value = "CommentDTOWithoutNews")
	@Mapping(target = "newsDTO", ignore = true)
	@Mapping(source = "time", target = "createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	CommentDTO toCommentDTOWithoutNews(Comment comment);
		
	@Mapping(target = "news.comments", ignore = true)
	@Mapping(source = "newsDTO", target = "news")
	@Mapping(source = "newsDTO.createDate", target = "news.time", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@Mapping(source = "createDate", target = "time", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	Comment toComment(CommentDTO comment);
	
	@IterableMapping(qualifiedByName = "CommentDTOWithNews")
	List<CommentDTO> toCommentsDTO(List<Comment> comments);	
	
	@IterableMapping(qualifiedByName = "CommentDTOWithoutNews")
	List<CommentDTO> toCommentsDTOWithoutNews(List<Comment> comments);	
	
	List<Comment> toComments(List<CommentDTO> commentsDTO);
}
