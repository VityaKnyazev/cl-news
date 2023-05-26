package ru.clevertec.ecl.knyazev.service.exception;

public class ServiceException extends Exception {

	private static final long serialVersionUID = -358812184919688719L;

	public ServiceException() {
		super();
	}

	public ServiceException(String message, Throwable cause) {
		super(message, cause);
	}

	public ServiceException(String message) {
		super(message);
	}
	
}
