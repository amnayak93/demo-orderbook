package com.cs.Orderbook.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.cs.Orderbook.Entity.OrderEntity;
import com.cs.Orderbook.Entity.OrderbookEntity;
import com.cs.Orderbook.Exception.OrderbookIsClosedException;
import com.cs.Orderbook.Exception.OrderbookIsNotClosedException;
import com.cs.Orderbook.Exception.OrderbookIsNotOpenException;
import com.cs.Orderbook.Exception.OrderbookNotFoundException;
import com.cs.Orderbook.model.Execution;
import com.cs.Orderbook.model.Orderbook;
import com.cs.Orderbook.repository.OrderbookRepository;
import com.cs.Orderbook.utils.ConversionUtils;
import com.cs.Orderbook.utils.StaticUtils;

@Service
public class OrderbookService {

	Logger logger = LoggerFactory.getLogger(OrderbookService.class);

	@Autowired
	OrderbookRepository orderbookRepository;

	@Autowired
	ConversionUtils conversionUtils;

	public Orderbook openOrderbook(String id) {
		Orderbook orderbook = new Orderbook();
		boolean ifOrderbookExists = checkIfOrderbookExists(id);
		if (!ifOrderbookExists)
			throw new OrderbookNotFoundException("The are no orderbooks for financial instrument id " + id);
		boolean ifOrderbookIsClosed = checkIfOrderbookIsClosed(id);
		if (!ifOrderbookIsClosed)
			throw new OrderbookIsNotClosedException(
					"The order book for financial instrument id " + id + " is either already open or executed");
		orderbook = conversionUtils.convertToModelForOrderbook(orderbookRepository.findById(id).get());
		return orderbook;
	}

	public Orderbook closeOrderbook(String id) {
		Orderbook orderbook = new Orderbook();
		boolean ifOrderbookExists = checkIfOrderbookExists(id);
		if (!ifOrderbookExists)
			throw new OrderbookNotFoundException("There are no orderbooks for financial instrument id " + id);
		boolean ifOrderbookIsOpen = checkIfOrderbookIsOpen(id);
		if (!ifOrderbookIsOpen)
			throw new OrderbookIsNotOpenException(
					"The order book for financial instrument id " + id + " is not open. So it cannot be closed");
		orderbook = conversionUtils.convertToModelForOrderbook(orderbookRepository.findById(id).get());
		return orderbook;
	}

	public ResponseEntity<Object> addOrders(List<OrderEntity> orders, String fid) {
		if (!checkIfOrderbookExists(fid))
			throw new OrderbookNotFoundException("There are no orderbooks for financial instrument id " + fid);
		if (checkIfOrderbookIsClosed(fid))
			throw new OrderbookIsClosedException(
					"The order book for financial instrument id " + fid + " is closed. So orders cannot be added");
		OrderbookEntity orderbookEntity = orderbookRepository.findById(fid).get();
		List<OrderEntity> ordersToBeAdded = orderbookEntity.getOrders();
		ordersToBeAdded.addAll(orders);
		orderbookEntity.setOrders(ordersToBeAdded);
		orderbookRepository.save(orderbookEntity);
		return ResponseEntity.noContent().build();
	}

	public ResponseEntity<Object> executeOrders(Execution execution, String fid, BigDecimal executionPrice) {
		if (!checkIfOrderbookExists(fid))
			throw new OrderbookNotFoundException("There are no orderbooks for financial instrument id " + fid);
		if (!checkIfOrderbookIsClosed(fid))
			throw new OrderbookIsNotClosedException(
					"The order book for financial instrument id " + fid + " is not closed. Executions cannot be added");
		OrderbookEntity orderBook = orderbookRepository.findById(fid).get();
		if (orderBook.isFirstExecutionFlag()) {
			orderBook = determineValidOrders(execution, orderBook);
		}
		orderBook = linearDistributionAmongVaildOrders(orderBook, execution);
		orderBook.setTotExecutionOrders(orderBook.getTotExecutionOrders().add(execution.getQuantity().toBigInteger()));
		if (orderBook.getTotExecutionOrders().compareTo(orderBook.getValidOrders()) == 0)
			orderBook.setStatus(StaticUtils.EXECUTE);
		orderbookRepository.save(orderBook);
		return ResponseEntity.noContent().build();
	}

