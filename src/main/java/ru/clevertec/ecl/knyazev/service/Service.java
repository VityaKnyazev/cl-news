package ru.clevertec.ecl.knyazev.service;

import java.awt.print.Pageable;
import java.util.List;

import ru.clevertec.ecl.knyazev.service.exception.ServiceException;

public interface Service<T> {
	
	T show(Long id) throws ServiceException;
	
	List<T> showAll(Pageable pageable) throws ServiceException;

	T add(T t) throws ServiceException;

	T change(T t) throws ServiceException;

	void remove(T t) throws ServiceException;
	
}
