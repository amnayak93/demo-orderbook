package com.cs.Orderbook.service.impl;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.cs.Orderbook.Entity.ExecutionEntity;
import com.cs.Orderbook.Entity.OrderEntity;
import com.cs.Orderbook.Entity.OrderbookEntity;
import com.cs.Orderbook.Exception.ExecutionPriceShouldNotChangeException;
import com.cs.Orderbook.Exception.ExecutionQuantityIsMoreThanTheValidDemandException;
import com.cs.Orderbook.Exception.LimitOrderDoesNotHaveLimitPriceException;
import com.cs.Orderbook.Exception.MarketOrderHasLimitPriceException;
import com.cs.Orderbook.Exception.OrderDoesNotExistForTheGivenOrderIdException;
import com.cs.Orderbook.Exception.OrderbookFoundException;
import com.cs.Orderbook.Exception.OrderbookIsNotClosedException;
import com.cs.Orderbook.Exception.OrderbookIsNotOpenException;
import com.cs.Orderbook.Exception.OrderbookNotFoundException;
import com.cs.Orderbook.repository.OrderRepository;
import com.cs.Orderbook.repository.OrderbookRepository;
import com.cs.Orderbook.service.OrderbookService;
import com.cs.Orderbook.utils.OrderStatus;
import com.cs.Orderbook.utils.OrderType;
import com.cs.Orderbook.utils.Status;

public class OrderbookServiceImpl implements OrderbookService {

	Logger logger = LoggerFactory.getLogger(OrderbookServiceImpl.class);

	@Autowired
	OrderbookRepository orderbookRepository;

