package com.cs.Orderbook.Exception;

public class OrderbookFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OrderbookFoundException(String exception) {
		super(exception);
	}
}
