package com.vCampus.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.vCampus.entity.Product;
import com.vCampus.net.HttpClientUtil;

import java.util.List;

/**
 * 商品服务实现类（客户端CS）：HTTP调用服务端
 */
public class ProductServiceImpl implements IProductService {

    @Override
    public boolean addProduct(Product product) {
        try {
            JsonNode data = HttpClientUtil.sendPost("/products", product);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean deleteProduct(String productId) {
        try {
            JsonNode data = HttpClientUtil.sendDelete("/products/" + productId);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean updateProduct(Product product) {
        try {
            JsonNode data = HttpClientUtil.sendPut("/products", product);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public Product getProductById(String productId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/" + productId);
            return HttpClientUtil.parseObject(data, Product.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public List<Product> getAllProducts() {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products");
            return HttpClientUtil.parseList(data, Product.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/by-category/" + category);
            return HttpClientUtil.parseList(data, Product.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean updateProductStock(String productId, int quantity) {
        try {
            JsonNode data = HttpClientUtil.sendPost("/products/" + productId + "/stock?quantity=" + quantity, java.util.Map.of());
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean checkStock(String productId, int requiredQuantity) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/" + productId + "/check-stock?requiredQuantity=" + requiredQuantity);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public boolean isProductExists(String productId) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/exists/" + productId);
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public List<Product> searchProductsByName(String productName) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/search/" + productName);
            return HttpClientUtil.parseList(data, Product.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public List<String> getAllCategories() {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/categories");
            return HttpClientUtil.parseList(data, String.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public List<Product> getProductsWithLowStock(int minStock) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/low-stock?minStock=" + minStock);
            return HttpClientUtil.parseList(data, Product.class);
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean setProductStock(String productId, int newStock) {
        try {
            JsonNode data = HttpClientUtil.sendPost("/products/" + productId + "/set-stock?newStock=" + newStock, java.util.Map.of());
            return data.asBoolean(false);
        } catch (Exception e) { return false; }
    }

    @Override
    public int getTotalProductCount() {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/count");
            return data.asInt(0);
        } catch (Exception e) { return 0; }
    }

    @Override
    public int getProductCountByCategory(String category) {
        try {
            JsonNode data = HttpClientUtil.sendGet("/products/count/by-category/" + category);
            return data.asInt(0);
        } catch (Exception e) { return 0; }
    }
}