	private OrderbookEntity linearDistributionAmongVaildOrders(OrderbookEntity orderBook, Execution execution) {
		List<OrderEntity> orderList = orderBook.getOrders();
		Long orderQuantity = orderList.stream().mapToLong(x -> x.getQuantiy().longValue()).sum();
		List<Integer> ratioList = orderList.stream().map(record -> record.getQuantiy()
				.divide(BigDecimal.valueOf(orderQuantity)).multiply(BigDecimal.valueOf(orderList.size())).ROUND_HALF_UP)
				.collect(Collectors.toList());
		Integer ratioSum = ratioList.stream().collect(Collectors.summingInt(Integer::intValue));
		List<OrderEntity> newOrderList = new ArrayList<>();
		for (int i = 0; i < orderList.size(); i++) {
			OrderEntity order = orderList.get(i);
			order.setQuantiy(BigDecimal.valueOf(order.getQuantiy().add(execution.getQuantity().divide(
					BigDecimal.valueOf(ratioSum).multiply(BigDecimal.valueOf(ratioList.get(i))))).ROUND_HALF_UP));
			newOrderList.add(order);
		}
		orderBook.setOrders(newOrderList);
		return orderBook;
	}

	private OrderbookEntity determineValidOrders(Execution execution, OrderbookEntity orderbookEntity) {
		List<OrderEntity> validOrders = orderbookEntity.getOrders().stream()
				.filter(record -> StaticUtils.MARKET.equalsIgnoreCase(record.getOrderType()))
				.collect(Collectors.toList());
		List<OrderEntity> limitOrders = orderbookEntity.getOrders().stream()
				.filter(record -> StaticUtils.LIMIT.equalsIgnoreCase(record.getOrderType()))
				.collect(Collectors.toList());
		validOrders.addAll(limitOrders.stream().filter(record -> {
			if (record.getPrice().compareTo(execution.getPrice()) != -1)
				return true;
			else
				return false;
		}).collect(Collectors.toList()));
		orderbookEntity.setOrders(validOrders);
		orderbookEntity.setValidOrders(BigInteger.valueOf(validOrders.size()));
		orderbookEntity.setFirstExecutionFlag(false);
		return orderbookEntity;
	}

	private boolean checkIfOrderbookIsOpen(String id) {
		if (orderbookRepository.findById(id).get().getStatus().equalsIgnoreCase(StaticUtils.OPEN))
			return true;
		else
			return false;
	}

	private boolean checkIfOrderbookIsClosed(String id) {
		if (orderbookRepository.findById(id).get().getStatus().equalsIgnoreCase(StaticUtils.CLOSE))
			return true;
		else
			return false;
	}

	private boolean checkIfOrderbookExists(String id) {
		return orderbookRepository.findById(id).isPresent();
	}

	public ResponseEntity<Object> openOrCloseBook(Orderbook book) {
		orderbookRepository.save(conversionUtils.convertToEntityForOrderbook(book));
		return ResponseEntity.noContent().build();
	}

	public void getStatistics1() {
		List<OrderbookEntity> orderBooks = orderbookRepository.findAll();
		orderBooks.stream().forEach(record -> {
			printNumberOfOrdersInEachBook(record);
			getBiggestAndSmallestOrder(record);
			getFirstAndLastEntryOfOrder(record);
		});

	}

	private void getFirstAndLastEntryOfOrder(OrderbookEntity record) {
		String id = record.getFinancialId();
		List<OrderEntity> orders = record.getOrders();
		orders.sort((OrderEntity o1, OrderEntity o2) -> o1.getEntryDate().compareTo(o2.getEntryDate()));
		logger.info("First Entry Date for OrderEntity book " + id + " is " + orders.get(0) + " and the last entry date is "
				+ orders.get(orders.size() - 1));
	}

	private void getBiggestAndSmallestOrder(OrderbookEntity record) {
		String id = record.getFinancialId();
		List<OrderEntity> orders = record.getOrders();
		orders.sort((OrderEntity o1, OrderEntity o2) -> o1.getQuantiy().intValue() - o2.getQuantiy().intValue());
		logger.info("Biggest OrderEntity for OrderEntity book " + id + " is : " + orders.get(orders.size() - 1)
				+ " and smallest order is " + orders.get(0));
	}

	private void printNumberOfOrdersInEachBook(OrderbookEntity record) {
		String id = record.getFinancialId();
		BigInteger totOrders = BigInteger
				.valueOf(record.getOrders().stream().mapToInt(x -> x.getQuantiy().intValue()).sum());
		logger.info("For OrderEntity Book " + id + " number of orders is " + record.getOrders().size());
		logger.info("For OrderEntity Book " + id + " total demand as accumulated order quantity is " + totOrders);
	}

}
