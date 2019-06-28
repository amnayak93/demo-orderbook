package com.cs.Orderbook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import com.cs.Orderbook.Entity.ExecutionEntity;
import com.cs.Orderbook.Entity.OrderEntity;
import com.cs.Orderbook.Entity.OrderbookEntity;
import com.cs.Orderbook.Entity.Status;
import com.cs.Orderbook.Exception.OrderbookIsClosedException;
import com.cs.Orderbook.Exception.OrderbookIsNotClosedException;
import com.cs.Orderbook.Exception.OrderbookIsNotOpenException;
import com.cs.Orderbook.Exception.OrderbookNotFoundException;
import com.cs.Orderbook.repository.OrderbookRepository;
import com.cs.Orderbook.service.OrderbookService;
import com.cs.Orderbook.service.impl.OrderbookServiceImpl;

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

	@MockBean
	private OrderbookRepository orderbookRepository;

	private OrderbookEntity orderbook1;
	private List<OrderEntity> orders = new ArrayList<>();
	private List<ExecutionEntity> executions = new ArrayList<>();

	private OrderbookEntity orderbook2;
	private List<OrderEntity> orders2 = new ArrayList<>();
	private LocalDateTime today = LocalDateTime.now();

	private OrderbookEntity orderbook3;
	private List<OrderEntity> orders3 = new ArrayList<>();
	private List<OrderEntity> orders4 = new ArrayList<>();

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		orders.add(new OrderEntity(1L, orderbook1, BigDecimal.valueOf(100), today, BigDecimal.valueOf(50), "Market"));
		orders.add(new OrderEntity(2L, orderbook1, BigDecimal.valueOf(50), today, BigDecimal.valueOf(75), "Limit"));

		orders2.add(new OrderEntity(3L, orderbook2, BigDecimal.valueOf(100), today, BigDecimal.valueOf(50), "Limit"));
		orders2.add(new OrderEntity(4L, orderbook2, BigDecimal.valueOf(50), today, BigDecimal.valueOf(75), "Limit"));

		orders3.add(new OrderEntity(5L, orderbook2, BigDecimal.valueOf(50), today, BigDecimal.valueOf(10), "Market"));
		orders3.add(new OrderEntity(6L, orderbook2, BigDecimal.valueOf(70), today, BigDecimal.valueOf(25), "Limit"));

		orders4.add(new OrderEntity(5L, orderbook3, BigDecimal.valueOf(50), today, BigDecimal.valueOf(10), "Market"));

		orders2.addAll(orders3);

		orderbook1 = new OrderbookEntity("Fi1", Status.CLOSE, orders, executions, BigInteger.valueOf(0),
				BigInteger.valueOf(0), true);
		orderbook2 = new OrderbookEntity("Fi2", Status.OPEN, orders2, executions, BigInteger.valueOf(0),
				BigInteger.valueOf(0), true);
		orderbook3 = new OrderbookEntity("Fi3", Status.EXECUTE, orders4, executions, BigInteger.valueOf(0),
				BigInteger.valueOf(0), true);

		when(orderbookRepository.findById(orderbook1.getInstrument())).thenReturn(Optional.of(orderbook1));
		when(orderbookRepository.findById(orderbook2.getInstrument())).thenReturn(Optional.of(orderbook2));
		when(orderbookRepository.findById(orderbook3.getInstrument())).thenReturn(Optional.of(orderbook3));
	}

	/*
	 * Instrument Fi3 does not exist so it throws OrderbookNotFoundException
	 */
	@Test(expected = OrderbookNotFoundException.class)
	public void whenInstrumentDoesNotExistShowException() {
		String instrument = "Fi4";
		orderbookService.openOrderbook(instrument);
		orderbookService.closeOrderbook(instrument);
		orderbookService.addOrders(orders3, instrument);
	}

	/*
	 * Instrument Fi2 exists but the book status is already open so it cannot be
	 * opened and an exception will be thrown.
	 */
	@Test(expected = OrderbookIsNotClosedException.class)
	public void whenInstrumentExistsToOpenOrderBookButOrderBookIsAlreadyOpen() {
		String instrument = "Fi2";
		orderbookService.openOrderbook(instrument);
	}

	/*
	 * Instrument Fi1 exists but the book status is already close so it cannot
	 * be closed and an exception will be thrown.
	 */
	@Test(expected = OrderbookIsNotOpenException.class)
	public void whenInstrumentExistsToCloseOrderBookButOrderBookIsAlreadyClose() {
		String instrument = "Fi1";
		orderbookService.closeOrderbook(instrument);
	}

	@Test
	public void whenInstrumentExistsOpenAndGetOrderBook() {
		String instrument = "Fi1";
		OrderbookEntity orderbook = orderbookService.openOrderbook(instrument);
		assertNotNull(orderbook);
		assertEquals(orderbook, orderbook1);
		assertEquals(orderbook.getInstrument(), instrument);
		assertEquals(orderbook.getStatus(), Status.CLOSE);
		assertEquals(orderbook.getOrders(), orders);
		assertEquals(orderbook.getTotExecutionOrders(), BigInteger.valueOf(0));
		assertEquals(orderbook.getValidOrders(), BigInteger.valueOf(0));
	}

	@Test
	public void whenInstrumentExistsAndIsOpenAddOrderSuccess() {
		String instrument = "Fi2";
		OrderbookEntity orderbook = orderbookService.addOrders(orders3, instrument);
		assertNotNull(orderbook);
		assertEquals(orderbook, orderbook2);
		assertEquals(orderbook.getInstrument(), instrument);
		assertEquals(orderbook.getStatus(), Status.OPEN);
		assertEquals(orderbook.getOrders(), orders2);
		assertEquals(orderbook.getTotExecutionOrders(), BigInteger.valueOf(0));
		assertEquals(orderbook.getValidOrders(), BigInteger.valueOf(0));
	}

	/*
	 * Orderbook exists for the instrument but it is closed so orders cannot be
	 * added and exception will be thrown
	 */
	@Test(expected = OrderbookIsClosedException.class)
	public void whenInstrumentExistsButBookIsClosedAddOrderFailure() {
		String instrument = "Fi1";
		orderbookService.addOrders(orders3, instrument);
	}

	/*
	 * Orderbook exists for the instrument but it is not open so orders cannot
	 * be added and exception will be thrown
	 */
	@Test(expected = OrderbookIsNotOpenException.class)
	public void whenInstrumentExistsButBookIsNotOpenAddOrderFailure() {
		String instrument = "Fi3";
		orderbookService.addOrders(orders3, instrument);
	}

}
