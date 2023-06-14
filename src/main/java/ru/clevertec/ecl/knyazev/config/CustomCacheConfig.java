package ru.clevertec.ecl.knyazev.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.Setter;
import ru.clevertec.ecl.knyazev.aspect.cache.custom.CommentRepositoryCacheCustomAspect;
import ru.clevertec.ecl.knyazev.aspect.cache.custom.CustomCacheManager;
import ru.clevertec.ecl.knyazev.aspect.cache.custom.NewsRepositoryCacheCustomAspect;
import ru.clevertec.ecl.knyazev.cache.CacheFactory;

@ConditionalOnExpression(
		  "${aspect.cache.enable:true} and '${aspect.cache.type}'.equals('custom')"
		)
@Configuration
@ConfigurationProperties(value = "aspect.cache.custom")
@Setter
public class CustomCacheConfig {
	
	private String algorithm;
	private Integer size;
	
	@Bean
	CacheFactory cacheFactory() {
		return new CacheFactory();
	}
	
	@Bean
	CustomCacheManager customCacheManager() {
		return new CustomCacheManager(cacheFactory().initCache(algorithm, size), cacheFactory().initCache(algorithm, size));
	}
	
	@Bean
	CommentRepositoryCacheCustomAspect commentRepositoryCacheCustomAspect() {
		return new CommentRepositoryCacheCustomAspect(customCacheManager());
	}
	
	@Bean
	NewsRepositoryCacheCustomAspect repositoryCacheCustomAspect() {
		return new NewsRepositoryCacheCustomAspect(customCacheManager());
	}
	
}
