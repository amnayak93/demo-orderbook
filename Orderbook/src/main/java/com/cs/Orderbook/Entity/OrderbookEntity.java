package com.cs.Orderbook.Entity;

import java.math.BigInteger;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Data;;

@Data
@Entity
public class OrderbookEntity {

	@Id
	private String financialId;
	private Status status;
	@OneToMany(mappedBy = "orderBook", cascade = CascadeType.ALL)
	private List<OrderEntity> orders;
	@OneToMany(mappedBy = "orderBook", cascade = CascadeType.ALL)
	private List<ExecutionEntity> executions;
	private BigInteger validOrders;
	private BigInteger totExecutionOrders;
	private boolean firstExecutionFlag = true;	
	
	
	public OrderbookEntity() {
		super();
	}

	public OrderbookEntity(String financialId, Status status, List<OrderEntity> orders, BigInteger validOrders,
			BigInteger totExecutionOrders, boolean firstExecutionFlag, List<ExecutionEntity> executions) {
		this.financialId = financialId;
		this.status = status;
		this.orders = orders;
		this.validOrders = validOrders;
		this.totExecutionOrders = totExecutionOrders;
		this.firstExecutionFlag = firstExecutionFlag;
		this.executions = executions;
	}

	public String getFinancialId() {
		return financialId;
	}

	public void setFinancialId(String financialId) {
		this.financialId = financialId;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
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
