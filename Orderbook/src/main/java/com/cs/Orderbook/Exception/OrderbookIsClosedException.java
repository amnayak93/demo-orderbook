package com.cs.Orderbook.Exception;

public class OrderbookIsClosedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OrderbookIsClosedException(String exception) {
		super(exception);
	}

}
