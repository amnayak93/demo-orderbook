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

	OrderbookEntity executeOrders(ExecutionEntity execution, String fid);

	void openOrCloseBook(OrderbookEntity book);

}
