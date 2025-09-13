package com.vcampus.service;

import com.vcampus.entity.Order;
import com.vcampus.entity.OrderItem;
import com.vcampus.entity.Product;
import java.util.List;

/**
 * 商店服务接口
 * 定义商店相关的业务逻辑操作，整合商品和订单操作
 */
public interface IShopService {
    
    /**
     * 获取所有商品
     * @return 商品列表
     */
    List<Product> getAllProducts();
    
    /**
     * 根据分类获取商品
     * @param category 商品分类
     * @return 商品列表
     */
    List<Product> getProductsByCategory(String category);
    
    /**
     * 根据ID获取商品
     * @param productId 商品ID
     * @return 商品对象
     */
    Product getProductById(String productId);
    
    /**
     * 购买商品（创建订单）
     * @param studentId 学生ID
     * @param items 购买项列表
     * @return 订单ID，如果购买失败返回null
     */
    String purchase(String studentId, List<OrderItem> items);
    
    /**
     * 支付订单
     * @param orderId 订单ID
     * @return 是否支付成功
     */
    boolean payOrder(String orderId);
    
    /**
     * 取消订单
     * @param orderId 订单ID
     * @return 是否取消成功
     */
    boolean cancelOrder(String orderId);
    
    /**
     * 根据学生ID获取订单历史
     * @param studentId 学生ID
     * @return 订单列表
     */
    List<Order> getOrderHistory(String studentId);
    
    /**
     * 获取订单详情（包含订单项和商品信息）
     * @param orderId 订单ID
     * @return 订单对象（包含订单项和商品信息）
     */
    Order getOrderDetails(String orderId);
    
    /**
     * 检查商品库存是否足够
     * @param productId 商品ID
     * @param requiredQuantity 需要数量
     * @return 是否足够
     */
    boolean checkStock(String productId, int requiredQuantity);
    
    /**
     * 根据商品名称搜索商品
     * @param productName 商品名称
     * @return 商品列表
     */
    List<Product> searchProducts(String productName);
    
    /**
     * 获取所有商品分类
     * @return 分类列表
     */
    List<String> getAllCategories();
    
    /**
     * 获取购物车总金额
     * @param items 购物车项列表
     * @return 总金额
     */
    double calculateCartTotal(List<OrderItem> items);
    
    /**
     * 验证购物车项是否有效
     * @param items 购物车项列表
     * @return 是否有效
     */
    boolean validateCartItems(List<OrderItem> items);
    
    /**
     * 获取推荐商品（根据销量或库存）
     * @param limit 返回数量限制
     * @return 推荐商品列表
     */
    List<Product> getRecommendedProducts(int limit);
    
    /**
     * 获取热销商品
     * @param limit 返回数量限制
     * @return 热销商品列表
     */
    List<Product> getHotProducts(int limit);
}