package com.cs.Orderbook.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OrderbookNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public OrderbookNotFoundException(String exception){
		super(exception);
	}
}
