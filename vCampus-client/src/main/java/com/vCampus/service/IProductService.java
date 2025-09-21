package com.vCampus.service;

import com.vCampus.entity.Product;
import java.util.List;

/**
 * 商品服务接口
 * 定义商品相关的业务逻辑操作
 */
public interface IProductService {
    
    /**
     * 添加商品
     * @param product 商品对象
     * @return 是否添加成功
     */
    boolean addProduct(Product product);
    
    /**
     * 删除商品
     * @param productId 商品ID
     * @return 是否删除成功
     */
    boolean deleteProduct(String productId);
    
    /**
     * 更新商品信息
     * @param product 商品对象
     * @return 是否更新成功
     */
    boolean updateProduct(Product product);
    
    /**
     * 根据ID获取商品
     * @param productId 商品ID
     * @return 商品对象
     */
    Product getProductById(String productId);
    
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
     * 更新商品库存
     * @param productId 商品ID
     * @param quantity 变化数量（正数为增加，负数为减少）
     * @return 是否更新成功
     */
    boolean updateProductStock(String productId, int quantity);
    
    /**
     * 检查商品库存是否足够
     * @param productId 商品ID
     * @param requiredQuantity 需要数量
     * @return 是否足够
     */
    boolean checkStock(String productId, int requiredQuantity);
    
    /**
     * 检查商品是否存在
     * @param productId 商品ID
     * @return 是否存在
     */
    boolean isProductExists(String productId);
    
    /**
     * 根据商品名称搜索商品
     * @param productName 商品名称（支持模糊搜索）
     * @return 商品列表
     */
    List<Product> searchProductsByName(String productName);
    
    /**
     * 获取所有商品分类
     * @return 分类列表
     */
    List<String> getAllCategories();
    
    /**
     * 获取库存不足的商品
     * @param minStock 最小库存阈值
     * @return 商品列表
     */
    List<Product> getProductsWithLowStock(int minStock);
    
    /**
     * 设置商品库存（直接设置，不是增减）
     * @param productId 商品ID
     * @param newStock 新库存数量
     * @return 是否更新成功
     */
    boolean setProductStock(String productId, int newStock);
    
    /**
     * 获取商品总数
     * @return 商品总数
     */
    int getTotalProductCount();
    
    /**
     * 获取某个分类的商品数量
     * @param category 分类名称
     * @return 商品数量
     */
    int getProductCountByCategory(String category);
}
