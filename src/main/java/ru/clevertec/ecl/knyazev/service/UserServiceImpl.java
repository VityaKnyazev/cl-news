package ru.clevertec.ecl.knyazev.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import feign.FeignException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.clevertec.ecl.knyazev.client.UserClient;
import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;
import ru.clevertec.ecl.knyazev.token.JwtUtil;

@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = { @Autowired })
public class UserServiceImpl implements UserService {
	
	private static final String AUTHENTIFICATION_FAILED = "User authentification failed";
	private static final String REGISTRATION_ERROR = "User registration failed";
	
	private UserClient userClient;

	private JwtUtil jwtUtil;

	private AuthenticationManager providerManager;

	@Override
	@Transactional(readOnly = true)
	public String authenticateUser(UserDTO userDTO) throws ServiceException {

		try {
			Authentication authentication = providerManager
					.authenticate(new UsernamePasswordAuthenticationToken(userDTO.getName(), userDTO.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);
			
			String token = jwtUtil.generateJWT(authentication);
			
			return token;
			
		} catch (AuthenticationException e) {
			log.error(AUTHENTIFICATION_FAILED + ": {}", e.getMessage(), e);
			throw new ServiceException(AUTHENTIFICATION_FAILED);
		}

	}

	@Override
	@Transactional(rollbackFor = ServiceException.class)
	public UserDTO registerUser(UserDTO userDTO) throws ServiceException {
		
		try {
			UserDTO addedUserDTO = userClient.addUser(userDTO);
			
			return addedUserDTO;
		} catch (FeignException e) {
			log.error(REGISTRATION_ERROR + ": {}", e.getMessage(), e);
			throw new ServiceException(REGISTRATION_ERROR);
		}
		
	}

}
