package com.vCampus.service;

import com.vCampus.dao.IProductDao;
import com.vCampus.dao.ProductDaoImpl;
import com.vCampus.entity.Product;

import java.sql.SQLException;
import java.util.List;

/**
 * 商品服务实现类
 * 处理商品相关的业务逻辑
 */
public class ProductServiceImpl implements IProductService {
    
    private IProductDao productDao;
    
    public ProductServiceImpl() {
        this.productDao = new ProductDaoImpl();
    }
    
    /**
     * 构造方法，允许注入不同的DAO实现（用于测试等场景）
     * @param productDao 商品DAO实现
     */
    public ProductServiceImpl(IProductDao productDao) {
        this.productDao = productDao;
    }
    
    @Override
    public boolean addProduct(Product product) {
        try {
            // 验证商品数据
            if (product == null) {
                System.err.println("添加商品失败：商品对象不能为空");
                return false;
            }
            
            if (product.getProductId() == null || product.getProductId().trim().isEmpty()) {
                System.err.println("添加商品失败：商品ID不能为空");
                return false;
            }
            
            if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
                System.err.println("添加商品失败：商品名称不能为空");
                return false;
            }
            
            if (product.getPrice() < 0) {
                System.err.println("添加商品失败：商品价格不能为负数");
                return false;
            }
            
            if (product.getStock() < 0) {
                System.err.println("添加商品失败：商品库存不能为负数");
                return false;
            }
            
            // 检查商品ID是否已存在
            if (productDao.getProductById(product.getProductId()) != null) {
                System.err.println("添加商品失败：商品ID '" + product.getProductId() + "' 已存在");
                return false;
            }
            
            return productDao.addProduct(product);
            
        } catch (SQLException e) {
            System.err.println("添加商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean deleteProduct(String productId) {
        try {
            if (productId == null || productId.trim().isEmpty()) {
                System.err.println("删除商品失败：商品ID不能为空");
                return false;
            }
            
            // 检查商品是否存在
            Product existingProduct = productDao.getProductById(productId);
            if (existingProduct == null) {
                System.err.println("删除商品失败：商品ID '" + productId + "' 不存在");
                return false;
            }
            
            return productDao.deleteProduct(productId);
            
        } catch (SQLException e) {
            System.err.println("删除商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean updateProduct(Product product) {
        try {
            if (product == null || product.getProductId() == null) {
                System.err.println("更新商品失败：商品对象或商品ID不能为空");
                return false;
            }
            
            // 验证商品数据
            if (product.getPrice() < 0) {
                System.err.println("更新商品失败：商品价格不能为负数");
                return false;
            }
            
            if (product.getStock() < 0) {
                System.err.println("更新商品失败：商品库存不能为负数");
                return false;
            }
            
            // 检查商品是否存在
            Product existingProduct = productDao.getProductById(product.getProductId());
            if (existingProduct == null) {
                System.err.println("更新商品失败：商品ID '" + product.getProductId() + "' 不存在");
                return false;
            }
            
            return productDao.updateProduct(product);
            
        } catch (SQLException e) {
            System.err.println("更新商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public Product getProductById(String productId) {
        try {
            if (productId == null || productId.trim().isEmpty()) {
                System.err.println("获取商品失败：商品ID不能为空");
                return null;
            }
            
            return productDao.getProductById(productId);
            
        } catch (SQLException e) {
            System.err.println("获取商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Product> getAllProducts() {
        try {
            return productDao.getAllProducts();
            
        } catch (SQLException e) {
            System.err.println("获取所有商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Product> getProductsByCategory(String category) {
        try {
            if (category == null || category.trim().isEmpty()) {
                System.err.println("获取商品失败：分类不能为空");
                return null;
            }
            
            return productDao.getProductsByCategory(category);
            
        } catch (SQLException e) {
            System.err.println("根据分类获取商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean updateProductStock(String productId, int quantity) {
        try {
            if (productId == null || productId.trim().isEmpty()) {
                System.err.println("更新库存失败：商品ID不能为空");
                return false;
            }
            
            // 检查库存是否足够减少
            if (quantity < 0) {
                Product product = productDao.getProductById(productId);
                if (product != null && product.getStock() + quantity < 0) {
                    System.err.println("更新库存失败：商品 '" + productId + "' 库存不足，当前库存: " + product.getStock() + ", 需要: " + (-quantity));
                    return false;
                }
            }
            
            return productDao.updateProductStock(productId, quantity);
            
        } catch (SQLException e) {
            System.err.println("更新库存过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean checkStock(String productId, int requiredQuantity) {
        try {
            if (productId == null || productId.trim().isEmpty()) {
                System.err.println("检查库存失败：商品ID不能为空");
                return false;
            }
            
            if (requiredQuantity <= 0) {
                System.err.println("检查库存失败：需要数量必须大于0");
                return false;
            }
            
            Product product = productDao.getProductById(productId);
            return product != null && product.getStock() >= requiredQuantity;
            
        } catch (SQLException e) {
            System.err.println("检查库存过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public boolean isProductExists(String productId) {
        try {
            if (productId == null || productId.trim().isEmpty()) {
                return false;
            }
            
            return productDao.getProductById(productId) != null;
            
        } catch (SQLException e) {
            System.err.println("检查商品是否存在过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public List<Product> searchProductsByName(String productName) {
        try {
            if (productName == null || productName.trim().isEmpty()) {
                System.err.println("搜索商品失败：商品名称不能为空");
                return null;
            }
            
            return productDao.searchProductsByName(productName);
            
        } catch (SQLException e) {
            System.err.println("搜索商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<String> getAllCategories() {
        try {
            return productDao.getAllCategories();
            
        } catch (SQLException e) {
            System.err.println("获取所有分类过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Product> getProductsWithLowStock(int minStock) {
        try {
            if (minStock < 0) {
                System.err.println("获取低库存商品失败：最小库存阈值不能为负数");
                return null;
            }
            
            return productDao.getProductsWithLowStock(minStock);
            
        } catch (SQLException e) {
            System.err.println("获取低库存商品过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public boolean setProductStock(String productId, int newStock) {
        try {
            if (productId == null || productId.trim().isEmpty()) {
                System.err.println("设置库存失败：商品ID不能为空");
                return false;
            }
            
            if (newStock < 0) {
                System.err.println("设置库存失败：库存数量不能为负数");
                return false;
            }
            
            // 检查商品是否存在
            Product existingProduct = productDao.getProductById(productId);
            if (existingProduct == null) {
                System.err.println("设置库存失败：商品ID '" + productId + "' 不存在");
                return false;
            }
            
            return productDao.setProductStock(productId, newStock);
            
        } catch (SQLException e) {
            System.err.println("设置库存过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    @Override
    public int getTotalProductCount() {
        try {
            return productDao.getTotalProductCount();
            
        } catch (SQLException e) {
            System.err.println("获取商品总数过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    @Override
    public int getProductCountByCategory(String category) {
        try {
            if (category == null || category.trim().isEmpty()) {
                System.err.println("获取商品数量失败：分类不能为空");
                return 0;
            }
            
            return productDao.getProductCountByCategory(category);
            
        } catch (SQLException e) {
            System.err.println("获取分类商品数量过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }
    
    /**
     * 批量更新商品库存（事务处理）
     * @param stockUpdates 库存更新列表，每个元素为 [productId, quantity]
     * @return 是否全部更新成功
     */
    public boolean batchUpdateStock(List<Object[]> stockUpdates) {
        // 这里可以实现事务处理，确保要么全部成功，要么全部失败
        // 由于Access数据库的事务支持有限，这里简化实现
        boolean allSuccess = true;
        
        for (Object[] update : stockUpdates) {
            String productId = (String) update[0];
            int quantity = (Integer) update[1];
            
            if (!updateProductStock(productId, quantity)) {
                allSuccess = false;
                System.err.println("批量更新库存失败：商品 " + productId);
            }
        }
        
        return allSuccess;
    }
    
    /**
     * 获取商品价格
     * @param productId 商品ID
     * @return 商品价格，如果商品不存在返回-1
     */
    public double getProductPrice(String productId) {
        try {
            Product product = productDao.getProductById(productId);
            return product != null ? product.getPrice() : -1;
            
        } catch (SQLException e) {
            System.err.println("获取商品价格过程中发生数据库错误: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
}