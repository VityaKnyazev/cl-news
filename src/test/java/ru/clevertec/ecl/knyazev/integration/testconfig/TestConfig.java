package ru.clevertec.ecl.knyazev.integration.testconfig;

import java.util.List;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.clevertec.ecl.knyazev.config.SecurityWebConfig;
import ru.clevertec.ecl.knyazev.config.WebConfig;

@ImportAutoConfiguration({ FeignAutoConfiguration.class })
@Import(value = { WebConfig.class, SecurityWebConfig.class })
@EnableWebMvc
@EnableJpaRepositories("ru.clevertec.ecl.knyazev.repository")
@ComponentScan(basePackages = { "ru.clevertec.ecl.knyazev.service", "ru.clevertec.ecl.knyazev.mapper",
		"ru.clevertec.ecl.knyazev.controller", "ru.clevertec.ecl.knyazev.config.connection", "ru.clevertec.ecl.knyazev.token" })
public class TestConfig implements WebMvcConfigurer {
	
	@Bean
	ObjectMapper objectMapper() {

		return new Jackson2ObjectMapperBuilder()
									    .build();
	}
	
	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(new PageableHandlerMethodArgumentResolver());
	}
}
