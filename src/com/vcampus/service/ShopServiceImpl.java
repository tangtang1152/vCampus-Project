package com.vcampus.service;

import com.vcampus.dao.IOrderDao;
import com.vcampus.dao.IOrderItemDao;
import com.vcampus.dao.IProductDao;
import com.vcampus.dao.OrderDaoImpl;
import com.vcampus.dao.OrderItemDaoImpl;
import com.vcampus.dao.ProductDaoImpl;
import com.vcampus.entity.Order;
import com.vcampus.entity.OrderItem;
import com.vcampus.entity.Product;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 商店服务实现类
 * 处理商店相关的业务逻辑，整合商品和订单操作
 */
public class ShopServiceImpl implements IShopService {
    
    private IProductService productService;
    private IOrderService orderService;
    private IOrderDao orderDao;
    private IOrderItemDao orderItemDao;
    private IProductDao productDao;
    
    public ShopServiceImpl() {
        this.productService = new ProductServiceImpl();
        this.orderService = new OrderServiceImpl();
        this.orderDao = new OrderDaoImpl();
        this.orderItemDao = new OrderItemDaoImpl();
        this.productDao = new ProductDaoImpl();
    }
    
    /**
     * 构造方法，允许注入不同的服务实现
     */
    public ShopServiceImpl(IProductService productService, IOrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }
    
