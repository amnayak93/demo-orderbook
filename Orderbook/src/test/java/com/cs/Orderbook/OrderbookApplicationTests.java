package com.cs.Orderbook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.math.BigInteger;
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
import com.cs.Orderbook.service.impl.OrderbookServiceImpl;
import com.cs.Orderbook.utils.StaticUtils;

@RunWith(SpringRunner.class)
@DataJpaTest
public class OrderbookApplicationTests {

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

	private OrderbookEntity orderbook1;

	private OrderbookEntity orderbook2;

	private OrderbookEntity orderbook3;

	private List<OrderEntity> orders1 = new ArrayList<>();

	private List<OrderEntity> orders2 = new ArrayList<>();

	private List<OrderEntity> orders3 = new ArrayList<>();

	private ExecutionEntity execution1;

	private ExecutionEntity execution2;

	private ExecutionEntity execution3;

	private ExecutionEntity execution4;

	private List<ExecutionEntity> executions1 = new ArrayList<>();

	private LocalDateTime today = LocalDateTime.now();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		orderbook1 = new OrderbookEntity("Fi1", Status.OPEN);
		orderbookRepository.save(orderbook1);
		orderbook2 = new OrderbookEntity("Fi2", Status.CLOSE);
		orderbookRepository.save(orderbook2);
		orderbook3 = new OrderbookEntity("Fi3", Status.EXECUTE);
		orderbookRepository.save(orderbook3);
		orders1.add(new OrderEntity(BigInteger.valueOf(40), today, StaticUtils.LIMIT, BigDecimal.valueOf(80)));
		orders1.add(new OrderEntity(BigInteger.valueOf(20), today, StaticUtils.MARKET));

		orders2.add(new OrderEntity(BigInteger.valueOf(10), today, StaticUtils.LIMIT, BigDecimal.valueOf(100)));
		orders2.add(new OrderEntity(BigInteger.valueOf(20), today, StaticUtils.MARKET, BigDecimal.valueOf(100)));
		orders2.add(new OrderEntity(BigInteger.valueOf(20), today, StaticUtils.LIMIT));

		orders3.add(new OrderEntity(BigInteger.valueOf(10), today, StaticUtils.LIMIT, BigDecimal.valueOf(80)));
		orders3.add(new OrderEntity(BigInteger.valueOf(10), today, StaticUtils.LIMIT));

		execution1 = new ExecutionEntity(BigInteger.valueOf(15), BigDecimal.valueOf(90));
		execution2 = new ExecutionEntity(BigInteger.valueOf(20), BigDecimal.valueOf(100));
		execution3 = new ExecutionEntity(BigInteger.valueOf(15), BigDecimal.valueOf(90));
		execution4 = new ExecutionEntity(BigInteger.valueOf(35), BigDecimal.valueOf(90));

