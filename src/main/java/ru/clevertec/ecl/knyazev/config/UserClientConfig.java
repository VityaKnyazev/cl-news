package ru.clevertec.ecl.knyazev.config;

import java.io.IOException;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.mediatype.hal.Jackson2HalModule;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import com.fasterxml.jackson.databind.deser.ValueInstantiator;

import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.Setter;


@EnableConfigurationProperties
@ConfigurationProperties(prefix = "spring.cloud.openfeign.client.config.user-client.authentication")
@Setter
public class UserClientConfig {
	
	private String username;
	
	private String password;
	
	@Bean
	public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
	    return new BasicAuthRequestInterceptor(username, password);
	}
	
	@Bean
	public Decoder feignDecoder() {
	    ObjectMapper mapper = new ObjectMapper()
	                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
	                    .registerModule(new Jackson2HalModule())
	                    .addHandler(new DeserializationProblemHandler() {


	                        @Override
	                        public Object handleMissingInstantiator(DeserializationContext ctxt, Class<?> instClass, ValueInstantiator valueInsta, JsonParser p, String msg) throws IOException {
	                            return super.handleMissingInstantiator(ctxt, instClass, valueInsta, p, msg);
	                        }

	                    });
	    
	    mapper.enable(SerializationFeature.INDENT_OUTPUT);

	    return new ResponseEntityDecoder(new JacksonDecoder(mapper));
	}
	
	
	@Bean
	public Encoder feignEncoder() {
		
		ObjectMapper objectMapper = new ObjectMapper()
				.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, true);
		
		return new JacksonEncoder(objectMapper);
	}
	
	
	
	
}
