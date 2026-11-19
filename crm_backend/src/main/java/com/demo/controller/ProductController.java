package com.demo.controller;

import com.demo.common.PageResult;
import com.demo.common.Result;
import com.demo.entity.CrmProduct;
import com.demo.entity.CrmProductCategory;
import com.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 产品（易损件 / 养护用品）与产品分类 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /* ---------------- 产品 ---------------- */

    @GetMapping("/products")
    public Result<PageResult<CrmProduct>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.ok(productService.page(keyword, categoryId, brand, status, page, pageSize));
    }

    @GetMapping("/products/{id}")
    public Result<CrmProduct> detail(@PathVariable Long id) {
        return Result.ok(productService.detail(id));
    }

    @PostMapping("/products")
    public Result<Void> create(@RequestBody CrmProduct product) {
        productService.save(null, product);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/products/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CrmProduct product) {
        productService.save(id, product);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/products/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.ok(null, "删除成功");
    }

    /* ---------------- 分类 ---------------- */

    @GetMapping("/product-categories")
    public Result<List<CrmProductCategory>> categories(@RequestParam(required = false) Integer status) {
        return Result.ok(productService.listCategories(status));
    }

    @PostMapping("/product-categories")
    public Result<Void> createCategory(@RequestBody CrmProductCategory category) {
        productService.saveCategory(null, category);
        return Result.ok(null, "新增成功");
    }

    @PutMapping("/product-categories/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody CrmProductCategory category) {
        productService.saveCategory(id, category);
        return Result.ok(null, "修改成功");
    }

    @DeleteMapping("/product-categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        productService.deleteCategory(id);
        return Result.ok(null, "删除成功");
    }
}
