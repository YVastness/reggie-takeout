package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.entity.Employee;
import com.yinhaoyu.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 员工登录
     *
     * @param request  HttpServletRequest
     * @param employee 请求体里的内容封装成员工实体类
     * @return Result(Employee)
     */
    @PostMapping("/login")
    public Result<Employee> login(HttpServletRequest request, @RequestBody Employee employee) {
        // 1. 根据前端提交的密码通过DigestUtils.md5DigestAsHex转化MD5格式
        String password = DigestUtils.md5DigestAsHex(employee.getPassword().getBytes());
        // 2. 根据前端提交的用户名username查询数据库
        log.info(employee.getUsername());
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        // 3. 没有查询到用户就返回登陆失败
        if (emp == null) {
            return Result.error("登陆失败");
        }
        // 4. 如果密码匹配不上就返回登陆失败
        if (!emp.getPassword().equals(password)) {
            return Result.error("登陆失败");
        }
        // 5. 查看员工状态，如果是已禁用状态，返回禁用结果
        if (emp.getStatus() == 0) {
            return Result.error("账号已禁用");
        }
        // 6. 登录成功，将用户id存入session并返回用户登陆成功
        request.getSession().setAttribute("employee", emp.getId());
        return Result.success(emp);
    }

    /**
     * 员工退出
     *
     * @param request HttpServletRequest
     * @return Result(Employee)
     */
    @PostMapping("/logout")
    public Result<Employee> logout(HttpServletRequest request) {
        request.getSession().removeAttribute("employee");
        Result<Employee> result = new Result<>();
        result.setCode(1);
        return result;
    }

    @PostMapping
    public Result<String> saveEmployee(HttpServletRequest request, @RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser((Long) request.getSession().getAttribute("employee"));
        employee.setUpdateUser((Long) request.getSession().getAttribute("employee"));
        log.info("新增员工：{}", employee);
        employeeService.save(employee);
        return Result.success("成功添加员工");
    }


}
