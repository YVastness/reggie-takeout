package com.yinhaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinhaoyu.entity.User;
import com.yinhaoyu.mapper.UserMapper;
import com.yinhaoyu.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author Vastness
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
}