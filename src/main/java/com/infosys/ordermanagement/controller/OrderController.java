package com.infosys.ordermanagement.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.infosys.ordermanagement.Beans.OrderBean;
import com.infosys.ordermanagement.Beans.Product;
import com.infosys.ordermanagement.Beans.ProductId;
import com.infosys.ordermanagement.model.OrderModel;
import com.infosys.ordermanagement.entities.OrderEntity;
import com.infosys.ordermanagement.entities.ProductsOrdered;
import com.infosys.ordermanagement.services.OrderService;

@RestController
public class OrderController {
	
	@Autowired
	private OrderService orderservice;
	@Autowired
	public RestTemplate restTemplate;
	
	
	@RequestMapping("/orders")
	public Iterable<OrderEntity> getOrders() {
		return orderservice.getAllOrders();	
	}
	
	@PostMapping("/orders/placeOrder")
	// I need buyer Id
	public void placeOrder(@RequestBody OrderBean order) {	
//		ArrayList<Product> products= order.getOrderedProducts();
//		MediaType contentType = responseEntity.getHeaders().getContentType();
//		HttpStatus statusCode = responseEntity.getStatusCode();
		ResponseEntity<Product[]> responseEntity = restTemplate.getForEntity("http://vfpmys-37:8080/api/cart/checkout?buyerId=12", Product[].class);
		Product[] objects = responseEntity.getBody();		
		List<Integer> prodIds= new ArrayList<Integer>();
		for(int i=0;i<objects.length;i++) {
			
			prodIds.add(objects[i].getProdId());
		}
		ProductId prodId1=new ProductId();
		prodId1.setProdId(prodIds);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
//		MultiValueMap<String, ProductId> map= new LinkedMultiValueMap<String, List<Integer>>();
//		map.add("prodId", prodIds);
		HttpEntity<ProductId> request = new HttpEntity<ProductId>(prodId1, headers);
		ResponseEntity<Product[]> products1 = restTemplate.postForEntity("http://vfpmys-56:8080/api/products/",request, Product[].class);
		Product[] products12 = products1.getBody();
		ArrayList<Product> products=new ArrayList<Product>();
		for(int i=0;i<products12.length;i++) {
			products.add(products12[i]);
		}
		System.out.println(products);
		products.forEach(element->{
			for(int j=0;j<objects.length;j++) {
				if(objects[j].getProdId().equals(element.getProdId())) {
					element.setQuantity(objects[j].getQuantity());
				}
			}
		});
		order.setOrderedProducts(products);
		order.setBuyerId(12);
		orderservice.placeOrder(order,products);
	}
	
	@RequestMapping(method=RequestMethod.PUT,value="/orders/seller/status")
	public void updateStatus (@RequestBody Product product) {
		Integer orderId=product.getOrderId();
		Integer prodId=product.getProdId();
		String status= product.getStatus();
		orderservice.updateStatus(orderId,prodId,status);
	}
	
//	With the seller id, this method will return all the orders that are present for a particular seller
	@RequestMapping("/orders/seller/{sellerId}")
	public ArrayList<ProductsOrdered> getSellerOrders(@PathVariable("sellerId") Integer sellerId) {
		return orderservice.getSellerOrders(sellerId);	
	}
	
	@RequestMapping("/orders/cancel/{orderId}")
	public void cancelOrder(@PathVariable("orderId") Integer orderId ) {
		orderservice.cancelAnOrder(orderId);
	}
	
}


//HttpHeaders headers = new HttpHeaders();
//headers.setContentType(MediaType.APPLICATION_JSON);
//
////MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
//ArrayList<Integer> prodIds= new ArrayList();
//
//map.add("email", "first.last@example.com");
//
//HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
//
//ResponseEntity<String> response = restTemplate.postForEntity( url, request , String.class );