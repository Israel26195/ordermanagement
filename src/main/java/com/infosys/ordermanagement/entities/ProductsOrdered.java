package com.infosys.ordermanagement.entities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

import org.springframework.stereotype.Component;

@Component
@Entity
@IdClass(ProductsOrdered.class)
@Table(name="productsordered")
public class ProductsOrdered implements Serializable{
	
	@Id
	@Column(name="ORDERID")
	private Integer orderId;
	
	@Id
	@Column(name="PRODID")
	private Integer prodId;
	
	@Column(name="SELLERID")
	private Integer sellerId;
	
	@Column(name="QUANTITY")
	private Integer quantity;
	
	@Column(name="STATUS")
	private String status;

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getOrderId() {
		return orderId;
	}

	public void setOrderId(Integer orderId) {
		this.orderId = orderId;
	}

	public Integer getProdId() {
		return prodId;
	}

	public void setProdId(Integer prodId) {
		this.prodId = prodId;
	}

	public Integer getSellerId() {
		return sellerId;
	}

	@Override
	public String toString() {
		return "ProductsOrdered [orderId=" + orderId + ", prodId=" + prodId + ", sellerId=" + sellerId + ", quantity="
				+ quantity + ", status=" + status + "]";
	}

	public void setSellerId(Integer sellerId) {
		this.sellerId = sellerId;
	}

}
