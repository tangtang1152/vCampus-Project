package com.vcampus.dao;

import java.sql.SQLException;
import java.util.List;
import com.vcampus.entity.Product;

public interface IProductDao {

	boolean addProduct(Product product) throws SQLException;
	boolean deleteProduct(String productId) throws SQLException;
	boolean updateProduct(Product product) throws SQLException;
	Product getProductById(String productId) throws SQLException;
	List<Product> getAllProducts() throws SQLException;
	List<Product> getProductsByCategory(String category) throws SQLException;
	
	boolean updateProductStock(String productId, int quantity) throws SQLException;
	
	 // 新增的方法
    boolean productExists(String productId) throws SQLException;
    List<Product> searchProductsByName(String productName) throws SQLException;
    List<String> getAllCategories() throws SQLException;
    List<Product> getProductsWithLowStock(int minStock) throws SQLException;
    boolean setProductStock(String productId, int newStock) throws SQLException;
    int getTotalProductCount() throws SQLException;
    int getProductCountByCategory(String category) throws SQLException;
}
