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

    public Integer usingRewardPoints(Integer buyerId) {
        String getrewardUrl = userServiceUrl + "rewardPoint?buyerId=" + buyerId;
        ResponseEntity<Integer> responseEntity = restTemplate.getForEntity(getrewardUrl, Integer.class);
        Integer reward = responseEntity.getBody();
        Integer discount = reward / 4;
        return discount;
    }

    //	This method inserts an order into the orderdetails table and also each and every record of products that are ordered into productsordered table.
    public void placeOrder(OrderBean order) {
        ArrayList<Product> productsReceived = (ArrayList<Product>) order.getOrderedProducts();

        BigDecimal amount = new BigDecimal(0);
        for (int j = 0; j < productsReceived.size(); j++) {
            Product product = productsReceived.get(j);
            amount = amount.add(product.getPrice().multiply(new BigDecimal(product.getQuantity())));
        }
        // invoking usingRewardPoints method to get the discount
        BigDecimal discount = new BigDecimal(this.usingRewardPoints(order.getBuyerId()));

        // Checking user is Priviledged or not
        String isPrivilegeUrl = userServiceUrl + "buyer/isPrivilege?buyerId=" + order.getBuyerId();
        ResponseEntity<Boolean> responseEntity1 = restTemplate.getForEntity(isPrivilegeUrl, Boolean.class);
        Boolean isPrivileged = responseEntity1.getBody();

        // Based on isPrivileged, finding the shipping cost
        BigDecimal shippingCost = new BigDecimal(50);
        if (isPrivileged.equals(true)) {
            shippingCost = new BigDecimal(0);
        }

        amount = amount.subtract(discount);
        amount = amount.add(shippingCost);
        order.setAmount(amount);
        order.setDate(new Date());
        order.setStatus("ORDER PLACED");

        BeanUtils.copyProperties(order, orderEntity);
        orderrepo.save(orderEntity);

        // Adding all the individual products into the db
        Integer orderId = orderEntity.getOrderId();
        productsReceived.forEach((Product prod) -> {
            prod.setOrderId(orderId);
            prod.setStatus("ORDER PLACED");
            BeanUtils.copyProperties(prod, productsOrdered);
            orderProdsRepo.save(productsOrdered);
        });

        // Calculating and Updating the reward points in the user service
        Integer newRewardPoints = new Integer(amount.intValue()); // 100 ruppees equals 1 point
        String updateRewardPointsUrl = userServiceUrl + "rewardPoint/update?buyerId=" + order.getBuyerId() + "&point=" + newRewardPoints;
        restTemplate.put(updateRewardPointsUrl, newRewardPoints, Integer.class);
    }

    public void reOrder(OrderBean order) {
        ArrayList<Product> orderedProducts = new ArrayList<Product>();
        Integer orderId = order.getOrderId();
        orderProdsRepo.findAll().forEach((product) -> {
            if (product.getOrderId().equals(orderId)) {
                Product prodBean = new Product();
                BeanUtils.copyProperties(product, prodBean);
                orderedProducts.add(prodBean);
            }
        });
        order.setOrderedProducts(orderedProducts);
        order.setAmount(new BigDecimal(0));
        order.setOrderId(null);
        this.placeOrder(order);
    }

    public ArrayList<OrderBean> getAllOrders(Integer buyerId) {
        Iterable<OrderEntity> ordersEntities = orderrepo.findAll();
        ArrayList<OrderBean> orders = new ArrayList<>();
        for (OrderEntity oe : ordersEntities) {
            if (oe.getBuyerId().equals(buyerId)) {
                OrderBean ob = new OrderBean();
                BeanUtils.copyProperties(oe, ob);
                orders.add(ob);
            }
        }
        return orders;
    }

    public ArrayList<ProductsOrdered> getSellerOrders(Integer sellerId) {
        ArrayList<ProductsOrdered> orderedProducts = new ArrayList<>();
        orderProdsRepo.findAll().forEach((ProductsOrdered ordProd) -> {
            if (ordProd.getSellerid().equals(sellerId)) {
                orderedProducts.add(ordProd);
            }
        });

        return orderedProducts;
    }

    public void updateStatus(Integer orderId, Integer prodId, String status) {
        List<ProductsOrdered> products = (List<ProductsOrdered>) orderProdsRepo.findAll();
        products.forEach((product) -> {
            if (product.getOrderId().equals(orderId) && product.getProdId().equals(prodId)) {
                BeanUtils.copyProperties(product, productsOrdered);
                orderProdsRepo.delete(product);
                productsOrdered.setStatus(status);
                orderProdsRepo.save(productsOrdered);
            }
        });

    }

    public void cancelAnOrder(Integer orderId) {
        List<ProductsOrdered> products = (List<ProductsOrdered>) orderProdsRepo.findAll();
        products.forEach((product) -> {
            if (product.getOrderId().equals(orderId)) {
                orderProdsRepo.delete(product);
            }
        });
        orderrepo.findAll().forEach((order) -> {
            if (order.getOrderId().equals(orderId)) {
                orderrepo.delete(order);
            }
        });

    }
}
