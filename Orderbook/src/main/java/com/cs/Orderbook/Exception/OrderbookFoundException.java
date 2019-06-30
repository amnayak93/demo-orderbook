package com.cs.Orderbook.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderbookFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public OrderbookFoundException(String exception){
		super(exception);
	}
}
