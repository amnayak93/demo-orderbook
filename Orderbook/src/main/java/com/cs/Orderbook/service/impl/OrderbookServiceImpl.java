package com.cs.Orderbook.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cs.Orderbook.Entity.ExecutionEntity;
import com.cs.Orderbook.Entity.OrderEntity;
import com.cs.Orderbook.Entity.OrderStatus;
import com.cs.Orderbook.Entity.OrderbookEntity;
import com.cs.Orderbook.Entity.Status;
import com.cs.Orderbook.Exception.ExecutionPriceShouldNotChangeException;
import com.cs.Orderbook.Exception.ExecutionQuantityIsMoreThanTheValidDemandException;
import com.cs.Orderbook.Exception.LimitOrderDoesNotHaveLimitPriceException;
import com.cs.Orderbook.Exception.MarketOrderHasLimitPriceException;
import com.cs.Orderbook.Exception.OrderbookFoundException;
import com.cs.Orderbook.Exception.OrderbookIsAlreadyExecutedException;
import com.cs.Orderbook.Exception.OrderbookIsClosedException;
import com.cs.Orderbook.Exception.OrderbookIsExecutedException;
import com.cs.Orderbook.Exception.OrderbookIsOpenException;
import com.cs.Orderbook.Exception.OrderbookNotFoundException;
import com.cs.Orderbook.repository.OrderbookRepository;
import com.cs.Orderbook.service.OrderbookService;
import com.cs.Orderbook.utils.StaticUtils;

public class OrderbookServiceImpl implements OrderbookService {

	Logger logger = LoggerFactory.getLogger(OrderbookServiceImpl.class);

	@Autowired
	OrderbookRepository orderbookRepository;

	@Override
	public OrderbookEntity openOrderbook(String id) {
		OrderbookEntity orderbookEntity = new OrderbookEntity();
		boolean ifOrderbookExists = checkIfOrderbookExists(id);
		if (ifOrderbookExists) {
			orderbookEntity = orderbookRepository.findbyInstrument(id).get();
			throw new OrderbookFoundException("The orderbook for financial instrument id " + id
					+ " already exists with status as " + orderbookEntity.getStatus() + ". Cannot open the orderbook");
		}
		orderbookEntity.setInstrument(id);
		orderbookEntity.setStatus(Status.OPEN);
		return orderbookRepository.save(orderbookEntity);
	}

	@Override
	public OrderbookEntity closeOrderbook(String id) {
		OrderbookEntity orderbookEntity = new OrderbookEntity();
		if (!checkIfOrderbookExists(id))
			throw new OrderbookNotFoundException("There are no orderbooks for financial instrument id " + id);
		if (checkIfOrderbookIsClosed(id))
			throw new OrderbookIsClosedException(
					"The order book for financial instrument id " + id + " is already closed. So it cannot be closed");
		if (checkIfOrderbookIsExecuted(id))
			throw new OrderbookIsExecutedException(
					"The order book for financial instrument id " + id + " is executed. So it cannot be closed");
		if (checkIfOrderbookIsOpen(id)) {
			orderbookEntity = orderbookRepository.findbyInstrument(id).get();
			orderbookEntity.setStatus(Status.CLOSE);
		}
		return orderbookRepository.save(orderbookEntity);
	}

	@Override
	public OrderbookEntity addOrders(List<OrderEntity> orders, String fid) {
		if (!checkIfOrderbookExists(fid))
			throw new OrderbookNotFoundException("There are no orderbooks for financial instrument id " + fid
					+ ". Please open it first before adding orders");
		if (checkIfOrderbookIsClosed(fid))
			throw new OrderbookIsClosedException(
					"The order book for financial instrument id " + fid + " is closed. So orders cannot be added");
		if (checkIfOrderbookIsExecuted(fid))
			throw new OrderbookIsExecutedException(
					"The order book for financial instrument id " + fid + " is executed cannot add orders");
		OrderbookEntity orderbookEntity = orderbookRepository.findbyInstrument(fid).get();
		List<OrderEntity> ordersToBeAdded = orderbookEntity.getOrders();
		orders.forEach(x -> {
			if (checkIfMarketOrder(x) && checkIfMarketOrderHasLimitPrice(x))
				throw new MarketOrderHasLimitPriceException("The order with order id " + x.getOrderId()
						+ " cannot be added to the orderbook with instrument id " + fid
						+ " because it is a market order but has a limit price");
			else if (checkIfLimitOrder(x) && !checkIfLimitOrderHasLimitPrice(x))
				throw new LimitOrderDoesNotHaveLimitPriceException("The order with order id " + x.getOrderId()
						+ " cannot be added to the orderbook with instrument id " + fid
						+ " because it is a limit order but does not have a limit price");
		});
		orderbookEntity.setOrders(ordersToBeAdded);
		return orderbookRepository.save(orderbookEntity);
	}

