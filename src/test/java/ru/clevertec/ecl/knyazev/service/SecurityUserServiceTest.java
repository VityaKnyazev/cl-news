package ru.clevertec.ecl.knyazev.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import feign.FeignException;
import ru.clevertec.ecl.knyazev.client.UserClient;
import ru.clevertec.ecl.knyazev.dto.RoleDTO;
import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.mapper.SecurityUserMapper;
import ru.clevertec.ecl.knyazev.mapper.SecurityUserMapperImpl;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

@ExtendWith(MockitoExtension.class)
public class SecurityUserServiceTest {
	
	private MockedStatic<SecurityContextHolder> securityContextHolderMock;
	
	@Mock
	private UserClient userClientMock;
	
	@Mock
	private SecurityContext securityContextMock;
	
	@Spy
	private SecurityUserMapper securityUserMapperImpl = new SecurityUserMapperImpl();
	
	@InjectMocks
	private SecurityUserService securityUserService;
	
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
	public void checkLoadUserByUsernameShouldReturnUser() {
		
		UserDTO expectedUserDTO = UserDTO.builder()
				 						 .name("Misha")
				 						 .password("{bcrypt}hash")
				 						 .email("misha@mail.ru")
				 						 .enabled(true)
				 						 .rolesDTO(new ArrayList<>() {
				 							 
											private static final long serialVersionUID = -6332293037689765330L;

										{
				 							add(RoleDTO.builder()
				 									   .name("ROLE_ADMIN")
				 									   .build()); 
				 						 }})
				 						 .build();
		
		Mockito.when(userClientMock.getSecurityUser(Mockito.anyString()))
		       .thenReturn(expectedUserDTO);
		
		String inputUserName = "Misha";
		
		User actualUser = securityUserService.loadUserByUsername(inputUserName);
		
		assertAll( 
				() -> assertThat(actualUser).isNotNull(),
				() -> assertThat(actualUser.getUsername()).isEqualTo("Misha"),
				() -> assertThat(actualUser.getAuthorities()).anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))
		);
	}
	
	@Test
	public void checkLoadUserByUsernameShouldThrowUserNotFoundException() {
		
		Mockito.when(userClientMock.getSecurityUser(Mockito.anyString()))
	       .thenThrow(FeignException.class);
		
		String inputUserName = "Morkovka";
		
		assertThatExceptionOfType(UsernameNotFoundException.class)
							 .isThrownBy(() -> securityUserService.loadUserByUsername(inputUserName));
	}
	
	@Test
	public void checkGetSecurityUserRolesShouldReturnUserRoles() throws ServiceException {
		
		UserDetails expectedUser = User.builder()
	               .username("Misha")
	               .password("{bcrypt}hash")
	               .authorities(new ArrayList<>() {
	            	   
					private static final long serialVersionUID = 8273722092805849311L;

			       {
	            	  add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	               }})
	               .build();	
		Authentication expectedAuthentication = new UsernamePasswordAuthenticationToken(expectedUser, 
				                                                                        expectedUser.getPassword(),
				                                                                        expectedUser.getAuthorities());
		
		securityContextHolderMock.when(SecurityContextHolder::getContext)
        	.thenReturn(securityContextMock);

		Mockito.when(securityContextMock.getAuthentication())
		       .thenReturn(expectedAuthentication);
		
		List<String> actualUserRoles = SecurityUserService.getSecurityUserRoles();
		
		assertAll(
				() -> assertThat(actualUserRoles).isNotNull(),
				() -> assertThat(actualUserRoles).isNotEmpty(),
				() -> assertThat(actualUserRoles).anyMatch(role -> role.equals("ROLE_ADMIN"))
		);		
		
	}
	
	@Test
	public void checkGetSecurityUserNameShouldReturnUserName() throws ServiceException {
		
		UserDetails expectedUser = User.builder()
	               .username("Misha")
	               .password("{bcrypt}hash")
	               .authorities(new ArrayList<>() {
	            	   
					private static final long serialVersionUID = 8273722092805849311L;

			       {
	            	  add(new SimpleGrantedAuthority("ROLE_ADMIN"));
	               }})
	               .build();	
		Authentication expectedAuthentication = new UsernamePasswordAuthenticationToken(expectedUser, 
				                                                                        expectedUser.getPassword(),
				                                                                        expectedUser.getAuthorities());
		
		securityContextHolderMock.when(SecurityContextHolder::getContext)
        	.thenReturn(securityContextMock);

		Mockito.when(securityContextMock.getAuthentication())
		       .thenReturn(expectedAuthentication);
		
		String actualUserName = SecurityUserService.getSecurityUserName();
		
		assertAll(
				() -> assertThat(actualUserName).isNotNull(),
				() -> assertThat(actualUserName).isEqualTo("Misha")
		);		
		
	}
	
	@ParameterizedTest
	@MethodSource("getInvalidAuthentication")
	public void checkGetSecurityUserFromSecurityContextShouldThrowServiceException(Authentication invalidAuthentication) {
		
		securityContextHolderMock.when(SecurityContextHolder::getContext)
    							 .thenReturn(securityContextMock);

		Mockito.when(securityContextMock.getAuthentication())
		       .thenReturn(invalidAuthentication);
		
		assertThatExceptionOfType(ServiceException.class)
							.isThrownBy(() -> SecurityUserService.getSecurityUserName());
	}
	
	private static Stream<Authentication> getInvalidAuthentication() {
		return Stream.of(
					null,
					new UsernamePasswordAuthenticationToken("Manya", "2569")
				);
	}
	
}
