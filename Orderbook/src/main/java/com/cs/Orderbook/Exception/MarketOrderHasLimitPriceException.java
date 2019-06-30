package com.cs.Orderbook.Exception;

public class MarketOrderHasLimitPriceException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MarketOrderHasLimitPriceException(String exception) {
		super(exception);
	}

}
