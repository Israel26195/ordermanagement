package com.infosys.ordermanagement.model;

import java.math.BigDecimal;
import java.util.Date;

public class OrderModel {
	private Integer orderId;
	private Integer buyerId;
	private BigDecimal amount;
	private Date date;
	private String address;
	private String status;
	private String orderedProducts;
	
	public Integer getOrderId() {
		return orderId;
	}
	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}
	public Integer getBuyerId() {
		return buyerId;
	}
	public void setBuyerId(Integer buyerId) {
		this.buyerId = buyerId;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getOrderedProducts() {
		return orderedProducts;
	}
	public void setOrderedProducts(String orderedProducts) {
		this.orderedProducts = orderedProducts;
	}
	
	
	
}
