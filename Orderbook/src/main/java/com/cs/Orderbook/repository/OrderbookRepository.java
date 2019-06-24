package com.cs.Orderbook.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cs.Orderbook.Entity.OrderbookEntity;

@Repository
public interface OrderbookRepository extends JpaRepository<OrderbookEntity, String>{ 

}
