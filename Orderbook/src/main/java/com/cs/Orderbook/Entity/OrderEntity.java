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
public class OrderEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long orderId;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private OrderbookEntity orderBook;
	private Long quantity;
	private LocalDateTime entryDate;
	private BigDecimal price;
	private OrderType orderType;
	private OrderStatus status;
	private Long executionQuantity;
	private BigDecimal executionPrice;

	public OrderEntity() {
		super();
	}

	public OrderEntity(Long quantity, LocalDateTime entryDate, OrderType orderType, BigDecimal price) {
		super();
		this.quantity = quantity;
		this.entryDate = entryDate;
		this.orderType = orderType;
		this.price = price;
	}

	public OrderEntity(Long quantity, LocalDateTime entryDate, OrderType orderType) {
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

	public Long getQuantiy() {
		return quantity;
	}

	public void setQuantiy(Long quantiy) {
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
