package com.yinhaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yinhaoyu.entity.Employee;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Vastness
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {
}