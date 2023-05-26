package ru.clevertec.ecl.knyazev.dto.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import ru.clevertec.ecl.knyazev.dto.CommentDTO;
import ru.clevertec.ecl.knyazev.entity.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {
	
	@Mapping(target = "newsDTO.comments", ignore = true)
	@Mapping(source = "news", target = "newsDTO")
	@Mapping(source = "news.time", target = "newsDTO.createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@Mapping(source = "time", target = "createDate", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	CommentDTO toCommentDTO(Comment comment);
	
	
	@Mapping(target = "news.comments", ignore = true)
	@Mapping(source = "newsDTO", target = "news")
	@Mapping(source = "newsDTO.createDate", target = "news.time", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	@Mapping(source = "createDate", target = "time", dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSS")
	Comment toComment(CommentDTO comment);
	
	List<CommentDTO> toCommentsDTO(List<Comment> comments);
	
	List<Comment> toComments(List<CommentDTO> commentsDTO);
}
