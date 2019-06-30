package com.cs.Orderbook.Exception;

public class OrderbookIsExecutedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public OrderbookIsExecutedException(String exception) {
		super(exception);
	}

}
