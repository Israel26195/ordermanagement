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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
	@Autowired
	public RestTemplate restTemplate;
	@Value("${userServiceUrl}")
	public String userServiceUrl;

	public Integer[] usingRewardPoints(Integer buyerId,Integer eligibleDiscount) {
        String getrewardUrl=userServiceUrl+"rewardPoint?buyerId="+buyerId;
        ResponseEntity<Integer> responseEntity = restTemplate.getForEntity(getrewardUrl, Integer.class);
        Integer reward=responseEntity.getBody();
        Integer discount=reward/4;
        if(discount>eligibleDiscount) {
            discount=eligibleDiscount;
            reward=reward-eligibleDiscount*4;
        }else {
            reward=0;
        }
        Integer [] valuesArray=new Integer[2];
        valuesArray[0]=discount;valuesArray[1]=reward;
        return valuesArray;
	}
	
//	This method inserts an order into the orderdetails table and also each and every record of products that are ordered into productsordered table.
	public void placeOrder(OrderBean order) {
		ArrayList<Product> productsReceived=(ArrayList<Product>) order.getOrderedProducts();
		
		BigDecimal amount=new BigDecimal(0);
		for (int j=0;j<productsReceived.size();j++) {
			Product product=productsReceived.get(j);
			amount=amount.add(product.getPrice().multiply(new BigDecimal(product.getQuantity())));
		}
		Integer eligibleDiscount=new Integer(amount.multiply(new BigDecimal(0.1)).intValue());
		
        // invoking usingRewardPoints method to get the discount
        Integer [] valuesArray=this.usingRewardPoints(order.getBuyerId(),eligibleDiscount);
        BigDecimal discount=new BigDecimal(valuesArray[0]);

		// Checking user is Priviledged or not
		String isPrivilegeUrl=userServiceUrl+"buyer/isPrivilege?buyerId="+order.getBuyerId();
		ResponseEntity<Boolean> responseEntity1 = restTemplate.getForEntity(isPrivilegeUrl, Boolean.class);
		Boolean isPrivileged=responseEntity1.getBody();
		
		// Based on isPrivileged, finding the shipping cost
		BigDecimal shippingCost=new BigDecimal(50);
		if(isPrivileged.equals(true)) {
			shippingCost=new BigDecimal(0);
		}
		amount=amount.subtract(discount);
		amount=amount.add(shippingCost);
		order.setAmount(amount);
		order.setDate(new Date());order.setStatus("ORDER PLACED");
		
		BeanUtils.copyProperties(order, orderEntity);
		orderrepo.save(orderEntity);
		
		// Adding all the individual products into the db				
		Integer orderId=orderEntity.getOrderId();
		productsReceived.forEach((Product prod)->{
			prod.setOrderId(orderId);
			prod.setStatus("ORDER PLACED");
			BeanUtils.copyProperties(prod, productsOrdered);
			orderProdsRepo.save(productsOrdered);
		});
		
		// Calculating and Updating the reward points in the user service
		Integer newRewardPoints = new Integer(amount.intValue()/100)+valuesArray[1]; // 100 ruppees equals 1 point
		String updateRewardPointsUrl = userServiceUrl +"rewardPoint/update?buyerId="+order.getBuyerId()+"&point="+newRewardPoints;
		restTemplate.put(updateRewardPointsUrl,newRewardPoints,Integer.class);
	}
	
	public String reOrder(OrderBean order) {
		try {
		ArrayList<Product> orderedProducts=new ArrayList<Product>();
		Integer orderId=order.getOrderId();
		// Finding all the products for that particular Order
		orderProdsRepo.findAll().forEach((product)->{
			if(product.getOrderId().equals(orderId)) {
				Product prodBean=new Product();
				BeanUtils.copyProperties(product, prodBean);
				orderedProducts.add(prodBean);
			}
		});
		// Setting some basic values before placing the order
		order.setOrderedProducts(orderedProducts);
		order.setAmount(new BigDecimal(0));
		order.setOrderId(null);
		this.placeOrder(order);
		}catch(Exception e){
			e.printStackTrace();
			return "Error in placing the order! Contact your Admin";
		}
		return "Reorder is successful";
	}
	
	public ArrayList <OrderBean> getAllOrders(Integer buyerId) {
        Iterable<OrderEntity> ordersEntities=orderrepo.findAll();
        ArrayList <OrderBean> orders= new ArrayList<>();
        List<ProductsOrdered> allOrderedProducts=(List<ProductsOrdered>) orderProdsRepo.findAll();
        for(OrderEntity oe: ordersEntities){
			// It gets all the orders and filters based on BuyerId
            if (oe.getBuyerId().equals(buyerId)) {
				ArrayList<Product> orderedProducts=new ArrayList<>();
                for(int i=0;i<allOrderedProducts.size();i++){
                    ProductsOrdered orderprod=allOrderedProducts.get(i);
					// Finding the ordered Products for each and every order
                    if(oe.getOrderId().equals(orderprod.getOrderId())) {
                        Product prod=new Product();
                        BeanUtils.copyProperties(orderprod, prod);
                        orderedProducts.add(prod);
                    }
                }
                OrderBean ob = new OrderBean();
                BeanUtils.copyProperties(oe, ob);
                ob.setOrderedProducts(orderedProducts);
                orders.add(ob);
                }
        }
        return orders;
    }	
	// Identifies all the products ordered to a particular seller
	public ArrayList<ProductsOrdered> getSellerOrders(Integer sellerId) {
		ArrayList <ProductsOrdered> orderedProducts = new ArrayList<>();
		orderProdsRepo.findAll().forEach((ProductsOrdered ordProd)->{
			if(ordProd.getSellerid().equals(sellerId)) {
				orderedProducts.add(ordProd);
			}
		});
		
		return orderedProducts;
	}
	
	public String updateStatus(Integer orderId,Integer prodId,String status) {
		Boolean flag=false;
		try {
		List<ProductsOrdered> products=(List<ProductsOrdered>) orderProdsRepo.findAll();
		for(int i=0;i<products.size();i++){
			ProductsOrdered product=products.get(i);
			// Checking the orderId and ProdId before updating
			if(product.getOrderId().equals(orderId) && product.getProdId().equals(prodId)) {
				BeanUtils.copyProperties(product,productsOrdered);
				orderProdsRepo.delete(product);
				productsOrdered.setStatus(status);
				orderProdsRepo.save(productsOrdered);
				flag=true;
			}
		}
		}catch(Exception e){
			e.printStackTrace();
			return "Error in updating the order! Contact your Admin";
		}
		if(flag){
			return "Order status updated successfully";
		}else{
			return "Updation is not successful. Check for issues";
		}
	}
	
	public String cancelAnOrder(Integer orderId) {
		Boolean flag=false;
		Boolean flagOne=false;
		try{
			Iterable<OrderEntity> ordersEntities=orderrepo.findAll();
			// Deleting the order
			for(OrderEntity order: ordersEntities){
				if(order.getOrderId().equals(orderId)) {
					orderrepo.delete(order);					
					flagOne=true;
				}
			}
			List<ProductsOrdered> products=(List<ProductsOrdered>) orderProdsRepo.findAll();
			// Deleting all the product ordered in that order
			for(int i=0;i<products.size();i++){
				ProductsOrdered product=products.get(i);
				if(product.getOrderId().equals(orderId)) {
						orderProdsRepo.delete(product);
						flag=true;
				}
			}			
		}catch(Exception e){
			e.printStackTrace();
			return "Error in canceling the order! Contact your Admin";
		}
		if(flag&flagOne){
			return "Order Canceled successfully.";
		}else{
			return "Cancellation is not successful. Either order is not present or that order doesnt have any products";
		}
		
	}
}