package com.cs.Orderbook.Exception;

public class LimitOrderDoesNotHaveLimitPriceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public LimitOrderDoesNotHaveLimitPriceException(String exception) {
		super(exception);
	}
}