	@Override
	public OrderbookEntity executeOrders(ExecutionEntity execution, String fid) {
		String instrument = fid;
		List<ExecutionEntity> executions = new ArrayList<>();
		if (!checkIfOrderbookExists(instrument))
			throw new OrderbookNotFoundException(
					"There are no orderbooks for financial instrument id " + instrument + " cannot add executions");
		if (checkIfOrderbookIsOpen(instrument))
			throw new OrderbookIsOpenException("The orderbook for financial instrument id " + instrument
					+ " is open. The orderbook has to be closed to add executions");
		if (checkIfOrderbookIsExecuted(instrument))
			throw new OrderbookIsAlreadyExecutedException("The orderbook for financial instrument id " + instrument
					+ " is already executed . Cannot accept new executions");

		OrderbookEntity orderBook = orderbookRepository.findbyInstrument(instrument).get();
		if (orderBook.getExecutions() == null) {
			orderBook = determineValidOrders(execution, orderBook);
		} else {
			executions = orderBook.getExecutions();
			if (execution.getExecutionPrice().compareTo(orderBook.getExecutions().get(0).getExecutionPrice()) != 0)
				throw new ExecutionPriceShouldNotChangeException(
						"The Execution Price is not equal to the initial execution price of the orderbook with instrument id "
								+ instrument + " this execution cannot be executed. ");
			if (orderBook.getExecutions().stream().map(ExecutionEntity::getExecutionQuantity)
					.reduce(BigInteger.ZERO, BigInteger::add).add(execution.getExecutionQuantity())
					.longValue() > orderBook.getOrders().stream()
							.filter(record -> record.getStatus().equals(OrderStatus.VALID)).map(OrderEntity::getQuantiy)
							.reduce(BigInteger.ZERO, BigInteger::add).longValue())
				throw new ExecutionQuantityIsMoreThanTheValidDemandException(
						"The execution cannot be executed because it is more than the valid demand for the orderbook with intrument id "
								+ instrument);
			executions.add(execution);
			orderBook.setExecutions(executions);
		}
		linearDistributionAmongVaildOrders(orderBook, execution);
		if (orderBook.getExecutions().stream().map(ExecutionEntity::getExecutionQuantity)
				.reduce(BigInteger.ZERO, BigInteger::add).longValue() == orderBook.getOrders().stream()
						.filter(x -> x.getStatus().equals(OrderStatus.VALID)).map(OrderEntity::getQuantiy)
						.reduce(BigInteger.ZERO, BigInteger::add).longValue())
			orderBook.setStatus(Status.EXECUTE);
		return orderbookRepository.save(orderBook);
	}

	private OrderbookEntity determineValidOrders(ExecutionEntity execution, OrderbookEntity orderbookEntity) {
		List<ExecutionEntity> executions = new ArrayList<>();
		executions.add(execution);
		List<OrderEntity> orders = orderbookEntity.getOrders();
		orders.stream().filter(record -> StaticUtils.MARKET.equalsIgnoreCase(record.getOrderType()))
				.forEach(record -> record.setStatus(OrderStatus.VALID));
		orders.stream().filter(record -> StaticUtils.LIMIT.equalsIgnoreCase(record.getOrderType())).forEach(record -> {
			if (record.getPrice().compareTo(execution.getExecutionPrice()) != -1)
				record.setStatus(OrderStatus.VALID);
			else
				record.setStatus(OrderStatus.INVALID);
		});
		orderbookEntity.setExecutions(executions);
		return orderbookEntity;
	}

