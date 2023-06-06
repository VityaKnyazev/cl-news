package ru.clevertec.ecl.knyazev.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface Service<T> {
	
	/**
	 * 
	 * Show T by it's id
	 * 
	 * @param id T object id
	 * @return T object
	 * @throws ServiceException when T not found or invalid id
	 * 
	 */
	T showById(Long id) throws ServiceException;
	
	/**
	 * 
	 * Show all T objects with pageable (page, size, sort)
	 *  
	 * @param pageable object
	 * @return all T objects depends on pageable
	 * @throws ServiceException when nothing found
	 * 
	 */
	List<T> showAll(Pageable pageable) throws ServiceException;
	
	/**
	 * 
	 * Show all T objects on String textPart with pageable (page, size, sort).
	 * If String textPart is null than show all T objects with pageable
	 * 
	 * @param textPart for searching in text field of object
	 * @param pageable object
	 * @return all T objects on String textPart or all T objects
	 *         using pageable
	 * @throws ServiceException when nothing found
	 * 
	 */
	List<T> showAllOrByTextPart(String textPart, Pageable pageable) throws ServiceException;

	/**
	 * 
	 * Add T t object
	 * 
	 * @param t object for adding
	 * @return T t added object
	 * @throws ServiceException when invalid T t object was given or was failed on saving
	 * 
	 */
	T add(T t) throws ServiceException;

	/**
	 * 
	 * Change T t object
	 * 
	 * @param t object for changing
	 * @return T t changed object
	 * @throws ServiceException when invalid T t object was given or was failed on changing
	 * 
	 */
	T change(T t) throws ServiceException;

	/**
	 * 
	 * Remove T t object
	 * 
	 * @param t object for removing
	 * @throws ServiceException when invalid T t object was given or was failed on removing
	 * 
	 */
	void remove(T t) throws ServiceException;
	
}
