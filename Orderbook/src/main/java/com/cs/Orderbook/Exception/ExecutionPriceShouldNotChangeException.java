package com.cs.Orderbook.Exception;

public class ExecutionPriceShouldNotChangeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExecutionPriceShouldNotChangeException(String exception) {
		super(exception);
	}

}
