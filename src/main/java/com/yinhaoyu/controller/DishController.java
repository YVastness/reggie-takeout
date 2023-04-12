package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.dto.DishDto;
import com.yinhaoyu.entity.Category;
import com.yinhaoyu.entity.Dish;
import com.yinhaoyu.entity.DishFlavor;
import com.yinhaoyu.service.CategoryService;
import com.yinhaoyu.service.DishFlavorService;
import com.yinhaoyu.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("dish")
public class DishController {
    private final DishService dishService;
    private final DishFlavorService dishFlavorService;
    private final CategoryService categoryService;

    public DishController(DishService dishService, DishFlavorService dishFlavorService, CategoryService categoryService) {
        this.dishService = dishService;
        this.dishFlavorService = dishFlavorService;
        this.categoryService = categoryService;
    }

    @GetMapping("page")
    public Result<Page<DishDto>> pagination(Integer page, Integer pageSize, String name) {
        log.info("{} {}", page, pageSize);
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> pageInfoDto = new Page<>();
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null, Dish::getName, name);
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        queryWrapper.ne(Dish::getIsDeleted, 1);
        dishService.page(pageInfo, queryWrapper);
        // 对象属性拷贝
        BeanUtils.copyProperties(pageInfo, pageInfoDto, "records");
        List<Dish> records = pageInfo.getRecords();

        List<DishDto> dishDtoList = records.stream().map(dishItem -> {
            DishDto dishDto = new DishDto();
            // 获取categoryId得到 category
            BeanUtils.copyProperties(dishItem, dishDto);
            Category category = categoryService.getById(dishItem.getCategoryId());
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());
        pageInfoDto.setRecords(dishDtoList);
        return Result.success(pageInfoDto);
    }

    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
            dishFlavorService.save(flavor);
        }
        boolean isSave = dishService.save(dishDto);
        if (isSave) {
            return Result.success("菜品添加成功");
        }
        return Result.error("菜品添加失败");
    }

    @PostMapping("status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Dish::getStatus, status);
            updateWrapper.eq(Dish::getId, ids[i]);
            dishService.update(updateWrapper);
            if (i == ids.length - 1) {
                return Result.success("");
            }
        }
        return Result.error("转换售卖状态失败");
    }

    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        List<DishFlavor> flavors = dishDto.getFlavors();
        dishFlavorService.updateBatchById(flavors);
        boolean isSave = dishService.updateById(dishDto);
        if (isSave) {
            return Result.success("菜品修改成功");
        }
        return Result.error("菜品修改失败");
    }

    @GetMapping("{id}")
    public Result<DishDto> getById(@PathVariable Long id) {
        DishDto dishDto = new DishDto();
        Dish dish = dishService.getById(id);
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, id);
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);
        BeanUtils.copyProperties(dish, dishDto);
        return Result.success(dishDto);
    }

    @DeleteMapping
    public Result<String> delete(Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Dish::getIsDeleted, 1);
            updateWrapper.eq(Dish::getId, ids[i]);
            LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId, ids[i]);
            List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
            int finalI = i;
            flavors.stream().filter(dishFlavor -> Objects.equals(dishFlavor.getDishId(), ids[finalI])).forEach(dishFlavor -> {
                LambdaUpdateWrapper<DishFlavor> updateFlavor = new LambdaUpdateWrapper<>();
                updateFlavor.set(DishFlavor::getIsDeleted, 1);
                updateFlavor.eq(DishFlavor::getDishId, ids[finalI]);
                dishFlavorService.update(updateFlavor);
            });
            dishService.update(updateWrapper);
            if (i == ids.length - 1) {
                return Result.success("");
            }
        }
        return Result.error("菜品删除失败");
    }
}
