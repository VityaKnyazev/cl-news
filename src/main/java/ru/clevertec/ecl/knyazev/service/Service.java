package ru.clevertec.ecl.knyazev.service;

import java.util.List;

import org.springframework.data.domain.Pageable;

import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface Service<T> {
	
	T show(Long id) throws ServiceException;
	
	List<T> showAll(Pageable pageable) throws ServiceException;
	
	List<T> showAllOrByTextPart(String textPart, Pageable pageable) throws ServiceException;

	T add(T t) throws ServiceException;

	T change(T t) throws ServiceException;

	void remove(T t) throws ServiceException;
	
}
