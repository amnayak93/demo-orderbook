package com.cs.Orderbook.controller;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cs.Orderbook.model.Execution;
import com.cs.Orderbook.Entity.OrderEntity;
import com.cs.Orderbook.model.Orderbook;
import com.cs.Orderbook.service.OrderbookService;
import com.cs.Orderbook.utils.StaticUtils;

@RestController
public class OrderController {
	
	@Autowired
	OrderbookService orderbookService;

	@PutMapping(value = "/open/{fid}")
	private ResponseEntity<Object> openOrderbook(@PathVariable String fid){
		Orderbook book = orderbookService.openOrderbook(fid);
		book.setStatus(StaticUtils.OPEN);
		return orderbookService.openOrCloseBook(book);
	}
	
	@PutMapping(value = "/close/{fid}")
	private ResponseEntity<Object> closeOrderbook(@PathVariable String fid){
		Orderbook book = orderbookService.closeOrderbook(fid);
		book.setStatus(StaticUtils.CLOSE);
		return orderbookService.openOrCloseBook(book);
	}
	
	@PutMapping(value = "/add/{fid}")
	private ResponseEntity<Object> addOrders(@RequestBody List<OrderEntity> orders, @PathVariable String fid){
		return orderbookService.addOrders(orders, fid);
	}
	
	@PutMapping(value = "/execute/{fid}")
	private ResponseEntity<Object> executeOrders(@RequestBody Execution execution, @PathVariable String fid, @RequestParam BigDecimal executionPrice){
		return orderbookService.executeOrders(execution, fid, executionPrice);
	}
	
	@GetMapping(value = "/statistics1")
	private void statistics1(){
		orderbookService.getStatistics1();
	}
	
}
