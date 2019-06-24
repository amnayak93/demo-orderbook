package com.cs.Orderbook.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import com.cs.Orderbook.Entity.OrderbookEntity;
import com.cs.Orderbook.model.Orderbook;

@Component
public class ConversionUtils {

	public Orderbook convertToModelForOrderbook(OrderbookEntity orderbookEntity) {
		Orderbook orderbook = new Orderbook();
		BeanUtils.copyProperties(orderbookEntity, orderbook);
		return orderbook;
	}

	public OrderbookEntity convertToEntityForOrderbook(Orderbook book) {
		OrderbookEntity orderbookEntity = new OrderbookEntity();
		BeanUtils.copyProperties(book, orderbookEntity);
		return orderbookEntity;
	}
	
	

}
