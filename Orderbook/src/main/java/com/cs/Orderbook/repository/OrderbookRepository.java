package com.cs.Orderbook.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cs.Orderbook.Entity.OrderbookEntity;

@Repository
public interface OrderbookRepository extends JpaRepository<OrderbookEntity, Long> {
	@Query("Select orderbook from OrderbookEntity orderbook where orderbook.instrument = :instrument")
	public Optional<OrderbookEntity> findbyInstrument(@Param("instrument") String instrument);
}
