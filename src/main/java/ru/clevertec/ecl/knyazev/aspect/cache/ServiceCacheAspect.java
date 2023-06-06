package ru.clevertec.ecl.knyazev.aspect.cache;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

//TODO Add cache for service layer around method depend on configuration
@ConditionalOnExpression(
		  "${aspect.caching.enable:true} and '${aspect.caching.type}'.equals('redis')"
		)
@Aspect
@Component
public class ServiceCacheAspect {
	
	//Add mappers?
	//Add repostory?
	
	@Autowired
	private RedisTemplate<?, ?> redisTemplate;
	
	//Pointcut for methods showById, add, change, remove
}
