package com.cs.Orderbook.Exception;

public class OrderbookNotFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OrderbookNotFoundException(String exception) {
		super(exception);
	}

}
