package com.cs.Orderbook.Entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class OrderEntity{
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private String financialId;
	private BigDecimal quantiy;
	private LocalDateTime entryDate;
	private BigDecimal price;
	private String orderType;

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

	public String getFid() {
		return financialId;
	}

	public void setFid(String fid) {
		this.financialId = fid;
	}

}
