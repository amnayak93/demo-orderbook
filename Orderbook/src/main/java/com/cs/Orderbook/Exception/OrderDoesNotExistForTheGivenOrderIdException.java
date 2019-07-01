package com.cs.Orderbook.Exception;

public class OrderDoesNotExistForTheGivenOrderIdException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public OrderDoesNotExistForTheGivenOrderIdException(String exception) {
		super(exception);
	}

}
