package com.cs.Orderbook.Entity;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.cs.Orderbook.utils.OrderStatus;
import com.cs.Orderbook.utils.OrderType;

@Entity
public class OrderEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long orderId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private OrderbookEntity orderBook;
	private BigDecimal quantity;
	private LocalDateTime entryDate;
	private BigDecimal price;
	private OrderType orderType;
	private OrderStatus status;
	private BigDecimal executionQuantity;

	public OrderEntity() {
		super();
	}

	public OrderEntity(BigDecimal quantity, LocalDateTime entryDate, OrderType orderType, BigDecimal price) {
		super();
		this.quantity = quantity;
		this.entryDate = entryDate;
		this.orderType = orderType;
		this.price = price;
	}

	public OrderEntity(BigDecimal quantity, LocalDateTime entryDate, OrderType orderType) {
		super();
		this.quantity = quantity;
		this.entryDate = entryDate;
		this.orderType = orderType;
	}

	public long getOrderId() {
		return orderId;
	}

	public void setOrderId(long orderId) {
		this.orderId = orderId;
	}

	public BigDecimal getQuantiy() {
		return quantity;
	}

	public void setQuantiy(BigDecimal quantiy) {
		this.quantity = quantiy;
	}

	public LocalDateTime getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(LocalDateTime entryDate) {
		this.entryDate = entryDate;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public OrderbookEntity getOrderBook() {
		return orderBook;
	}

	public void setOrderBook(OrderbookEntity orderBook) {
		this.orderBook = orderBook;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public BigDecimal getExecutionQuantity() {
		return executionQuantity;
	}

	public void setExecutionQuantity(BigDecimal executionQuantity) {
		this.executionQuantity = executionQuantity;
	}

}
