package com.crossborder.controller;

import com.crossborder.dto.ResultDTO;
import com.crossborder.model.Product;
import com.crossborder.store.MemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private MemoryStore memoryStore;

    @GetMapping
    public ResultDTO<List<Product>> getAllProducts() {
        List<Product> products = memoryStore.getAllProducts();
        return ResultDTO.success(products);
    }

    @GetMapping("/{id}")
    public ResultDTO<Product> getProductById(@PathVariable String id) {
        Product product = memoryStore.getProducts().get(id);
        if (product == null) {
            return ResultDTO.error("商品不存在");
        }
        return ResultDTO.success(product);
    }

    @PostMapping
    public ResultDTO<Product> createProduct(@RequestBody Product product) {
        product.setId(UUID.randomUUID().toString());
        memoryStore.getProducts().put(product.getId(), product);
        return ResultDTO.success("商品创建成功", product);
    }

    @PutMapping("/{id}")
    public ResultDTO<Product> updateProduct(@PathVariable String id, @RequestBody Product product) {
        Product existingProduct = memoryStore.getProducts().get(id);
        if (existingProduct == null) {
            return ResultDTO.error("商品不存在");
        }
        product.setId(id);
        memoryStore.getProducts().put(id, product);
        return ResultDTO.success("商品更新成功", product);
    }

    @DeleteMapping("/{id}")
    public ResultDTO<Void> deleteProduct(@PathVariable String id) {
        if (!memoryStore.getProducts().containsKey(id)) {
            return ResultDTO.error("商品不存在");
        }
        memoryStore.getProducts().remove(id);
        return ResultDTO.success("商品删除成功", null);
    }
}