	@Autowired
	OrderRepository orderRepository;

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
		OrderbookEntity orderbookEntity = orderbookRepository.findbyInstrument(id).orElseThrow(
				() -> new OrderbookNotFoundException("There are no orderbooks for financial instrument id " + id));
		if (!checkIfOrderbookIsOpen(id))
			throw new OrderbookIsNotOpenException(
					"The Orderbook is not opened for instrument id " + id + " so it cannot be closed");
		else {
			orderbookEntity = orderbookRepository.findbyInstrument(id).get();
			orderbookEntity.setStatus(Status.CLOSE);
		}
		return orderbookRepository.save(orderbookEntity);
	}

	@Override
	public OrderbookEntity addOrders(List<OrderEntity> orders, String fid) {

		OrderbookEntity orderbookEntity = orderbookRepository.findbyInstrument(fid)
				.orElseThrow(() -> new OrderbookNotFoundException("There are no orderbooks for financial instrument id "
						+ fid + ". Please open it first before adding orders"));
		if (!checkIfOrderbookIsOpen(fid))
			throw new OrderbookIsNotOpenException(
					"The Orderbook is not opened for instrument id " + fid + " Please open it to add orders");
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
		OrderbookEntity orderBook = orderbookRepository.findbyInstrument(fid)
				.orElseThrow(() -> new OrderbookNotFoundException("There are no orderbooks for financial instrument id "
						+ instrument + " cannot add executions"));
		if (!checkIfOrderbookIsClosed(fid))
			throw new OrderbookIsNotClosedException(
					"The orderbook for instrument id " + fid + " is not closed. Executions cannot be added");
		if (orderBook.getExecutions() != null && !orderBook.getExecutions().isEmpty()) {
			executions = orderBook.getExecutions();
			if (execution.getExecutionPrice().compareTo(orderBook.getExecutions().get(0).getExecutionPrice()) != 0)
				throw new ExecutionPriceShouldNotChangeException(
						"The Execution Price is not equal to the initial execution price of the orderbook with instrument id "
								+ instrument + " this execution cannot be executed. ");
			if (checkIfExecutionCannotBeAdded(execution, orderBook))
				throw new ExecutionQuantityIsMoreThanTheValidDemandException(
						"The execution cannot be executed because it is more than the valid demand for the orderbook with intrument id "
								+ instrument);
			executions.add(execution);
			orderBook.setExecutions(executions);
		} else {
			orderBook = determineValidOrders(execution, orderBook);
		}
		linearDistributionAmongVaildOrders(orderBook, execution);
		if (orderBook.getExecutions().stream().map(ExecutionEntity::getExecutionQuantity)
				.collect(Collectors.summingLong(Long::longValue)).longValue() == orderBook.getOrders().stream()
						.filter(x -> x.getStatus().equals(OrderStatus.VALID)).map(OrderEntity::getQuantiy)
						.collect(Collectors.summingLong(Long::longValue)).longValue())
			orderBook.setStatus(Status.EXECUTE);
		return orderbookRepository.save(orderBook);
	}

	private boolean checkIfExecutionCannotBeAdded(ExecutionEntity execution, OrderbookEntity orderBook) {
		return execution.getExecutionQuantity() > orderBook.getOrders().stream()
				.filter(record -> record.getStatus().equals(OrderStatus.VALID)).map(OrderEntity::getQuantiy)
				.collect(Collectors.summingLong(Long::longValue)).longValue()
				- orderBook.getExecutions().stream().map(ExecutionEntity::getExecutionQuantity)
						.collect(Collectors.summingLong(Long::longValue)).longValue();
	}

	private OrderbookEntity determineValidOrders(ExecutionEntity execution, OrderbookEntity orderbookEntity) {
		List<ExecutionEntity> executions = new ArrayList<>();
		executions.add(execution);
		List<OrderEntity> orders = orderbookEntity.getOrders();
		orders.stream().filter(record -> OrderType.MARKET.equals(record.getOrderType()))
				.forEach(record -> record.setStatus(OrderStatus.VALID));
		orders.stream().filter(record -> OrderType.LIMIT.equals(record.getOrderType())).forEach(record -> {
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
		BigDecimal roundOfTotalDistributedExecution = new BigDecimal(0);
		BigDecimal executionToBeDistributed = new BigDecimal(0);
		for (int i = 0; i < orderList.size(); i++) {
			if (orderList.get(i).getExecutionQuantity() == null) {
				executionToBeDistributed = BigDecimal.valueOf(execution.getExecutionQuantity())
						.multiply(new BigDecimal(ratioList.get(i)))
						.divide(new BigDecimal(ratioSum), 0, RoundingMode.HALF_DOWN);
				roundOfTotalDistributedExecution = roundOfTotalDistributedExecution.add(executionToBeDistributed);
				orderList.get(i).setExecutionQuantity(executionToBeDistributed.longValue());
				orderList.get(i).setExecutionPrice(orderBook.getExecutions().get(0).getExecutionPrice());
			} else {
				executionToBeDistributed = BigDecimal.valueOf(execution.getExecutionQuantity())
						.multiply(new BigDecimal(ratioList.get(i)))
						.divide(new BigDecimal(ratioSum), 0, RoundingMode.HALF_DOWN);
				roundOfTotalDistributedExecution = roundOfTotalDistributedExecution.add(executionToBeDistributed);
				orderList.get(i).setExecutionQuantity(
						orderList.get(i).getExecutionQuantity().longValue() + executionToBeDistributed.longValue());
				orderList.get(i).setExecutionPrice(orderBook.getExecutions().get(0).getExecutionPrice());
			}
		}

		BigDecimal difference = BigDecimal.valueOf(execution.getExecutionQuantity())
				.subtract(roundOfTotalDistributedExecution).abs();

		if (roundOfTotalDistributedExecution.compareTo(BigDecimal.valueOf(execution.getExecutionQuantity())) == 1) {
			int i = 0;
			while (difference.intValue() != 0) {
				orderList.get(i).setExecutionQuantity(orderList.get(i).getExecutionQuantity().longValue() - 1);
				i++;
				difference = difference.subtract(BigDecimal.ONE);
			}
		} else if (roundOfTotalDistributedExecution
				.compareTo(BigDecimal.valueOf(execution.getExecutionQuantity())) == -1) {
			int i = 0;
			while (difference.intValue() != 0) {
				if (!checkIfExecutionIsFulfilled(orderList, i)) {
					orderList.get(i).setExecutionQuantity(orderList.get(i).getExecutionQuantity().longValue() + 1);
					difference = difference.subtract(BigDecimal.ONE);
				}
				i++;
			}
		}
	}

	private boolean checkIfExecutionIsFulfilled(List<OrderEntity> orderList, int i) {
		return orderList.get(i).getExecutionQuantity().longValue() >= orderList.get(i).getQuantiy();
	}

	private BigInteger findGcdOfOrderQuantities(List<OrderEntity> orderList) {
		BigInteger result = BigInteger.valueOf(orderList.get(0).getQuantiy());
		for (int i = 1; i < orderList.size(); i++) {
			result = BigInteger.valueOf(orderList.get(i).getQuantiy()).gcd(result);
		}
		return result;
	}

	private List<BigInteger> calculateRatioList(List<OrderEntity> orderList, BigInteger gcd) {
		return orderList.stream().map(x -> BigInteger.valueOf(x.getQuantiy()).divide(gcd)).collect(Collectors.toList());
	}

	@Override
	public void printFirstStatistics() {
		List<OrderbookEntity> orderBooks = orderbookRepository.findAll();
		orderBooks.stream().forEach(record -> {
			logger.info("========================================================Printing statistics for order book "
					+ record.getInstrument() + " =================================================================");
			printNumberOfOrdersInEachBook(record);
			printBiggestAndSmallestOrder(record);
			printFirstAndLastEntryOfOrder(record);
			printLimitBreakDown(record);
			printValidLimitBreakDown(record);
			printInvalidLimitBreakDown(record);
			logger.info(
					"==============================================================End of statistics for Order book "
							+ record.getInstrument()
							+ " =================================================================================");
		});
	}

	@Override
	public OrderEntity printSecondStatistics(long orderId) {
		OrderEntity order = orderRepository.findById(orderId)
				.orElseThrow(() -> new OrderDoesNotExistForTheGivenOrderIdException(
						"Order does not exist for the order id " + orderId));
		logger.info(
				"===============================================================Printing Statistics2 for Order ==============================================================================================");
		if (order.getStatus().equals(OrderStatus.VALID))
			logger.info("The Order with order id " + orderId + " is a vaild order");
		else if (order.getStatus().equals(OrderStatus.INVALID))
			logger.info("The Order with order id " + orderId + " is an invaild order");
		if (order.getOrderType().equals(OrderType.LIMIT))
			logger.info("The Order with order id " + orderId + "has a limit price of " + order.getPrice());
		logger.info("The execution quantity for the order with order id " + orderId + " is "
				+ order.getExecutionQuantity());
		logger.info("The execution price for the order with order id " + orderId + " is " + order.getExecutionPrice());
		logger.info(
				"=============================================================End of Statistics2 for Order =================================================================================");
		return order;

	}

	private void printInvalidLimitBreakDown(OrderbookEntity record) {
		Map<BigDecimal, Long> invalidLimitMap = record.getOrders().stream()
				.filter(x -> x.getStatus().equals(OrderStatus.INVALID))
				.collect(Collectors.groupingBy(OrderEntity::getPrice, Collectors.counting()));
		logger.info("Limit Prices break down for invalid orders in orderbook " + record.getInstrument() + " is "
				+ invalidLimitMap);
	}

	private void printValidLimitBreakDown(OrderbookEntity record) {
		Map<BigDecimal, Long> validLimitMap = record.getOrders().stream()
				.filter(x -> x.getOrderType().equals(OrderType.LIMIT))
				.filter(x -> x.getStatus().equals(OrderStatus.VALID))
				.collect(Collectors.groupingBy(OrderEntity::getPrice, Collectors.counting()));
		logger.info("Limit Prices break down for valid orders in orderbook " + record.getInstrument() + " is "
				+ validLimitMap);
	}

	private void printLimitBreakDown(OrderbookEntity record) {
		Map<BigDecimal, Long> limitMap = record.getOrders().stream()
				.filter(x -> x.getOrderType().equals(OrderType.LIMIT))
				.collect(Collectors.groupingBy(OrderEntity::getPrice, Collectors.counting()));
		logger.info("Limit Prices break down for orders in orderbook " + record.getInstrument() + " is " + limitMap);
	}

	private void printFirstAndLastEntryOfOrder(OrderbookEntity record) {
		String id = record.getInstrument();
		List<OrderEntity> orders = record.getOrders();
		orders.sort((OrderEntity o1, OrderEntity o2) -> o1.getEntryDate().compareTo(o2.getEntryDate()));
		logger.info("First Entry Date for Order book " + id + " is " + orders.get(0).getEntryDate()
				+ " and the last entry date is " + orders.get(orders.size() - 1).getEntryDate());
	}

	private void printBiggestAndSmallestOrder(OrderbookEntity record) {
		String id = record.getInstrument();
		List<OrderEntity> orders = record.getOrders();
		orders.sort((OrderEntity o1, OrderEntity o2) -> o1.getQuantiy().intValue() - o2.getQuantiy().intValue());
		logger.info("Biggest Order for Order book " + id + " is : " + orders.get(orders.size() - 1).getQuantiy()
				+ " and smallest order is " + orders.get(0).getQuantiy());
	}

	private void printNumberOfOrdersInEachBook(OrderbookEntity record) {
		String id = record.getInstrument();
		Long totalDemand = record.getOrders().stream().map(OrderEntity::getQuantiy)
				.collect(Collectors.summingLong(Long::longValue));
		long validOrdersCount = calculateValidOrInvalidCount(record, OrderStatus.VALID);
		long invalidOrdersCount = calculateValidOrInvalidCount(record, OrderStatus.INVALID);
		Long validDemand = calculateValidOrInvalidDemand(record, OrderStatus.VALID);
		Long invalidDemand = calculateValidOrInvalidDemand(record, OrderStatus.INVALID);
		Long accumulatedExecution = record.getExecutions().stream().map(ExecutionEntity::getExecutionQuantity)
				.collect(Collectors.summingLong(Long::longValue));
		BigDecimal executionPrice = record.getExecutions().get(0).getExecutionPrice();
		logger.info("For Order Book " + id + " number of orders is " + record.getOrders().size());
		logger.info("Number of Valid Orders in Book " + id + " is " + validOrdersCount);
		logger.info("Number of Invalid Orders in Book " + id + " is " + invalidOrdersCount);
		logger.info("For Order Book " + id + " total demand as accumulated order quantity is " + totalDemand);
		logger.info("For Order Book " + id + " valid demands is " + validDemand);
		logger.info("For Order Book " + id + " invalid demands is " + invalidDemand);
		logger.info("For Order Book " + id + " accumulated execution quantity is " + accumulatedExecution);
		logger.info("For Order Book " + id + " accumulated execution price is " + executionPrice);
	}

	private long calculateValidOrInvalidCount(OrderbookEntity record, OrderStatus orderStatus) {
		return record.getOrders().stream().filter(x -> x.getStatus().equals(orderStatus)).count();
	}

	private Long calculateValidOrInvalidDemand(OrderbookEntity record, OrderStatus orderStatus) {
		return record.getOrders().stream().filter(x -> x.getStatus().equals(orderStatus)).map(OrderEntity::getQuantiy)
				.collect(Collectors.summingLong(Long::longValue));
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

	private boolean checkIfMarketOrderHasLimitPrice(OrderEntity x) {
		return Optional.ofNullable(x.getPrice()).isPresent();
	}

	private boolean checkIfLimitOrder(OrderEntity x) {
		return x.getOrderType().equals(OrderType.LIMIT);
	}

	private boolean checkIfLimitOrderHasLimitPrice(OrderEntity x) {
		return Optional.ofNullable(x.getPrice()).isPresent();
	}

	private boolean checkIfMarketOrder(OrderEntity x) {
		return x.getOrderType().equals(OrderType.MARKET);
	}

}