    @Override
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }
    
    @Override
    public List<Product> getProductsByCategory(String category) {
        return productService.getProductsByCategory(category);
    }
    
    @Override
    public Product getProductById(String productId) {
        return productService.getProductById(productId);
    }
    
    @Override
    public String purchase(String studentId, List<OrderItem> items) {
        if (studentId == null || studentId.trim().isEmpty()) {
            System.err.println("购买失败：学生ID不能为空");
            return null;
        }
        
        if (items == null || items.isEmpty()) {
            System.err.println("购买失败：购物车不能为空");
            return null;
        }
        
        // 1. 验证购物车项
        if (!validateCartItems(items)) {
            System.err.println("购买失败：购物车项验证失败");
            return null;
        }
        
        // 2. 检查库存
        for (OrderItem item : items) {
            if (!productService.checkStock(item.getProductId(), item.getQuantity())) {
                System.err.println("购买失败：商品 " + item.getProductId() + " 库存不足");
                return null;
            }
        }
        
        // 3. 计算总金额
        double totalAmount = calculateCartTotal(items);
        
        // 4. 生成订单号
        String orderId = generateOrderId();
        
        // 5. 创建订单对象
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStudentId(studentId);
        order.setOrderDate(new Date());
        order.setTotalAmount(totalAmount);
        order.setStatus("待支付");
        
        try {
            // 6. 创建订单
            boolean orderCreated = orderDao.createOrder(order);
            if (!orderCreated) {
                System.err.println("购买失败：创建订单失败");
                return null;
            }
            
            // 7. 添加订单项并更新库存
            for (OrderItem item : items) {
                item.setOrderId(orderId);
                
                // 计算小计金额
                Product product = productService.getProductById(item.getProductId());
                if (product != null) {
                    item.setSubtotal(product.getPrice() * item.getQuantity());
                }
                
                // 添加订单项
                boolean itemAdded = orderItemDao.addOrderItem(item);
                if (!itemAdded) {
                    // 回滚：删除订单
                    orderDao.deleteOrder(orderId);
                    System.err.println("购买失败：添加订单项失败");
                    return null;
                }
                
                // 更新库存
                boolean stockUpdated = productService.updateProductStock(
                    item.getProductId(), -item.getQuantity());
                if (!stockUpdated) {
                    // 回滚：删除订单项和订单
                    orderItemDao.deleteOrderItemsByOrderId(orderId);
                    orderDao.deleteOrder(orderId);
                    System.err.println("购买失败：更新库存失败");
                    return null;
                }
            }
            
            System.out.println("订单创建成功: " + orderId);
            return orderId;
            
        } catch (SQLException e) {
            System.err.println("购买过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean payOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            System.err.println("支付失败：订单ID不能为空");
            return false;
        }
        
        // 检查订单是否存在且状态为待支付
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            System.err.println("支付失败：订单不存在");
            return false;
        }
        
        if (!"待支付".equals(order.getStatus())) {
            System.err.println("支付失败：订单状态不是待支付");
            return false;
        }
        
        // 更新订单状态为已支付
        return orderService.updateOrderStatus(orderId, "已支付");
    }
    
    @Override
    public boolean cancelOrder(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            System.err.println("取消订单失败：订单ID不能为空");
            return false;
        }
        
        // 检查订单是否存在
        Order order = orderService.getOrderById(orderId);
        if (order == null) {
            System.err.println("取消订单失败：订单不存在");
            return false;
        }
        
        // 只有待支付和已支付的订单可以取消
        if (!"待支付".equals(order.getStatus()) && !"已支付".equals(order.getStatus())) {
            System.err.println("取消订单失败：订单状态不允许取消");
            return false;
        }
        
        try {
            // 获取订单项
            List<OrderItem> items = orderItemDao.getOrderItemsByOrderId(orderId);
            
            // 恢复库存
            for (OrderItem item : items) {
                productService.updateProductStock(item.getProductId(), item.getQuantity());
            }
            
            // 更新订单状态为已取消
            return orderService.updateOrderStatus(orderId, "已取消");
            
        } catch (SQLException e) {
            System.err.println("取消订单过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<Order> getOrderHistory(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            System.err.println("获取订单历史失败：学生ID不能为空");
            return null;
        }
        
        return orderService.getOrdersByStudentId(studentId);
    }
    
    @Override
    public Order getOrderDetails(String orderId) {
        if (orderId == null || orderId.trim().isEmpty()) {
            System.err.println("获取订单详情失败：订单ID不能为空");
            return null;
        }
        
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            List<OrderItem> items = orderService.getOrderItemsByOrderId(orderId);
            // 这里可以进一步获取商品详细信息
            // for (OrderItem item : items) {
            //     Product product = productService.getProductById(item.getProductId());
            //     item.setProduct(product);
            // }
            // order.setItems(items);
        }
        return order;
    }
    
    @Override
    public boolean checkStock(String productId, int requiredQuantity) {
        return productService.checkStock(productId, requiredQuantity);
    }
    
    @Override
    public List<Product> searchProducts(String productName) {
        return productService.searchProductsByName(productName);
    }
    
    @Override
    public List<String> getAllCategories() {
        return productService.getAllCategories();
    }
    
    @Override
    public double calculateCartTotal(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return 0.0;
        }
        
        double total = 0.0;
        for (OrderItem item : items) {
            Product product = productService.getProductById(item.getProductId());
            if (product != null) {
                total += product.getPrice() * item.getQuantity();
            }
        }
        return total;
    }
    
    @Override
    public boolean validateCartItems(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        
        for (OrderItem item : items) {
            if (item.getProductId() == null || item.getProductId().trim().isEmpty()) {
                System.err.println("购物车项验证失败：商品ID不能为空");
                return false;
            }
            
            if (item.getQuantity() <= 0) {
                System.err.println("购物车项验证失败：数量必须大于0");
                return false;
            }
            
            // 检查商品是否存在
            Product product = productService.getProductById(item.getProductId());
            if (product == null) {
                System.err.println("购物车项验证失败：商品不存在");
                return false;
            }
        }
        
        return true;
    }
    
    @Override
    public List<Product> getRecommendedProducts(int limit) {
        // 简单的推荐逻辑：返回库存充足的商品，限制数量
        List<Product> allProducts = productService.getAllProducts();
        if (allProducts == null) {
            return null;
        }
        
        List<Product> recommended = new ArrayList<>();
        for (Product product : allProducts) {
            if (product.getStock() > 10) { // 库存大于10的商品
                recommended.add(product);
                if (recommended.size() >= limit) {
                    break;
                }
            }
        }
        return recommended;
    }
    
    @Override
    public List<Product> getHotProducts(int limit) {
        // 简单的热销逻辑：返回价格较低的商品（模拟热销）
        List<Product> allProducts = productService.getAllProducts();
        if (allProducts == null) {
            return null;
        }
        
        // 按价格排序（价格低的排在前面）
        allProducts.sort((p1, p2) -> Double.compare(p1.getPrice(), p2.getPrice()));
        
        List<Product> hotProducts = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, allProducts.size()); i++) {
            hotProducts.add(allProducts.get(i));
        }
        return hotProducts;
    }
    
    /**
     * 生成订单ID
     * @return 订单ID
     */
    private String generateOrderId() {
        return "ORD" + System.currentTimeMillis();
    }
    
    /**
     * 获取购物车商品详情
     * @param items 购物车项列表
     * @return 包含商品详情的购物车项列表
     */
    public List<OrderItem> getCartWithProductDetails(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<OrderItem> detailedItems = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productService.getProductById(item.getProductId());
            if (product != null) {
                // 创建新的订单项对象，包含商品详情
                OrderItem detailedItem = new OrderItem();
                detailedItem.setProductId(item.getProductId());
                detailedItem.setQuantity(item.getQuantity());
                detailedItem.setSubtotal(product.getPrice() * item.getQuantity());
                // 可以设置商品详情
                // detailedItem.setProduct(product);
                detailedItems.add(detailedItem);
            }
        }
        return detailedItems;
    }
    
    /**
     * 批量检查库存
     * @param items 购物车项列表
     * @return 是否所有商品库存都足够
     */
    public boolean batchCheckStock(List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            return false;
        }
        
        for (OrderItem item : items) {
            if (!productService.checkStock(item.getProductId(), item.getQuantity())) {
                return false;
            }
        }
        return true;
    }
}
