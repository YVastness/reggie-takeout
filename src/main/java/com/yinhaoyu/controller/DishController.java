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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 *
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("dish")
public class DishController {
    private final DishService dishService;
    private final DishFlavorService dishFlavorService;
    private final CategoryService categoryService;
    private final RedisTemplate<Object, Object> redisTemplate;

    public DishController(DishService dishService, DishFlavorService dishFlavorService, CategoryService categoryService, RedisTemplate<Object, Object> redisTemplate) {
        this.dishService = dishService;
        this.dishFlavorService = dishFlavorService;
        this.categoryService = categoryService;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 分页查询
     *
     * @param page     页码
     * @param pageSize 页的大小
     * @param name     按名查询
     * @return 页信息
     */
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

    /**
     * 添加菜品
     *
     * @param dishDto 组合菜品和菜品口味的映射类
     * @return 添加菜品是否成功
     */
    @PostMapping
    public Result<String> save(@RequestBody DishDto dishDto) {
        List<DishFlavor> flavors = dishDto.getFlavors();
        for (DishFlavor flavor : flavors) {
            flavor.setDishId(dishDto.getId());
            dishFlavorService.save(flavor);
        }
        boolean isSave = dishService.save(dishDto);
        if (isSave) {
            // 清除菜品缓存
            String key = "dishCategory_" + dishDto.getCategoryId();
            redisTemplate.delete(key);
            return Result.success("菜品添加成功");
        }
        return Result.error("菜品添加失败");
    }

    /**
     * 更新菜品售卖状态
     *
     * @param status 售卖状态
     * @param ids    需要更新售卖状态的菜品
     * @return 更新菜品售卖状态是否成功
     */
    @PostMapping("status/{status}")
    public Result<String> updateStatus(@PathVariable Integer status, Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.set(Dish::getStatus, status);
            updateWrapper.eq(Dish::getId, ids[i]);
            dishService.update(updateWrapper);
            if (i == ids.length - 1) {
                // 清除菜品缓存
                Set<Object> keys = redisTemplate.keys("dishCategory_*");
                if (keys != null) {
                    redisTemplate.delete(keys);
                }
                return Result.success("");
            }
        }
        return Result.error("转换售卖状态失败");
    }

    /**
     * 更新菜品信息
     *
     * @param dishDto 组合菜品和菜品口味的映射类
     * @return 更新菜品售卖状态是否成功
     */
    @PutMapping
    public Result<String> update(@RequestBody DishDto dishDto) {
        List<DishFlavor> flavors = dishDto.getFlavors();
        dishFlavorService.updateBatchById(flavors);
        boolean isSave = dishService.updateById(dishDto);
        if (isSave) {
            // 清除菜品缓存
            String key = "dishCategory_" + dishDto.getCategoryId();
            redisTemplate.delete(key);
            return Result.success("菜品修改成功");
        }
        return Result.error("菜品修改失败");
    }

    /**
     * 通过菜品id获取菜品信息
     *
     * @param id 菜品id
     * @return 菜品信息
     */
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

    /**
     * 删除菜品
     *
     * @param ids 被删除菜品们的id
     * @return 菜品们是否被删除
     */
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
            // 清除菜品缓存
            Set<Object> keys = redisTemplate.keys("dishCategory_*");
            if (keys != null) {
                redisTemplate.delete(keys);
            }
            dishService.update(updateWrapper);
            if (i == ids.length - 1) {
                return Result.success("");
            }
        }
        return Result.error("菜品删除失败");
    }

    /**
     * 显示一个菜品分类里的菜品信息
     *
     * @param dish 通过分类id获取菜品信息
     * @return 返回菜品信息
     */
    @GetMapping("list")
    public Result<List<DishDto>> list(Dish dish) {
        List<DishDto> dishDtos;
        String key = "dishCategory_" + dish.getCategoryId();
        // 先从Redis获取菜品缓存
        dishDtos = (List<DishDto>) redisTemplate.opsForValue().get(key);

        // 如果存在直接返回菜品数据
        if (dishDtos != null) {
            return Result.success(dishDtos);
        }

        // 不存在需要先查询数据库，将查询的数据缓存到redis中
        // 通过categoryId查询菜品信息
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        queryWrapper.eq(dish.getStatus() != null, Dish::getStatus, dish.getStatus());
        List<Dish> dishes = dishService.list(queryWrapper);
        // 通过stream流将菜品和菜品口味映射到dishDto
        dishDtos = dishes.stream().map(dishValue -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dishValue, dishDto);
            LambdaQueryWrapper<DishFlavor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DishFlavor::getDishId, dishDto.getId());
            List<DishFlavor> dishFlavors = dishFlavorService.list(wrapper);
            dishDto.setFlavors(dishFlavors);
            Category category = categoryService.getById(dishValue.getCategoryId());
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);
            return dishDto;
        }).collect(Collectors.toList());
        redisTemplate.opsForValue().set(key, dishDtos, 1, TimeUnit.HOURS);
        return Result.success(dishDtos);
    }
}