		executions1.add(execution1);

	}

	/*
	 * Instrument Fi1 exists so it throws OrderbookFoundException when you try
	 * to open the Orderbook
	 */
	@Test(expected = OrderbookFoundException.class)
	public void whenOrderbookExistsButOpenOrderBook() {
		String instrument = "Fi1";
		orderbookService.openOrderbook(instrument);
	}

	@Test
	public void whenOrderbookDoesNotExistOpenAndGetOrderbook() {
		String instrument = "Fi4";
		OrderbookEntity orderbook = orderbookService.openOrderbook(instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(Status.OPEN, orderbook.getStatus());
	}

	@Test(expected = OrderbookNotFoundException.class)
	public void whenOrderbookDoesNotExistButCloseOrderbook() {
		String instrument = "Fi4";
		orderbookService.closeOrderbook(instrument);
	}

	@Test(expected = OrderbookIsClosedException.class)
	public void whenOrderbookExistsAndIsClosedButCloseOrderbook() {
		String instrument = "Fi2";
		orderbookService.closeOrderbook(instrument);
	}

	@Test(expected = OrderbookIsExecutedException.class)
	public void whenOrderbookExistsAndIsExecutedButCloseOrderbook() {
		String instrument = "Fi3";
		orderbookService.closeOrderbook(instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsOpenCloseOrderbook() {
		String instrument = "Fi1";
		OrderbookEntity orderbook = orderbookService.closeOrderbook(instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(Status.CLOSE, orderbook.getStatus());
	}

	@Test(expected = OrderbookNotFoundException.class)
	public void whenOrderbookDoesNotExistButAddOrdersToOrderbook() {
		String instrument = "Fi4";
		orderbookService.addOrders(orders1, instrument);
	}

	@Test(expected = OrderbookIsClosedException.class)
	public void whenOrderbookExistsAndIsClosedButAddOrdersToOrderbook() {
		String instrument = "Fi2";
		orderbookService.addOrders(orders1, instrument);
	}

	@Test(expected = OrderbookIsExecutedException.class)
	public void whenOrderbookExistsAndIsExecutedButAddOrdersToOrderbook() {
		String instrument = "Fi3";
		orderbookService.addOrders(orders1, instrument);
	}

	@Test(expected = MarketOrderHasLimitPriceException.class)
	public void whenOrderbookExistsAndIsOpenButAddOrdersHavingMarketOrderAndLimitPrice() {
		String instrument = "Fi1";
		orderbookService.addOrders(orders2, instrument);
	}

	@Test(expected = LimitOrderDoesNotHaveLimitPriceException.class)
	public void whenOrderbookExistsAndIsOpenButAddOrdersHavingLimitOrderDoesNotHaveLimitPrice() {
		String instrument = "Fi1";
		orderbookService.addOrders(orders3, instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsOpenAndOrdersHavingMarketOrderThatDoesNotHaveLimitPrice() {
		orderbook1.setOrders(orders1); // Mocked orderbook with added orders
		String instrument = "Fi1";
		OrderbookEntity orderbook = orderbookService.addOrders(orders1, instrument);
		assertNotNull(orderbook);
		assertEquals(orderbook1.getInstrument(), orderbook.getInstrument());
		assertEquals(orderbook1.getStatus(), orderbook.getStatus());
		assertEquals(orderbook1.getOrders(), orderbook.getOrders());
	}

	@Test(expected = OrderbookNotFoundException.class)
	public void whenOrderbookDoesNotExistButExecuteOrderbook() {
		String instrument = "Fi4";
		orderbookService.executeOrders(execution1, instrument);
	}

	@Test(expected = OrderbookIsOpenException.class)
	public void whenOrderbookExistsAndIsOpenExecuteOrderbook() {
		String instrument = "Fi1";
		orderbookService.executeOrders(execution1, instrument);
	}

	@Test(expected = OrderbookIsAlreadyExecutedException.class)
	public void whenOrderbookExistsAndIsExecutedExecuteOrderbook() {
		String instrument = "Fi3";
		orderbookService.executeOrders(execution1, instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndFirstExecutionAndAllValidOrdersForOrderbook() {
		String instrument = "Fi2";
		orderbook2.setOrders(orders2);
		orderbook2.getOrders().get(2).setPrice(BigDecimal.valueOf(100));
		orderbookRepository.save(orderbook2);
		OrderbookEntity orderbook = orderbookService.executeOrders(execution1, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), BigInteger.valueOf(3));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), BigInteger.valueOf(6));
		assertEquals(orderbook.getOrders().get(2).getExecutionQuantity(), BigInteger.valueOf(6));
		assertEquals(
				orderbook.getOrders().stream().filter(record -> record.getStatus().equals(OrderStatus.VALID)).count(),
				3l);
		assertEquals(orderbook.getExecutions(), executions1);
		assertEquals(orderbook.getStatus(), Status.CLOSE);
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndFirstExecutionAndOneInvalidOrderForOrderbook() {
		String instrument = "Fi2";
		orderbook2.setOrders(orders2);
		orderbook2.getOrders().get(2).setPrice(BigDecimal.valueOf(80));
		orderbookRepository.save(orderbook2);
		OrderbookEntity orderbook = orderbookService.executeOrders(execution1, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(
				orderbook.getOrders().stream().filter(record -> record.getStatus().equals(OrderStatus.VALID)).count(),
				2l);
		assertEquals(
				orderbook.getOrders().stream().filter(record -> record.getStatus().equals(OrderStatus.INVALID)).count(),
				1l);
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), BigInteger.valueOf(5));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), BigInteger.valueOf(10));
		assertEquals(orderbook.getExecutions(), executions1);
		assertEquals(orderbook.getStatus(), Status.CLOSE);
	}

	@Test(expected = ExecutionPriceShouldNotChangeException.class)
	public void whenOrderbookExistsAndIsClosedButExecutionPriceIsChanged() {
		String instrument = "Fi2";
		orderbook2.setOrders(orders2);
		orderbook2.getOrders().get(2).setPrice(BigDecimal.valueOf(100));
		orderbook2.setExecutions(executions1);
		orderbookRepository.save(orderbook2);
		orderbookService.executeOrders(execution2, instrument);
	}

	@Test(expected = ExecutionQuantityIsMoreThanTheValidDemandException.class)
	public void whenOrderbookExistsAndIsClosedButExecutionQuantityIsMoreThanValidDemand() {
		String instrument = "Fi2";
		executions1.add(execution3);
		orders2.stream().forEach(record -> record.setStatus(OrderStatus.VALID));
		orderbook2.setOrders(orders2);
		orderbook2.getOrders().get(2).setPrice(BigDecimal.valueOf(100));
		orderbook2.setExecutions(executions1);
		orderbookRepository.save(orderbook2);
		orderbookService.executeOrders(execution4, instrument);
	}

	@Test
	public void whenOrderbookExistsAndIsClosedAndAfterFirstExecutionSecondExecutionForOrderbook() {

		/* Mock Data added for first execution */
		String instrument = "Fi2";
		executions1.add(execution1);
		orderbook2.setOrders(orders2);
		orderbook2.getOrders().get(2).setPrice(BigDecimal.valueOf(100));
		orders2.stream().forEach(record -> record.setStatus(OrderStatus.VALID));
		orderbook2.getOrders().get(0).setExecutionQuantity(BigInteger.valueOf(3));
		orderbook2.getOrders().get(1).setExecutionQuantity(BigInteger.valueOf(6));
		orderbook2.getOrders().get(2).setExecutionQuantity(BigInteger.valueOf(6));
		orderbookRepository.save(orderbook2);

		/* Second Execution with new execution */
		OrderbookEntity orderbook = orderbookService.executeOrders(execution3, instrument);
		assertNotNull(orderbook);
		assertEquals(instrument, orderbook.getInstrument());
		assertEquals(orderbook.getOrders().get(0).getExecutionQuantity(), BigInteger.valueOf(6));
		assertEquals(orderbook.getOrders().get(1).getExecutionQuantity(), BigInteger.valueOf(12));
		assertEquals(orderbook.getOrders().get(2).getExecutionQuantity(), BigInteger.valueOf(12));

	}
}
