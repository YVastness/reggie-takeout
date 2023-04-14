package com.yinhaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yinhaoyu.entity.Orders;

/**
 * @author Vastness
 */
public interface OrderService extends IService<Orders> {

    void submit(Orders orders);
}
