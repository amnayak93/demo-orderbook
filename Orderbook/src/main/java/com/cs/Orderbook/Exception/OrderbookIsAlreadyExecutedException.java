package com.cs.Orderbook.Exception;

public class OrderbookIsAlreadyExecutedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OrderbookIsAlreadyExecutedException(String exception) {
		super(exception);
	}
}
