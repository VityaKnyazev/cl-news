package ru.clevertec.ecl.knyazev.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import ru.clevertec.ecl.knyazev.aspect.cache.redis.CommentRepositoryCacheRedisAspect;
import ru.clevertec.ecl.knyazev.aspect.cache.redis.NewsRepositoryCacheRedisAspect;

@ConditionalOnExpression(
		  "${aspect.cache.enable:true} and '${aspect.cache.type}'.equals('redis')"
		)
@Configuration
public class RedisCacheConfig {	
	
	@Autowired
	private LettuceConnectionFactory lettuceConnectionFactory;
	
	@Bean
	RedisTemplate<String, Object> redisTemplate() {
	    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
	    redisTemplate.setConnectionFactory(lettuceConnectionFactory);
	    redisTemplate.afterPropertiesSet();
	    return redisTemplate;
	}

	@Bean
	CommentRepositoryCacheRedisAspect commentRepositoryCacheRedisAspect() {
		return new CommentRepositoryCacheRedisAspect(redisTemplate());
	}
	
	@Bean
	NewsRepositoryCacheRedisAspect newsRepositoryCacheRedisAspect() {
		return new NewsRepositoryCacheRedisAspect(redisTemplate());
	}
	
}
