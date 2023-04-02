package com.yinhaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yinhaoyu.entity.Employee;
import com.yinhaoyu.mapper.EmployeeMapper;
import com.yinhaoyu.service.EmployeeService;
import org.springframework.stereotype.Service;

/**
 * @author Vastness
 */
@Service
public class EmployeeServiceImpl extends ServiceImpl<EmployeeMapper, Employee> implements EmployeeService {
}