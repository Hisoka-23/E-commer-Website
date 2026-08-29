package com.ecom.service;

import com.ecom.model.OrderRequest;
import com.ecom.model.ProductOrder;

import java.util.List;

public interface OrderService {

    public void saveOrder(Integer userId, OrderRequest orderRequest);

    public List<ProductOrder> getOrders(Integer userId);

    public ProductOrder updateOrderStatus(Integer id, String status);

    public List<ProductOrder> getAllOrders();

    public ProductOrder getOrderById(String orderId);

}
