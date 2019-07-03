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

}
