package com.cs.Orderbook.Entity;

import java.math.BigDecimal;
import java.math.BigInteger;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
public class ExecutionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long executionId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private OrderbookEntity orderBook;
	private BigInteger executionQuantity;
	private BigDecimal executionPrice;

	public ExecutionEntity() {
		super();
	}

	public ExecutionEntity(BigInteger executionQuantity, BigDecimal executionPrice) {
		this.executionQuantity = executionQuantity;
		this.executionPrice = executionPrice;
	}

	public OrderbookEntity getOrderBook() {
		return orderBook;
	}

	public void setOrderBook(OrderbookEntity orderBook) {
		this.orderBook = orderBook;
	}

	public BigInteger getExecutionQuantity() {
		return executionQuantity;
	}

	public void setExecutionQuantity(BigInteger executionQuantity) {
		this.executionQuantity = executionQuantity;
	}

	public BigDecimal getExecutionPrice() {
		return executionPrice;
	}

	public void setExecutionPrice(BigDecimal executionPrice) {
		this.executionPrice = executionPrice;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executionPrice == null) ? 0 : executionPrice.hashCode());
		result = prime * result + ((executionQuantity == null) ? 0 : executionQuantity.hashCode());
		result = prime * result + ((orderBook == null) ? 0 : orderBook.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecutionEntity other = (ExecutionEntity) obj;
		if (executionPrice == null) {
			if (other.executionPrice != null)
				return false;
		} else if (!executionPrice.equals(other.executionPrice))
			return false;
		if (executionQuantity == null) {
			if (other.executionQuantity != null)
				return false;
		} else if (!executionQuantity.equals(other.executionQuantity))
			return false;
		if (orderBook == null) {
			if (other.orderBook != null)
				return false;
		} else if (!orderBook.equals(other.orderBook))
			return false;
		return true;
	}

}
