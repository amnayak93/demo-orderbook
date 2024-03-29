package com.cs.Orderbook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

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
import com.cs.Orderbook.service.impl.OrderbookServiceImpl;
import com.cs.Orderbook.utils.OrderStatus;
import com.cs.Orderbook.utils.OrderType;
import com.cs.Orderbook.utils.Status;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderbookServiceImplTests {

	@TestConfiguration
	static class OrderbookApplicationTestConfiguration {
		@Bean
		public OrderbookService orderbookService() {
			return new OrderbookServiceImpl();
		}
	}

	@Autowired
	private OrderbookService orderbookService;

	@Autowired
	private OrderbookRepository orderbookRepository;

	@Autowired
	private OrderRepository orderRepository;

	private OrderbookEntity orderbook;

	private ExecutionEntity execution;

	private ExecutionEntity executionWithChangedPrice;

	private ExecutionEntity execution3;

	private ExecutionEntity execution4;

	private LocalDateTime today = LocalDateTime.now();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/*
	 * Instrument Fi1 exists so it throws OrderbookFoundException when you try
	 * to open the Orderbook
	 */
	@Test(expected = OrderbookFoundException.class)
	public void whenOrderbookExistsButOpenOrderBook() {
		orderbook = new OrderbookEntity("Fi1", Status.OPEN);
		orderbookRepository.save(orderbook);
		String instrument = "Fi1";
		orderbookService.openOrderbook(instrument);
	}

	@Test
	public void whenOrderbookForTheGivenInstrumentDoesNotExistOpenAndGetOrderbook() {
		String instrument = "Fi1";
		OrderbookEntity orderbook = orderbookService.openOrderbook(instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(Status.OPEN, orderbook.getStatus());
	}

	@Test(expected = OrderbookNotFoundException.class)
	public void whenOrderbookForTheGivenInstrumentDoesNotExistButCloseOrderbook() {
		String instrument = "Fi1";
		orderbookService.closeOrderbook(instrument);
	}

	@Test(expected = OrderbookIsNotOpenException.class)
	public void whenOrderbookExistsButIsNotOpenCloseOrderbook() {
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		orderbookRepository.save(orderbook);
		String instrument = "Fi1";
		orderbookService.closeOrderbook(instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsOpenCloseOrderbook() {
		orderbook = new OrderbookEntity("Fi1", Status.OPEN);
		orderbookRepository.save(orderbook);
		String instrument = "Fi1";
		OrderbookEntity orderbook = orderbookService.closeOrderbook(instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(Status.CLOSE, orderbook.getStatus());
	}

	@Test(expected = OrderbookNotFoundException.class)
	public void whenOrderbookDoesNotExistButAddOrdersToOrderbook() {
		List<OrderEntity> orders = new ArrayList<>();
		orders.add(new OrderEntity(Long.valueOf(40), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		String instrument = "Fi1";
		orderbookService.addOrders(orders, instrument);
	}

	@Test(expected = OrderbookIsNotOpenException.class)
	public void whenOrderbookExistsButIsNotOpenAddOrdersToOrderbook() {
		List<OrderEntity> orders = new ArrayList<>();
		orders.add(new OrderEntity(Long.valueOf(40), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		orderbook.setOrders(orders);
		orderbookRepository.save(orderbook);
		String instrument = "Fi1";
		orderbookService.addOrders(orders, instrument);
	}

	@Test(expected = MarketOrderHasLimitPriceException.class)
	public void whenOrderbookExistsAndIsOpenButAddOrdersHavingMarketOrderAndLimitPrice() {
		List<OrderEntity> orders = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.OPEN);
		orders.add(new OrderEntity(Long.valueOf(40), today, OrderType.LIMIT, BigDecimal.valueOf(80)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.MARKET, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orderbookRepository.save(orderbook);
		String instrument = "Fi1";
		orderbookService.addOrders(orders, instrument);
	}

	@Test(expected = LimitOrderDoesNotHaveLimitPriceException.class)
	public void whenOrderbookExistsAndIsOpenButAddOrdersHavingLimitOrderDoesNotHaveLimitPrice() {
		List<OrderEntity> orders = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.OPEN);
		orders.add(new OrderEntity(Long.valueOf(40), today, OrderType.LIMIT, BigDecimal.valueOf(80)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT));
		orderbook.setOrders(orders);
		orderbookRepository.save(orderbook);
		String instrument = "Fi1";
		orderbookService.addOrders(orders, instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsOpenAndOrdersHavingMarketOrderThatDoesNotHaveLimitPrice() {
		List<OrderEntity> orders = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.OPEN);
		orders.add(new OrderEntity(Long.valueOf(40), today, OrderType.LIMIT, BigDecimal.valueOf(80)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.MARKET));
		orderbook.setOrders(orders);
		orderbookRepository.save(orderbook);
		String instrument = "Fi1";
		OrderbookEntity orderbook = orderbookService.addOrders(orders, instrument);
		assertNotNull(orderbook);
		assertEquals(orderbook.getInstrument(), orderbook.getInstrument());
		assertEquals(orderbook.getStatus(), Status.OPEN);
		assertEquals(orderbook.getOrders().size(), orders.size());
	}

	@Test(expected = OrderbookNotFoundException.class)
	public void whenOrderbookDoesNotExistButExecuteOrderbook() {
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		String instrument = "Fi1";
		orderbookService.executeOrders(execution, instrument);
	}

	@Test(expected = OrderbookIsNotClosedException.class)
	public void whenOrderbookExistsAndIsNotClosedExecuteOrderbook() {
		orderbook = new OrderbookEntity("Fi1", Status.OPEN);
		orderbookRepository.save(orderbook);
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		String instrument = "Fi1";
		orderbookService.executeOrders(execution, instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndFirstExecutionGivesAllValidOrdersForOrderbook() {
		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		executions.add(execution);
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(3), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(7), today, OrderType.MARKET));
		orders.add(new OrderEntity(Long.valueOf(11), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orderbookRepository.save(orderbook);
		OrderbookEntity orderbook = orderbookService.executeOrders(execution, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), Long.valueOf(2));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), Long.valueOf(5));
		assertEquals(orderbook.getOrders().get(2).getExecutionQuantity(), Long.valueOf(8));
		assertEquals(
				orderbook.getOrders().stream().filter(record -> record.getStatus().equals(OrderStatus.VALID)).count(),
				3l);
		assertEquals(orderbook.getStatus(), Status.CLOSE);
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndFirstExecutionAndOneInvalidOrderForOrderbook() {
		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		executions.add(execution);
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.MARKET));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(80)));
		orderbook.setOrders(orders);
		orderbookRepository.save(orderbook);
		OrderbookEntity orderbook = orderbookService.executeOrders(execution, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(
				orderbook.getOrders().stream().filter(record -> record.getStatus().equals(OrderStatus.VALID)).count(),
				2l);
		assertEquals(
				orderbook.getOrders().stream().filter(record -> record.getStatus().equals(OrderStatus.INVALID)).count(),
				1l);
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), Long.valueOf(5));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), Long.valueOf(10));
		assertEquals(orderbook.getStatus(), Status.CLOSE);
	}

	@Test(expected = ExecutionPriceShouldNotChangeException.class)
	public void whenOrderbookExistsAndIsClosedButExecutionPriceIsChanged() {

		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		executionWithChangedPrice = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(80));
		executions.add(execution);
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.MARKET));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orderbook.setExecutions(executions);
		orderbookRepository.save(orderbook);
		orderbookService.executeOrders(executionWithChangedPrice, instrument);
	}

	@Test(expected = ExecutionQuantityIsMoreThanTheValidDemandException.class)
	public void whenOrderbookExistsAndIsClosedButExecutionQuantityIsMoreThanValidDemand() {
		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		execution3 = new ExecutionEntity(Long.valueOf(25), BigDecimal.valueOf(90));
		execution4 = new ExecutionEntity(Long.valueOf(35), BigDecimal.valueOf(90));
		executions.add(execution);
		executions.add(execution3);
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.MARKET));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orderbook.setExecutions(executions);
		orders.stream().forEach(record -> record.setStatus(OrderStatus.VALID));
		orderbookRepository.save(orderbook);
		orderbookService.executeOrders(execution4, instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndAfterFirstExecutionSecondExecutionForOrderbook() {

		// Mock Data added for first execution
		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(10), BigDecimal.valueOf(90));
		execution3 = new ExecutionEntity(Long.valueOf(10), BigDecimal.valueOf(90));
		executions.add(execution);
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(90), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orderbook.setExecutions(executions);
		orders.stream().forEach(record -> record.setStatus(OrderStatus.VALID));
		orderbook.getOrders().get(0).setExecutionQuantity(Long.valueOf(9));
		orderbook.getOrders().get(0).setExecutionPrice(BigDecimal.valueOf(90));
		orderbook.getOrders().get(1).setExecutionQuantity(Long.valueOf(1));
		orderbook.getOrders().get(1).setExecutionPrice(BigDecimal.valueOf(90));
		orderbook.setExecutions(executions);
		orderbookRepository.save(orderbook);

		// Second Execution with new execution
		OrderbookEntity orderbook = orderbookService.executeOrders(execution3, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), Long.valueOf(18));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), Long.valueOf(2));
		assertEquals(orderbook.getOrders().get(0).getExecutionPrice(), BigDecimal.valueOf(90));
		assertEquals(orderbook.getOrders().get(1).getExecutionPrice(), BigDecimal.valueOf(90));
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndFirstExecutionHasPositiveRemainderWhileDistributingForOrderbook() {
		List<OrderEntity> orders = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(50), BigDecimal.valueOf(90));
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orders.stream().forEach(record -> record.setStatus(OrderStatus.VALID));
		orderbookRepository.save(orderbook);

		OrderbookEntity orderbook = orderbookService.executeOrders(execution, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), Long.valueOf(16));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), Long.valueOf(17));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), Long.valueOf(17));
		assertEquals(orderbook.getOrders().get(0).getExecutionPrice(), BigDecimal.valueOf(90));
		assertEquals(orderbook.getOrders().get(1).getExecutionPrice(), BigDecimal.valueOf(90));
		assertEquals(orderbook.getOrders().get(1).getExecutionPrice(), BigDecimal.valueOf(90));
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndSecondExecutionHasNegativeRemainderWhileDistributingForOrderbook() {
		// Mock Data added for first execution
		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(20), BigDecimal.valueOf(90));
		execution3 = new ExecutionEntity(Long.valueOf(10), BigDecimal.valueOf(90));
		executions.add(execution);
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orderbook.setExecutions(executions);
		orders.stream().forEach(record -> record.setStatus(OrderStatus.VALID));
		orderbook.getOrders().get(0).setExecutionQuantity(Long.valueOf(7));
		orderbook.getOrders().get(0).setExecutionPrice(BigDecimal.valueOf(90));
		orderbook.getOrders().get(1).setExecutionQuantity(Long.valueOf(7));
		orderbook.getOrders().get(1).setExecutionPrice(BigDecimal.valueOf(90));
		orderbook.getOrders().get(2).setExecutionQuantity(Long.valueOf(6));
		orderbook.getOrders().get(2).setExecutionPrice(BigDecimal.valueOf(90));
		orderbook.setExecutions(executions);
		orderbookRepository.save(orderbook);

		// Second Execution with new execution
		OrderbookEntity orderbook = orderbookService.executeOrders(execution3, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), Long.valueOf(10));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), Long.valueOf(10));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), Long.valueOf(10));
		assertEquals(orderbook.getOrders().get(0).getExecutionPrice(), BigDecimal.valueOf(90));
		assertEquals(orderbook.getOrders().get(1).getExecutionPrice(), BigDecimal.valueOf(90));
		assertEquals(orderbook.getOrders().get(1).getExecutionPrice(), BigDecimal.valueOf(90));
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndTotalExecutionEqualsAccumulatedValidDemand() {
		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		execution3 = new ExecutionEntity(Long.valueOf(35), BigDecimal.valueOf(90));
		executions.add(execution);
		String instrument = "Fi1";
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.MARKET));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orderbook.setOrders(orders);
		orderbook.setExecutions(executions);
		orders.stream().forEach(record -> record.setStatus(OrderStatus.VALID));
		orderbook.getOrders().get(0).setExecutionQuantity(Long.valueOf(3));
		orderbook.getOrders().get(1).setExecutionQuantity(Long.valueOf(6));
		orderbook.getOrders().get(2).setExecutionQuantity(Long.valueOf(6));
		orderbook.setExecutions(executions);
		orderbookRepository.save(orderbook);

		OrderbookEntity orderbook = orderbookService.executeOrders(execution3, instrument);
		assertNotNull(orderbook);
		assertEquals(orderbook.getInstrument(), instrument);
		assertEquals(orderbook.getStatus(), Status.EXECUTE);
	}

	@Test(expected = OrderDoesNotExistForTheGivenOrderIdException.class)
	public void whenOrderDoesNotExistForOrderIdPrintStatistics2() {
		OrderEntity order = new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100));
		orderRepository.save(order);
		orderbookService.printSecondStatistics(7L);
	}

	@Test
	public void whenOrderExistForOrderIdPrintStatistics2() {
		OrderEntity testOrder = new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100));
		testOrder.setStatus(OrderStatus.VALID);
		testOrder.setExecutionQuantity(Long.valueOf(5));
		testOrder.setExecutionPrice(BigDecimal.valueOf(80));
		orderRepository.save(testOrder);
		long existingOrderId = orderRepository.findAll().get(0).getOrderId();
		OrderEntity order = orderbookService.printSecondStatistics(existingOrderId);
		assertNotNull(order);

		testOrder.setStatus(OrderStatus.INVALID);
		testOrder.setExecutionQuantity(Long.valueOf(5));
		testOrder.setExecutionPrice(BigDecimal.valueOf(80));
		orderRepository.save(testOrder);
		existingOrderId = orderRepository.findAll().get(0).getOrderId();
		order = orderbookService.printSecondStatistics(existingOrderId);
		assertNotNull(order);

	}

	@Test
	public void testForPrintStatistics1() {
		List<OrderEntity> orders = new ArrayList<>();
		List<ExecutionEntity> executions = new ArrayList<>();
		orderbook = new OrderbookEntity("Fi1", Status.CLOSE);
		execution = new ExecutionEntity(Long.valueOf(15), BigDecimal.valueOf(90));
		execution3 = new ExecutionEntity(Long.valueOf(35), BigDecimal.valueOf(90));
		executions.add(execution);
		executions.add(execution3);
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(110)));
		orders.add(new OrderEntity(Long.valueOf(30), today, OrderType.LIMIT, BigDecimal.valueOf(120)));
		orders.add(new OrderEntity(Long.valueOf(10), today, OrderType.LIMIT, BigDecimal.valueOf(100)));
		orders.add(new OrderEntity(Long.valueOf(90), today, OrderType.LIMIT, BigDecimal.valueOf(110)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.MARKET));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(80)));
		orders.add(new OrderEntity(Long.valueOf(30), today, OrderType.LIMIT, BigDecimal.valueOf(80)));
		orders.add(new OrderEntity(Long.valueOf(20), today, OrderType.LIMIT, BigDecimal.valueOf(70)));
		orders.add(new OrderEntity(Long.valueOf(5), today, OrderType.LIMIT, BigDecimal.valueOf(60)));
		orderbook.setOrders(orders);
		orderbook.setExecutions(executions);
		orders.stream().limit(6).forEach(record -> record.setStatus(OrderStatus.VALID));
		orders.stream().skip(6).forEach(record -> record.setStatus(OrderStatus.INVALID));
		orders.stream().limit(2).forEach(record -> record.setEntryDate(LocalDateTime.now()));
		orders.stream().limit(4).forEach(record -> record.setEntryDate(LocalDateTime.now().minusDays(1)));
		orders.stream().limit(4).forEach(record -> record.setEntryDate(LocalDateTime.now().minusDays(2)));
		orderbook.setExecutions(executions);
		orderbookRepository.save(orderbook);
		orderbookService.printFirstStatistics();
	}

}
