package com.cs.Orderbook.Exception;

public class OrderbookIsNotClosedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OrderbookIsNotClosedException(String exception) {
		super(exception);
	}

}
