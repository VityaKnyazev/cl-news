package ru.clevertec.ecl.knyazev.controller;

import java.util.Date;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import ru.clevertec.ecl.knyazev.controller.ExceptionController.ErrorMessage;

@RestController
public class NewsErrorController implements ErrorController  {
	
	@GetMapping("/error")
	public ResponseEntity<ErrorMessage> handleError(HttpServletRequest request) throws Exception {
		Integer status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
		
		if (status == 404) {
			ErrorMessage message = new ExceptionController.ErrorMessage(status, new Date(),"Not Found");
			return new ResponseEntity<ErrorMessage>(message, HttpStatus.NOT_FOUND);
		}
		
		throw new Exception("UNKNOWN_ERROR");
	}
	
}
