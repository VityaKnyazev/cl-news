package ru.clevertec.ecl.knyazev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import ru.clevertec.ecl.knyazev.aspect.cache.CommentRepositoryCacheRedisAspect;
import ru.clevertec.ecl.knyazev.aspect.cache.NewsRepositoryCacheRedisAspect;
import ru.clevertec.ecl.knyazev.entity.Comment;
import ru.clevertec.ecl.knyazev.entity.News;

@ConditionalOnExpression(
		  "${aspect.cache.enable:true} and '${aspect.cache.type}'.equals('redis')"
		)
@Configuration
public class RedisCacheConfig {	
	
	@Autowired
	private LettuceConnectionFactory lettuceConnectionFactory;
	
	@Bean
	RedisTemplate<Long, Comment> commentRedisTemplate() {
	    RedisTemplate<Long, Comment> commentRedisTemplate = new RedisTemplate<Long, Comment>();
	    commentRedisTemplate.setConnectionFactory(lettuceConnectionFactory);
	    commentRedisTemplate.afterPropertiesSet();
	    return commentRedisTemplate;
	}
	
	@Bean
	RedisTemplate<Long, News> newsRedisTemplate() {
	    RedisTemplate<Long, News> newsRedisTemplate = new RedisTemplate<Long, News>();
	    newsRedisTemplate.setConnectionFactory(lettuceConnectionFactory);
	    newsRedisTemplate.afterPropertiesSet();
	    return newsRedisTemplate;
	}


	@Bean
	CommentRepositoryCacheRedisAspect commentRepositoryCacheRedisAspect() {
		return new CommentRepositoryCacheRedisAspect(commentRedisTemplate());
	}
	
	@Bean
	NewsRepositoryCacheRedisAspect newsRepositoryCacheRedisAspect() {
		return new NewsRepositoryCacheRedisAspect(newsRedisTemplate());
	}
	
}
