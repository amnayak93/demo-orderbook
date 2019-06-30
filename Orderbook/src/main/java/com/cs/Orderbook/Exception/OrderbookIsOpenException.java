package com.cs.Orderbook.Exception;

public class OrderbookIsOpenException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OrderbookIsOpenException(String exception) {
		super(exception);
	}
}
