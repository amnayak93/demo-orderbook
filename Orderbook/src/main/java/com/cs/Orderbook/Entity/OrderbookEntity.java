package com.cs.Orderbook.Entity;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.cs.Orderbook.Entity.OrderEntity;;

@Entity
public class OrderbookEntity {

	@Id
	private String financialId;
	private String status;
	@OneToMany(mappedBy = "financialId", cascade = CascadeType.ALL)
	private List<OrderEntity> orders;
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

	public List<OrderEntity> getOrders() {
		return orders;
	}

	public void setOrders(List<OrderEntity> orders) {
		this.orders = orders;
	}

	public boolean isFirstExecutionFlag() {
		return firstExecutionFlag;
	}

	public void setFirstExecutionFlag(boolean firstExecutionFlag) {
		this.firstExecutionFlag = firstExecutionFlag;
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

}
