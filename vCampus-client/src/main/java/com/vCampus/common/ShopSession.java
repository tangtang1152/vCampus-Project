package com.vCampus.common;

import com.vCampus.entity.OrderItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 商店会话（简易购物车与支付上下文）
 */
public class ShopSession {
    private static final List<OrderItem> cartItems = new ArrayList<>();
    private static double walletBalance = 200.0; // 模拟余额
    private static String pendingOrderId;

    public static List<OrderItem> getCartItems() {
        return cartItems;
    }

    public static void clearCart() {
        cartItems.clear();
    }

    public static double getWalletBalance() {
        return walletBalance;
    }

    public static void setWalletBalance(double value) {
        walletBalance = value;
    }

    public static String getPendingOrderId() {
        return pendingOrderId;
    }

    public static void setPendingOrderId(String orderId) {
        pendingOrderId = orderId;
    }
}


