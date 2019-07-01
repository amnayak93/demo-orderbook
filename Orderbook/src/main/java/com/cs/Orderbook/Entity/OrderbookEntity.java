package com.cs.Orderbook.Entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.cs.Orderbook.utils.Status;

import lombok.Data;;

@Data
@Entity
@Table(name = "OrderbookEntity")
public class OrderbookEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long orderbookId;
	@Column(unique = true)
	private String instrument;
	private Status status;
	@OneToMany(mappedBy = "orderBook", cascade = CascadeType.ALL)
	private List<OrderEntity> orders;
	@OneToMany(mappedBy = "orderBook", cascade = CascadeType.ALL)
	private List<ExecutionEntity> executions;

	public OrderbookEntity() {
		super();
	}

	public OrderbookEntity(String instrument, Status status) {
		super();
		this.instrument = instrument;
		this.status = status;
	}

	public String getInstrument() {
		return instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
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

	public List<ExecutionEntity> getExecutions() {
		return executions;
	}

	public void setExecutions(List<ExecutionEntity> executions) {
		this.executions = executions;
	}
}
