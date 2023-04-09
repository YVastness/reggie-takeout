package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.entity.Employee;
import com.yinhaoyu.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);
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
        log.info(password);
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername, employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        logger.info("test{}", emp);
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
    public Result<String> saveEmployee(@RequestBody Employee employee) {
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        log.info("新增员工：{}", employee);
        employeeService.save(employee);
        return Result.success("成功添加员工");
    }

    @GetMapping("page")
    public Result<Page<Employee>> pagination(Integer page, Integer pageSize, String name) {
        log.info("page={}, pageSize={}, name={}", page, pageSize, name);
        // 构造分页构造器
        Page<Employee> pageInfo = new Page<>(page, pageSize);
        // 构造查询包装器
        LambdaQueryWrapper<Employee> queryWrapper = new LambdaQueryWrapper<>();
        // 添加模糊查询条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Employee::getUsername, name);
        // 添加排序查询条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        // 执行查询
        employeeService.page(pageInfo, queryWrapper);
        log.info("page:{}", pageInfo.getRecords());
        return Result.success(pageInfo);
    }

    @PutMapping
    public Result<String> updateEmployee(@RequestBody Employee employee) {
        if (employeeService.updateById(employee)) {
            return Result.success("更新员工成功");
        }
        return Result.error("更新员工失败");
    }

    @GetMapping("{id}")
    public Result<Employee> getById(@PathVariable Long id) {
        Employee employee = employeeService.getById(id);
        if (employee != null) {
            return Result.success(employee);
        }
        return Result.error("获取成员失败");
    }

}
