package ru.clevertec.ecl.knyazev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import feign.FeignException;
import ru.clevertec.ecl.knyazev.client.UserClient;
import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;
import ru.clevertec.ecl.knyazev.token.JwtUtil;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {
	
	private static final String SECRET_KEY = "TEST_SECRET";
	
	private static final Long TOKEN_LIFE_TIME = 300L;
	
	private MockedStatic<SecurityContextHolder> securityContextHolderMock;
	
	@Mock
	private UserClient userClientMock;
	
	@Mock
	private AuthenticationManager providerManagerMock;
	
	@Mock
	private SecurityContext securityContextMock = Mockito.mock(SecurityContext.class);
	
	@Spy
	private JwtUtil jwtUtil = new JwtUtil(SECRET_KEY, TOKEN_LIFE_TIME);
	
	@InjectMocks
	private UserServiceImpl userServiceImpl;
	
	@BeforeEach
	public void setUp() {
		securityContextHolderMock = Mockito.mockStatic(SecurityContextHolder.class);
	}
	
	@AfterEach
	public void postExecute() {
		
		if (!securityContextHolderMock.isClosed()) {
			securityContextHolderMock.close();
		}
		
	}	
	
	@Test
	public void checkAuthentificateUserShouldReturnToken() throws ServiceException {
		
		UserDetails expectedUser = User.builder()
	               .username("Misha")
	               .password("123456")
	               .authorities(new ArrayList<>() {
	            	   
					private static final long serialVersionUID = 8273722092805849311L;

			       {
	            	  add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	               }})
	               .build();	
		Authentication expectedAuthentication = new UsernamePasswordAuthenticationToken(expectedUser, 
				                                                                        expectedUser.getPassword(),
				                                                                        expectedUser.getAuthorities());
		
		Mockito.when(providerManagerMock.authenticate(Mockito.any(Authentication.class)))
		       .thenReturn(expectedAuthentication);
		
		securityContextHolderMock.when(SecurityContextHolder::getContext)
		                         .thenReturn(securityContextMock);
		
		Mockito.doNothing().when(securityContextMock).setAuthentication(Mockito.any(Authentication.class));
		
		UserDTO inputUserDTO = UserDTO.builder()
									  .name("Misha")
									  .password("123456")
									  .build();
		
		String actualToken = userServiceImpl.authenticateUser(inputUserDTO);
		
		assertAll(
				() -> assertThat(actualToken).isNotNull(),
				() -> assertThat(actualToken).hasSize(168)
		);
		
	}
	
	@Test
	public void checkAuthentificateUserShouldThrowServiceException() {
		
		Mockito.when(providerManagerMock.authenticate(Mockito.any(Authentication.class)))
	       .thenThrow(new AuthenticationException("Authentication error") {
	    	   
			private static final long serialVersionUID = -1752405471455408289L;
	    	   
	       });
		
		UserDTO inputUserDTO = UserDTO.builder()
				  .name("Misha")
				  .password("123456")
				  .build();
		
		assertThatExceptionOfType(ServiceException.class)
		                     .isThrownBy(() -> userServiceImpl.authenticateUser(inputUserDTO));
	}
	
	@Test
	public void checkRegisterUserShouldReturnUserDTO() throws ServiceException {
		
		UserDTO expectedUserDTO = UserDTO.builder()
										 .name("Vanya")
										 .password("{BCRYPT}hash")
										 .email("vanya@mail.ru")
										 .enabled(true)
										 .build();
		
		Mockito.when(userClientMock.addUser(Mockito.any(UserDTO.class)))
			   .thenReturn(expectedUserDTO);
		
		UserDTO inputUserDTO = UserDTO.builder()
									  .name("Vanya")
									  .password("123456")
									  .email("vanya@mail.ru")
									  .build();
		
		UserDTO actualUserDTO = userServiceImpl.registerUser(inputUserDTO);
		
		assertAll(
				() -> assertThat(actualUserDTO).isNotNull(),
				() -> assertThat(actualUserDTO).isEqualTo(expectedUserDTO)
		);
		
	}
	
	@Test
	public void checkRegisterUserShouldThrowServiceException() {
		
		Mockito.when(userClientMock.addUser(Mockito.any(UserDTO.class)))
			   .thenThrow(FeignException.class);
		
		UserDTO inputUserDTO = UserDTO.builder()
									  .name("Vova")
									  .password("123456")
									  .email("vova@mail.ru")
									  .build();
		
		assertThatExceptionOfType(ServiceException.class)
		                    .isThrownBy(() -> userServiceImpl.registerUser(inputUserDTO));
		
	}
	

}
