package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.entity.User;
import com.yinhaoyu.service.UserService;
import com.yinhaoyu.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code: {}", code);
            // SMSUtils.sendMessage("瑞吉外卖", "", phone, code);
            session.setAttribute(phone, code);
            return Result.success("短信发送成功");
        }
        return Result.error("短信发送失败");
    }

    @PostMapping("login")
    public Result<User> login(@RequestBody Map<String, String> user, HttpSession session) {
        String phone = user.get("phone");
        String code = user.get("code");
        String sessionCode = (String) session.getAttribute(phone);
        if (sessionCode != null && sessionCode.equals(code)) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User userDto = userService.getOne(queryWrapper);
            if (userDto == null) {
                userDto = new User();
                userDto.setPhone(phone);
                userDto.setStatus(1);
                userService.save(userDto);
            }
            session.setAttribute("user", userDto.getId());
            return Result.success(userDto);
        }
        return Result.error("验证码错误");
    }
}
