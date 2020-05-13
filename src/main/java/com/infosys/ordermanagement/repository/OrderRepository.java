package com.infosys.ordermanagement.repository;

import org.springframework.data.repository.CrudRepository;

import com.infosys.ordermanagement.entities.OrderEntity;

public interface OrderRepository extends CrudRepository<OrderEntity, Integer>{

}
