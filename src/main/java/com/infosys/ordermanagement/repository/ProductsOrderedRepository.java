package com.infosys.ordermanagement.repository;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;

import com.infosys.ordermanagement.entities.ProductsOrdered;

public interface ProductsOrderedRepository  extends CrudRepository<ProductsOrdered, Integer>{

}