	private void linearDistributionAmongVaildOrders(OrderbookEntity orderBook, ExecutionEntity execution) {
		List<OrderEntity> orderList = orderBook.getOrders().stream()
				.filter(x -> x.getStatus().equals(OrderStatus.VALID)).collect(Collectors.toList());
		BigInteger gcd = findGcdOfOrderQuantities(orderList);
		List<BigInteger> ratioList = calculateRatioList(orderList, gcd);
		BigInteger ratioSum = ratioList.stream().reduce(BigInteger::add).get();
		for (int i = 0; i < orderList.size(); i++) {
			if (orderList.get(i).getExecutionQuantity() == null)
				orderList.get(i).setExecutionQuantity(
						execution.getExecutionQuantity().multiply(ratioList.get(i)).divide(ratioSum));
			else
				orderList.get(i).setExecutionQuantity(orderList.get(i).getExecutionQuantity()
						.add(execution.getExecutionQuantity().multiply(ratioList.get(i)).divide(ratioSum)));
		}
	}

	private BigInteger findGcdOfOrderQuantities(List<OrderEntity> orderList) {
		BigInteger result = orderList.get(0).getQuantiy();
		for (int i = 1; i < orderList.size(); i++) {
			result = orderList.get(i).getQuantiy().gcd(result);
		}
		return result;
	}

	private List<BigInteger> calculateRatioList(List<OrderEntity> orderList, BigInteger gcd) {
		return orderList.stream().map(x -> x.getQuantiy().divide(gcd)).collect(Collectors.toList());
	}

	@Override
	public void openOrCloseBook(OrderbookEntity book) {
		orderbookRepository.save(book);

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
		String id = record.getInstrument();
		List<OrderEntity> orders = record.getOrders();
		orders.sort((OrderEntity o1, OrderEntity o2) -> o1.getEntryDate().compareTo(o2.getEntryDate()));
		logger.info("First Entry Date for OrderEntity book " + id + " is " + orders.get(0)
				+ " and the last entry date is " + orders.get(orders.size() - 1));
	}

	private void getBiggestAndSmallestOrder(OrderbookEntity record) {
		String id = record.getInstrument();
		List<OrderEntity> orders = record.getOrders();
		orders.sort((OrderEntity o1, OrderEntity o2) -> o1.getQuantiy().intValue() - o2.getQuantiy().intValue());
		logger.info("Biggest OrderEntity for OrderEntity book " + id + " is : " + orders.get(orders.size() - 1)
				+ " and smallest order is " + orders.get(0));
	}

	private void printNumberOfOrdersInEachBook(OrderbookEntity record) {
		String id = record.getInstrument();
		BigInteger totOrders = BigInteger
				.valueOf(record.getOrders().stream().mapToInt(x -> x.getQuantiy().intValue()).sum());
		logger.info("For OrderEntity Book " + id + " number of orders is " + record.getOrders().size());
		logger.info("For OrderEntity Book " + id + " total demand as accumulated order quantity is " + totOrders);
	}

	private boolean checkIfOrderbookExists(String id) {
		return orderbookRepository.findbyInstrument(id).isPresent();
	}

	private boolean checkIfOrderbookIsOpen(String id) {
		if (orderbookRepository.findbyInstrument(id).get().getStatus() == Status.OPEN)
			return true;
		else
			return false;
	}

	private boolean checkIfOrderbookIsClosed(String id) {
		if (orderbookRepository.findbyInstrument(id).get().getStatus() == Status.CLOSE)
			return true;
		else
			return false;
	}

	private boolean checkIfOrderbookIsExecuted(String id) {
		if (orderbookRepository.findbyInstrument(id).get().getStatus() == Status.EXECUTE)
			return true;
		else
			return false;
	}

	private boolean checkIfMarketOrderHasLimitPrice(OrderEntity x) {
		return Optional.ofNullable(x.getPrice()).isPresent();
	}

	private boolean checkIfLimitOrder(OrderEntity x) {
		return x.getOrderType().equalsIgnoreCase(StaticUtils.LIMIT);
	}

	private boolean checkIfLimitOrderHasLimitPrice(OrderEntity x) {
		return Optional.ofNullable(x.getPrice()).isPresent();
	}

	private boolean checkIfMarketOrder(OrderEntity x) {
		return x.getOrderType().equalsIgnoreCase(StaticUtils.MARKET);
	}

}
