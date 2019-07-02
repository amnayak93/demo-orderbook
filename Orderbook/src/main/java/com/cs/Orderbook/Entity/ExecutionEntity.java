package com.cs.Orderbook.Entity;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.Data;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class ExecutionEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long executionId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private OrderbookEntity orderBook;
	private Long executionQuantity;
	private BigDecimal executionPrice;

	public ExecutionEntity() {
		super();
	}

	public ExecutionEntity(Long executionQuantity, BigDecimal executionPrice) {
		this.executionQuantity = executionQuantity;
		this.executionPrice = executionPrice;
	}

	public OrderbookEntity getOrderBook() {
		return orderBook;
	}

	public void setOrderBook(OrderbookEntity orderBook) {
		this.orderBook = orderBook;
	}

	public Long getExecutionQuantity() {
		return executionQuantity;
	}

	public void setExecutionQuantity(Long executionQuantity) {
		this.executionQuantity = executionQuantity;
	}

	public BigDecimal getExecutionPrice() {
		return executionPrice;
	}

	public void setExecutionPrice(BigDecimal executionPrice) {
		this.executionPrice = executionPrice;
	}

	/*@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (executionId ^ (executionId >>> 32));
		result = prime * result + ((executionPrice == null) ? 0 : executionPrice.hashCode());
		result = prime * result + (int) (executionQuantity ^ (executionQuantity >>> 32));
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
		if (executionId != other.executionId)
			return false;
		if (executionPrice == null) {
			if (other.executionPrice != null)
				return false;
		} else if (!executionPrice.equals(other.executionPrice))
			return false;
		if (executionQuantity != other.executionQuantity)
			return false;
		if (orderBook == null) {
			if (other.orderBook != null)
				return false;
		} else if (!orderBook.equals(other.orderBook))
			return false;
		return true;
	}*/

	

}
