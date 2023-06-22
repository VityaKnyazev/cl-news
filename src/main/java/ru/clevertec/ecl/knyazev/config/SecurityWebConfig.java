package ru.clevertec.ecl.knyazev.config;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.clevertec.ecl.knyazev.client.UserClient;
import ru.clevertec.ecl.knyazev.controller.ExceptionController.ErrorMessage;
import ru.clevertec.ecl.knyazev.filter.JWTAuthenticationFilter;
import ru.clevertec.ecl.knyazev.mapper.SecurityUserMapper;
import ru.clevertec.ecl.knyazev.service.SecurityUserService;
import ru.clevertec.ecl.knyazev.token.JwtUtil;

@Configuration
@EnableWebSecurity
@ConfigurationProperties(value = "spring.security.url")
@NoArgsConstructor
@Setter
public class SecurityWebConfig {
	
	private static final String NEWS_URI = "/news";
	private static final String SINGLE_NEWS_URI = "/news/\\d+";
	
	private static final String COMMENTS_URI = "/comments";
	private static final String COMMENT_URI = "/comments/\\d+";
	
	 private static final String[] SWAGER_WHITE_LIST = {
	            // -- Swagger UI v2
	            "/v2/api-docs",
	            "/swagger-resources",
	            "/swagger-resources/**",
	            "/configuration/ui",
	            "/configuration/security",
	            "/swagger-ui.html",
	            "/webjars/**",
	            // -- Swagger UI v3 (OpenAPI)
	            "/v3/api-docs/**",
	            "/swagger-ui/**"
	  };
	
	private String authenticationURL;

	
	private String registrationURL;	
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, 
			                                ObjectMapper objectMapper, 
			                                JWTAuthenticationFilter jwtAuthenticationFilter) throws Exception {
		 
		SecurityFilterChain securityFilterChain = 
				httpSecurity.cors().and().csrf().disable()
				
		.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()

		.exceptionHandling().authenticationEntryPoint((request, response, ex) -> {
	
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.setContentType(MediaType.APPLICATION_JSON_VALUE);
			
			try (OutputStream outputStream = response.getOutputStream()) {
				ErrorMessage errorMessage = ErrorMessage.builder()
														.message(ex.getMessage())
														.statusCode(HttpStatus.UNAUTHORIZED.value())
														.timestamp(new Date())
														.build();
				
				objectMapper.writeValue(outputStream, errorMessage);
				
				outputStream.flush();
			}

		}).and() 
		
		.authorizeHttpRequests()
		
		.requestMatchers(req -> (req.getMethod().equals(HttpMethod.POST.name())
								|| req.getMethod().equals(HttpMethod.PUT.name())
								|| req.getMethod().equals(HttpMethod.DELETE.name()))
								&& req.getRequestURI().equals(NEWS_URI))
								.hasAnyRole("ADMIN", "JOURNALIST")
								
		.requestMatchers(req -> (req.getMethod().equals(HttpMethod.POST.name())
										|| req.getMethod().equals(HttpMethod.PUT.name())
										|| req.getMethod().equals(HttpMethod.DELETE.name()))
										&& req.getRequestURI().equals(COMMENTS_URI))
										.hasAnyRole("ADMIN", "SUBSCRIBER")
		
		.requestMatchers(req -> req.getMethod().equals(HttpMethod.GET.name()) 
				                && (req.getRequestURI().equals(NEWS_URI)
				                || req.getRequestURI().equals(COMMENTS_URI)
				                || req.getRequestURI().matches(SINGLE_NEWS_URI)
				                || req.getRequestURI().matches(COMMENT_URI))).permitAll()
		
		.requestMatchers(HttpMethod.GET, SWAGER_WHITE_LIST).permitAll()
		
		.requestMatchers(HttpMethod.POST, authenticationURL).permitAll()		
		.requestMatchers(HttpMethod.POST, registrationURL).permitAll()
		
		.anyRequest().authenticated()
		
		.and()	
		
		.addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
		
		.build();
		
		return 	securityFilterChain;
	}
	
	@Bean
	PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}
	
	@Bean
	SecurityUserService securityUserService(UserClient userClient, SecurityUserMapper securityUserMapperImpl) {
		return new SecurityUserService(userClient, securityUserMapperImpl);
	}
	
	@Bean
	AuthenticationProvider daoAuthenticationProvider(SecurityUserService securityUserService) {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider(passwordEncoder());
		daoAuthenticationProvider.setUserDetailsService(securityUserService);
		
		return daoAuthenticationProvider;
	}
	
	@Bean
	AuthenticationManager providerManager(AuthenticationProvider daoAuthenticationProvider) {
		
		ProviderManager providerManager = new ProviderManager(daoAuthenticationProvider);
		
		return providerManager;
	}
	
	@Bean
	JWTAuthenticationFilter jwtAuthenticationFilter(SecurityUserService securityUserService, JwtUtil jwtUtil) {
		return new JWTAuthenticationFilter(authenticationURL, securityUserService, jwtUtil);
	}
	
	@Bean
	public CorsFilter corsFilter() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("*"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return new CorsFilter(source);
	}
	
}
