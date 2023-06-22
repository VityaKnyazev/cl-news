package ru.clevertec.ecl.knyazev.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.clevertec.ecl.knyazev.client.UserClient;
import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.mapper.SecurityUserMapper;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;


@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SecurityUserService implements UserDetailsService {
	
	private static final String NOT_AUTHETICATED = "Not authenticated";
	private static final String MICROSERVICE_ERROR = "Error in User Microservice";
	private static final String USER_NOT_FOUND = "User not found";
	
	private UserClient userClient;
	
	private SecurityUserMapper securityUserMapperImpl;
	
	@Override
	@Transactional(readOnly = true)
	public User loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User securityUser = null;
		
		try {
			UserDTO userDTO = userClient.getSecurityUser(username);
			
			securityUser = securityUserMapperImpl.toSecurityUser(userDTO);	
					                  
		} catch (FeignException e) {
			log.error(MICROSERVICE_ERROR + ": {}", e.getMessage(), e);
		}

		if (securityUser == null) {
			throw new UsernameNotFoundException(USER_NOT_FOUND);
		}
		
		return securityUser;
	}
	
	private static final User getSecurityUserFromSecurityContext() throws ServiceException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			log.error(NOT_AUTHETICATED);
			throw new ServiceException(NOT_AUTHETICATED);
		}
		
		return (User) authentication.getPrincipal();
	}
	
	public static final List<String> getSecurityUserRoles() throws ServiceException {
		return  getSecurityUserFromSecurityContext().getAuthorities()
                .stream()
                .map(grantedAuth -> grantedAuth.getAuthority())
                .toList();
	}
	
	public static final String getSecurityUserName() throws ServiceException {
		return getSecurityUserFromSecurityContext().getUsername();
	}

}
