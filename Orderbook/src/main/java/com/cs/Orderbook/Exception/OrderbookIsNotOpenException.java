package com.cs.Orderbook.Exception;

public class OrderbookIsNotOpenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OrderbookIsNotOpenException(String exception) {
		super(exception);
	}

}
