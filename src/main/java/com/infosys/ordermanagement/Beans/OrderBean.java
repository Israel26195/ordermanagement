package com.infosys.ordermanagement.Beans;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;

public class OrderBean {
	private Integer orderId;
	private Integer buyerId;
	private BigDecimal amount;
	private Date date;
	private String address;
	private String status;
	private ArrayList<Product> orderedProducts;
	
	public ArrayList<Product> getOrderedProducts() {
		return orderedProducts;
	}
	public void setOrderedProducts(ArrayList<Product> orderedProducts) {
		this.orderedProducts = orderedProducts;
	}
	public OrderBean() {
		
	}
	
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
	@Override
	public String toString() {
		return "OrderBean [orderId=" + orderId + ", buyerId=" + buyerId + ", amount=" + amount + ", date=" + date
				+ ", address=" + address + ", status=" + status + ", orderedProducts=" + orderedProducts + "]";
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
}
