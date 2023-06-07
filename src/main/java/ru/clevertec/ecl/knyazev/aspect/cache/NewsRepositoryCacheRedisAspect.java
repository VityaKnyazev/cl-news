package ru.clevertec.ecl.knyazev.aspect.cache;

import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.data.redis.core.RedisTemplate;

import ru.clevertec.ecl.knyazev.entity.News;

@Aspect
public class NewsRepositoryCacheRedisAspect extends RepositoryCacheAspect<News> {
	
	private static final String HASH_KEY = "NEWS";
	
	private RedisTemplate<Long, News> newsRedisTemplate;
	
	public NewsRepositoryCacheRedisAspect(RedisTemplate<Long, News> newsRedisTemplate) {
		this.newsRedisTemplate = newsRedisTemplate;
	}
	
	@Override
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.findById(..))")
	void searchRepositoryMethods() {}

	@Override
	@Pointcut(value = "execution(public * ru.clevertec.ecl.knyazev.repository.NewsRepository.save(*))")
	void saveOrUpdateRepositoryMethods() {}

	@Override
	@Pointcut(value = "execution(public void ru.clevertec.ecl.knyazev.repository.NewsRepository.delete(*))")
	void deleteRepositoryMethods() {}

	@Override
	Optional<News> cacheSearch(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		return Optional.empty();
	}

	@Override
	News cacheSaveOrUpdate(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	void cacheDelete(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
		// TODO Auto-generated method stub
		
	}

}
