package ru.clevertec.ecl.knyazev.aspect.log;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class ControllerLoggerAspect {

	@Pointcut(value = "execution(org.springframework.http.ResponseEntity ru.clevertec.ecl.knyazev.controller.*.*(..))")
	public void allControllerMethods() {
	}

	@Around(value = "allControllerMethods()")
	public ResponseEntity<?> logAroundMethod(ProceedingJoinPoint joinPoint) throws Throwable {

		String methodName = joinPoint.getSignature().getName();
		Object[] args = joinPoint.getArgs();

		log.debug("Request: {}", methodName + Arrays.toString(args));

		try {
			Object objectResult = joinPoint.proceed();			
			
			ResponseEntity<?> responseEntity = objectResult != null
					                           ? (ResponseEntity<?>) objectResult
					                           : null;
			
			log.debug("Response: {}", responseEntity);
			
			return responseEntity;
			
		} catch (Throwable t) {
			log.error("Response: {} {} {}", t.getMessage(), System.lineSeparator(), t);			
			throw t;
		}	

	}

}
