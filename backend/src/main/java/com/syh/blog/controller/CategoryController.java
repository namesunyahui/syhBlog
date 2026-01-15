package com.syh.blog.controller;

import com.syh.blog.common.Result;
import com.syh.blog.entity.Category;
import com.syh.blog.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类Controller
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@Tag(name = "分类管理")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Operation(summary = "获取分类列表")
    @GetMapping
    public Result<List<Category>> list() {
        List<Category> categories = categoryService.listWithCount();
        return Result.success(categories);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> detail(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        return Result.success(category);
    }

    // ==================== 管理后台接口 ====================

    @Operation(summary = "获取所有分类（管理后台）")
    @GetMapping("/all")
    public Result<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.list();
        return Result.success(categories);
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Category> createCategory(@RequestBody Category category) {
        // 检查分类名称是否已存在
        Category existing = categoryService.lambdaQuery()
                .eq(Category::getName, category.getName())
                .one();
        if (existing != null) {
            return Result.error("分类名称已存在");
        }
        categoryService.save(category);
        return Result.success(category);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category existing = categoryService.getById(id);
        if (existing == null) {
            return Result.error("分类不存在");
        }
        category.setId(id);
        categoryService.updateById(category);
        return Result.success(category);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        if (category == null) {
            return Result.error("分类不存在");
        }
        // TODO: 检查该分类下是否有关联的文章，如果有需要提示用户
        categoryService.removeById(id);
        return Result.success();
    }
}
