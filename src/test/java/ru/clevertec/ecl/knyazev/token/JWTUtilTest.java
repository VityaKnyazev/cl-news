package ru.clevertec.ecl.knyazev.token;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class JWTUtilTest {
	
	private static final String SECRET_KEY = "TEST_SECRET";
	
	private static final Long TOKEN_LIFE_TIME = 300L;
	
	private JwtUtil jwtUtil;
	
	@BeforeEach
	public void setUp() {
		jwtUtil = new JwtUtil(SECRET_KEY, TOKEN_LIFE_TIME);
	}
	
	@Test
	public void checkGenerateJWTShouldReturnToken() {
		
		UserDetails requestUser = User.builder()
				               .username("Misha")
				               .password("123456")
				               .authorities(new ArrayList<>() {
				            	   
								private static final long serialVersionUID = 8273722092805849311L;

						       {
				            	  add(new SimpleGrantedAuthority("ROLE_ADMIN"));
				               }})
				               .build();		
		Authentication authentication = new UsernamePasswordAuthenticationToken(requestUser, null);
		
		String actualToken = jwtUtil.generateJWT(authentication);
		
		assertAll(
			() -> assertThat(actualToken).isNotNull(),
			() -> assertThat(actualToken).hasSize(168)
		);
	}
	
	@Test
	public void checkGetUserNameFromJWTShouldReturnUserName() {
		
		String expectedUserName = "Misha";
		
		UserDetails requestUser = User.builder()
	               .username("Misha")
	               .password("123456")
	               .authorities(new ArrayList<>() {
	            	   
					private static final long serialVersionUID = 8273722092805849311L;

			       {
	            	  add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	               }})
	               .build();		
		Authentication authentication = new UsernamePasswordAuthenticationToken(requestUser, null);

		String actualToken = jwtUtil.generateJWT(authentication);
		
		String actualUserName = jwtUtil.getUserNameFromJWT(actualToken);
		
		assertAll(
				() -> assertThat(actualUserName).isNotNull(),
				() -> assertThat(actualUserName).isEqualTo(expectedUserName)
			);
	}
	
	@Test
	public void checkGetUserNameFromJWTShouldReturnNullOnInvalidToken() {
		
		String inputInvalidToken = """
				eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.
				eyJuYW1lIjoiSXZhbiIsImV4cCI6MTY4NzI0NDg1MH0
				.AeVqV9DVrWr-nuXIVwpB2RfQGzY8zJ6WLOe0TiaxNGTlBGa7zz-
				UZBNgElxV3302fv_wuE0TJG39ObKEwWgNzw
				""";
		
		String actualUserName = jwtUtil.getUserNameFromJWT(inputInvalidToken);
		
		assertThat(actualUserName).isNull();		
	}

}
