package com.cs.Orderbook.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.cs.Orderbook.Entity.ExecutionEntity;
import com.cs.Orderbook.Entity.OrderEntity;
import com.cs.Orderbook.Entity.OrderbookEntity;

@Service
public interface OrderbookService {

	OrderbookEntity openOrderbook(String id);

	OrderbookEntity closeOrderbook(String id);

	OrderbookEntity addOrders(List<OrderEntity> orders, String fid);

	void executeOrders(ExecutionEntity execution, BigDecimal executionPrice);

	void openOrCloseBook(OrderbookEntity book);

}
