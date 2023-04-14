package com.yinhaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinhaoyu.entity.OrderDetail;
import com.yinhaoyu.mapper.OrderDetailMapper;
import com.yinhaoyu.service.OrderDetailService;
import org.springframework.stereotype.Service;

/**
 * @author Vastness
 */
@Service
public class OrderDetailServiceimpl extends ServiceImpl<OrderDetailMapper, OrderDetail> implements OrderDetailService {
}
