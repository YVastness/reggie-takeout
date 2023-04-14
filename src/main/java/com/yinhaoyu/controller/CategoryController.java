package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.entity.Category;
import com.yinhaoyu.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类管理控制类
 *
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public Result<String> save(@RequestBody Category category) {
        boolean isSave = categoryService.save(category);
        if (isSave) {
            return Result.success("新增分类成功");
        }
        return Result.error("新增分类成功");
    }

    @GetMapping("page")
    public Result<Page<Category>> pagination(Integer page, Integer pageSize) {
        Page<Category> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        categoryService.page(pageInfo, queryWrapper);
        return Result.success(pageInfo);
    }

    @DeleteMapping
    public Result<String> delete(@RequestParam("ids") Long id) {
        categoryService.remove(id);
        return Result.success("删除成功");
    }

    @PutMapping
    public Result<String> update(@RequestBody Category category) {
        boolean isUpdate = categoryService.updateById(category);
        if (isUpdate) {
            return Result.success("更新分类成功");
        }
        return Result.error("更新分类失败");
    }

    @GetMapping("list")
    public Result<List<Category>> list(Integer type) {
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(type != null, Category::getType, type);
        List<Category> categories = categoryService.list(queryWrapper);
        if (categories != null) {
            return Result.success(categories);
        }
        return Result.error("还没有添加菜品分类");
    }
}
