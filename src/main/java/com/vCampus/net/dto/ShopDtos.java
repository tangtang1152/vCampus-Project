package com.vCampus.net.dto;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ShopDtos {
    public static class OrderItemDTO implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
        public String productId;
        public int quantity;
    }

    public static class CreateOrderReqDTO implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
        public String studentId;
        public List<OrderItemDTO> items = new ArrayList<>();
    }

    public static class CreateOrderRespDTO implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
        public String orderId;
        public double totalAmount;
        public String status;
    }
}


