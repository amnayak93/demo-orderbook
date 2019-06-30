package com.cs.Orderbook.Exception;

public class ExecutionQuantityIsMoreThanTheValidDemandException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ExecutionQuantityIsMoreThanTheValidDemandException(String exception) {
		super(exception);
	}

}
