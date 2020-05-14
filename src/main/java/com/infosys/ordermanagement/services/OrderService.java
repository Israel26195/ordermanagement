package com.infosys.ordermanagement.services;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.infosys.ordermanagement.Beans.OrderBean;
import com.infosys.ordermanagement.Beans.Product;
import com.infosys.ordermanagement.controller.OrderController;
import com.infosys.ordermanagement.entities.OrderEntity;
import com.infosys.ordermanagement.entities.ProductsOrdered;
import com.infosys.ordermanagement.repository.OrderRepository;
import com.infosys.ordermanagement.repository.ProductsOrderedRepository;

@Service
public class OrderService {
	@Autowired
	private OrderRepository orderrepo;
	@Autowired
	private OrderEntity orderEntity;
	@Autowired
	private ProductsOrderedRepository orderProdsRepo;
	@Autowired
	private ProductsOrdered productsOrdered;

	
//	This method inserts an order into the orderdetails table and also each and every record of products that are ordered into productsordered table.
	public void placeOrder(OrderBean order) {
		ArrayList<Product> productsReceived=(ArrayList<Product>) order.getOrderedProducts();
//		System.out.println(productsReceived.size());
		ArrayList<Integer> prodIds= new ArrayList();
		productsReceived.forEach((Product product)->{prodIds.add(product.getProdId());});
		
//		ArrayList<Product> productsReceived=new ArrayList();
//		productsReceived=(ArrayList<Product>) order.getOrderedProducts();
		BigDecimal amount=new BigDecimal(0);
		for (int j=0;j<productsReceived.size();j++) {
			Product product=productsReceived.get(j);
			amount=amount.add(product.getPrice().multiply(new BigDecimal(product.getQuantity())));
		}
		
		
//		for (int j=0;j<productsReceived.size();j++) {	
//			Product product=productsReceived.get(j);
//			for(int i=0;i<products.size();i++) {
//				Product prod=products.get(i);
//				if(prod.getProdId().equals(product.getProdId())) {
//					prod.setPrice(new BigDecimal(80));
//					amount=amount.add(prod.getPrice().multiply(new BigDecimal(prod.getQuantity())));
//					products.get(i).setSellerId(123);
//				}
//			}
//			
//		};
		order.setDate(new Date());	order.setAmount(amount);	order.setStatus("PROCESSING");
		
		BeanUtils.copyProperties(order, orderEntity);
		orderrepo.save(orderEntity);
		
		Integer orderId=orderEntity.getOrderId();
		productsReceived.forEach((Product prod)->{
//			System.out.println(prod);
			prod.setOrderId(orderId);
			prod.setStatus("PROCESSING");
			BeanUtils.copyProperties(prod, productsOrdered);
			orderProdsRepo.save(productsOrdered);
		});
	}
	
	public ArrayList <OrderBean> getAllOrders(Integer buyerId) {
//		List<OrderEntity> products=(List<ProductsOrdered>) ;
		Iterable<OrderEntity> ordersEntities=orderrepo.findAll();
		ArrayList <OrderBean> orders= new ArrayList<>();
		for(OrderEntity s: ordersEntities){
			if (s.getBuyerId().equals(buyerId)) {
//				System.out.println("Helooooo");
				OrderBean ob = new OrderBean();
				BeanUtils.copyProperties(s, ob);
				orders.add(ob);
				}
			
		}
		return orders;
	}	
	
	public ArrayList<ProductsOrdered> getSellerOrders(Integer sellerId) {
		ArrayList <ProductsOrdered> orderedProducts = new ArrayList<>();
		orderProdsRepo.findAll().forEach((ProductsOrdered ordProd)->{
			if(ordProd.getSellerid().equals(sellerId)) {
//				Product product=new Product();
//				BeanUtils.copyProperties(ordProd, product);
				orderedProducts.add(ordProd);
			}
			
		});
		
		return orderedProducts;
	}
	
	public void updateStatus(Integer orderId,Integer prodId,String status) {
		List<ProductsOrdered> products=(List<ProductsOrdered>) orderProdsRepo.findAll();
		products.forEach((product)->{
			if(product.getOrderId().equals(orderId) && product.getProdId().equals(prodId)) {
				BeanUtils.copyProperties(product,productsOrdered);
				orderProdsRepo.delete(product);
				productsOrdered.setStatus(status);
				orderProdsRepo.save(productsOrdered);
			}
		});
		
	}
	
	public void cancelAnOrder(Integer orderId) {
		List<ProductsOrdered> products=(List<ProductsOrdered>) orderProdsRepo.findAll();
		products.forEach((product)->{
			if(product.getOrderId().equals(orderId)) {
				orderProdsRepo.delete(product);
			}
		});
		orderrepo.findAll().forEach((order)->{
			if(order.getOrderId().equals(orderId)) {
				orderrepo.delete(order);
			}
		});
		
	}
}
