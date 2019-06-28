package com.cs.Orderbook.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class OrderEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn
	private OrderbookEntity orderBook;
	private BigDecimal quantiy;
	private LocalDateTime entryDate;
	private BigDecimal price;
	private String orderType;

	public OrderEntity() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OrderEntity(long id, OrderbookEntity orderBook, BigDecimal quantiy, LocalDateTime entryDate,
			BigDecimal price, String orderType) {
		super();
		this.id = id;
		this.orderBook = orderBook;
		this.quantiy = quantiy;
		this.entryDate = entryDate;
		this.price = price;
		this.orderType = orderType;
	}

	public BigDecimal getQuantiy() {
		return quantiy;
	}

	public void setQuantiy(BigDecimal quantiy) {
		this.quantiy = quantiy;
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

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public OrderbookEntity getOrderBook() {
		return orderBook;
	}

	public void setOrderBook(OrderbookEntity orderBook) {
		this.orderBook = orderBook;
	}

}
