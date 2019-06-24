package com.cs.Orderbook.model;

import java.math.BigInteger;
import java.util.List;

import lombok.Data;

@Data
public class Orderbook {

	private String financialId;
	private String status;
	private List<Order> orders;
	private BigInteger validOrders;
	private BigInteger totExecutionOrders;
	private boolean firstExecutionFlag = true;

	public String getFinancialId() {
		return financialId;
	}

	public void setFinancialId(String financialId) {
		this.financialId = financialId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}

	public BigInteger getValidOrders() {
		return validOrders;
	}

	public void setValidOrders(BigInteger validOrders) {
		this.validOrders = validOrders;
	}

	public BigInteger getTotExecutionOrders() {
		return totExecutionOrders;
	}

	public void setTotExecutionOrders(BigInteger totExecutionOrders) {
		this.totExecutionOrders = totExecutionOrders;
	}

	public boolean isFirstExecutionFlag() {
		return firstExecutionFlag;
	}

	public void setFirstExecutionFlag(boolean firstExecutionFlag) {
		this.firstExecutionFlag = firstExecutionFlag;
	}

}
