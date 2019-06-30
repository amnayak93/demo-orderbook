package com.cs.Orderbook.Exception;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(OrderbookFoundException.class)
	public final ResponseEntity<Object> handleOrderbookNotFoundException(OrderbookFoundException ex,
			WebRequest request, HttpServletResponse response) {
		List<String> details = new ArrayList<>();
		details.add(ex.getLocalizedMessage());
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		ErrorResponse error = new ErrorResponse(response.getStatus(), "Orderbook found", details);
		return new ResponseEntity(error, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(OrderbookIsNotClosedException.class)
	public final ResponseEntity<Object> handleOrderbookNotClosedException(OrderbookIsNotClosedException ex,
			WebRequest request, HttpServletResponse response) {
		List<String> details = new ArrayList<>();
		details.add(ex.getLocalizedMessage());
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		ErrorResponse error = new ErrorResponse(response.getStatus(), "Orderbook is alreay in open or executed, cannot open it", details);
		return new ResponseEntity(error, HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(OrderbookIsNotOpenException.class)
	public final ResponseEntity<Object> handleOrderbookNotOpenException(OrderbookIsNotOpenException ex,
			WebRequest request, HttpServletResponse response) {
		List<String> details = new ArrayList<>();
		details.add(ex.getLocalizedMessage());
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		ErrorResponse error = new ErrorResponse(response.getStatus(), "Orderbook is not open, so cannot close it", details);
		return new ResponseEntity(error, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(OrderbookIsClosedException.class)
	public final ResponseEntity<Object> handleOrderbookClosedException(OrderbookIsClosedException ex,
			WebRequest request, HttpServletResponse response) {
		List<String> details = new ArrayList<>();
		details.add(ex.getLocalizedMessage());
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		ErrorResponse error = new ErrorResponse(response.getStatus(), "Orderbook is closed, so cannot add new orders", details);
		return new ResponseEntity(error, HttpStatus.NOT_FOUND);
	}
}
