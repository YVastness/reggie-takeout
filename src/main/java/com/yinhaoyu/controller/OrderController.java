package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinhaoyu.common.BaseContext;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.dto.OrdersDto;
import com.yinhaoyu.entity.OrderDetail;
import com.yinhaoyu.entity.Orders;
import com.yinhaoyu.service.OrderDetailService;
import com.yinhaoyu.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("order")
public class OrderController {
    private final OrderService orderService;

    @Autowired
    private final OrderDetailService orderDetailService;

    public OrderController(OrderService orderService, OrderDetailService orderDetailService) {
        this.orderService = orderService;
        this.orderDetailService = orderDetailService;
    }


    @PostMapping("submit")
    public Result<String> submit(@RequestBody Orders orders) {
        orderService.submit(orders);
        return Result.success("下单成功");
    }

    @GetMapping("userPage")
    public Result<Page<OrdersDto>> pagination(Integer page, Integer pageSize) {

        Long userId = BaseContext.getCurrentId();
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrdersDto> pageInfoDto = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, userId);
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo, queryWrapper);
        BeanUtils.copyProperties(pageInfo, pageInfoDto);
        List<OrdersDto> records = pageInfoDto.getRecords();
        List<OrdersDto> ordersDtoList = records.stream().map(orders -> {
            OrdersDto ordersDto = new OrdersDto();
            LambdaQueryWrapper<OrderDetail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(OrderDetail::getOrderId, orders.getId());
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper);
            BeanUtils.copyProperties(orders, ordersDto);
            ordersDto.setOrderDetails(orderDetails);
            return ordersDto;
        }).collect(Collectors.toList());
        pageInfoDto.setRecords(ordersDtoList);
        return Result.success(pageInfoDto);
    }
}
