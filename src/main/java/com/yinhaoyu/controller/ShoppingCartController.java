package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinhaoyu.common.BaseContext;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.entity.ShoppingCart;
import com.yinhaoyu.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("shoppingCart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    public ShoppingCartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    /**
     * 查看购物车
     */
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list() {
        log.info("查看购物车...");

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);

        return Result.success(list);
    }

    @PostMapping("add")
    public Result<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        ShoppingCart shoppingCartExisted = isSaveShoppingCart(shoppingCart);

        if (shoppingCartExisted != null) {
            Integer number = shoppingCartExisted.getNumber();
            number++;
            shoppingCartExisted.setNumber(number);
            shoppingCartService.updateById(shoppingCartExisted);
        } else {
            shoppingCart.setNumber(1);
            shoppingCartService.save(shoppingCart);
            shoppingCartExisted = shoppingCart;
        }
        return Result.success(shoppingCartExisted);
    }

    @PostMapping("sub")
    public Result<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        ShoppingCart shoppingCartExisted = isSaveShoppingCart(shoppingCart);

        if (shoppingCartExisted != null) {
            Integer number = shoppingCartExisted.getNumber();
            if (number > 0) {
                number--;
                shoppingCartExisted.setNumber(number);
                shoppingCartService.updateById(shoppingCartExisted);
            }
        }
        return Result.success(shoppingCartExisted);
    }

    @DeleteMapping("clean")
    public Result<String> clean() {
        Long user = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, user);
        boolean isRemove = shoppingCartService.remove(queryWrapper);
        if (isRemove) {
            return Result.success("成功");
        }
        return Result.error("清空购物车失败");
    }

    private ShoppingCart isSaveShoppingCart(@RequestBody ShoppingCart shoppingCart) {
        Long user = BaseContext.getCurrentId();
        shoppingCart.setUserId(user);
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, user);
        queryWrapper.eq(shoppingCart.getSetmealId() != null, ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        queryWrapper.eq(shoppingCart.getDishId() != null, ShoppingCart::getDishId, shoppingCart.getDishId());

        return shoppingCartService.getOne(queryWrapper);
    }
}
