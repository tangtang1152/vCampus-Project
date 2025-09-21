package edu.seu.campus.server.controller;

import com.vCampus.common.ApiResponse;
import com.vCampus.entity.Product;
import com.vCampus.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    @Autowired
    private IProductService productService;

    @PostMapping
    public ApiResponse<Boolean> create(@RequestBody Product p) { return ApiResponse.ok(productService.addProduct(p)); }

    @DeleteMapping("/{productId}")
    public ApiResponse<Boolean> delete(@PathVariable String productId) { return ApiResponse.ok(productService.deleteProduct(productId)); }

    @PutMapping
    public ApiResponse<Boolean> update(@RequestBody Product p) { return ApiResponse.ok(productService.updateProduct(p)); }

    @GetMapping("/{productId}")
    public ApiResponse<Product> get(@PathVariable String productId) { return ApiResponse.ok(productService.getProductById(productId)); }

    @GetMapping
    public ApiResponse<List<Product>> listAll() { return ApiResponse.ok(productService.getAllProducts()); }

    @GetMapping("/by-category/{category}")
    public ApiResponse<List<Product>> byCategory(@PathVariable String category) { return ApiResponse.ok(productService.getProductsByCategory(category)); }

    @PostMapping("/{productId}/stock")
    public ApiResponse<Boolean> updateStock(@PathVariable String productId, @RequestParam int quantity) { return ApiResponse.ok(productService.updateProductStock(productId, quantity)); }

    @GetMapping("/{productId}/check-stock")
    public ApiResponse<Boolean> checkStock(@PathVariable String productId, @RequestParam int requiredQuantity) { return ApiResponse.ok(productService.checkStock(productId, requiredQuantity)); }

    @GetMapping("/exists/{productId}")
    public ApiResponse<Boolean> exists(@PathVariable String productId) { return ApiResponse.ok(productService.isProductExists(productId)); }

    @GetMapping("/search/{name}")
    public ApiResponse<List<Product>> searchByName(@PathVariable("name") String productName) { return ApiResponse.ok(productService.searchProductsByName(productName)); }

    @GetMapping("/categories")
    public ApiResponse<List<String>> categories() { return ApiResponse.ok(productService.getAllCategories()); }

    @GetMapping("/low-stock")
    public ApiResponse<List<Product>> lowStock(@RequestParam int minStock) { return ApiResponse.ok(productService.getProductsWithLowStock(minStock)); }

    @PostMapping("/{productId}/set-stock")
    public ApiResponse<Boolean> setStock(@PathVariable String productId, @RequestParam int newStock) { return ApiResponse.ok(productService.setProductStock(productId, newStock)); }

    @GetMapping("/count")
    public ApiResponse<Integer> totalCount() { return ApiResponse.ok(productService.getTotalProductCount()); }

    @GetMapping("/count/by-category/{category}")
    public ApiResponse<Integer> countByCategory(@PathVariable String category) { return ApiResponse.ok(productService.getProductCountByCategory(category)); }
}

