package com.cs.Orderbook.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Currency;

public class Execution {

	private BigDecimal quantity;
	private BigDecimal price;

	public BigDecimal getQuantity() {
		return quantity;
	}

	public void setQuantity(BigDecimal quantity) {
		this.quantity = quantity;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

}
