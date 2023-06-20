package ru.clevertec.ecl.knyazev.service;

import ru.clevertec.ecl.knyazev.dto.UserDTO;
import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface UserService {
	
	/**
	 * 
	 * Authenticate user in application and get JWT token
	 * 
	 * @param userDTO user DTO with user name and password for authentication 
	 *        in application
	 * @return JWT token that should be used for showing endpoints
	 * @throws ServiceException when authentication failed
	 */
	public String authenticateUser(UserDTO userDTO) throws ServiceException;
	
	/**
	 * 
	 * Register user information in application
	 * 
	 * @param userDTO user registration DTO information
	 * @return registered user DTO 
	 * @throws ServiceException when user registration failed
	 */
	public UserDTO registerUser(UserDTO userDTO) throws ServiceException;
	
}
