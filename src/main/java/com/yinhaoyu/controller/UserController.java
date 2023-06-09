package com.yinhaoyu.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yinhaoyu.common.Result;
import com.yinhaoyu.entity.User;
import com.yinhaoyu.service.UserService;
import com.yinhaoyu.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Vastness
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    private final UserService userService;
    private final RedisTemplate<String, String> redisTemplate;

    public UserController(UserService userService, RedisTemplate<String, String> redisTemplate) {
        this.userService = userService;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("sendMsg")
    public Result<String> sendMsg(@RequestBody User user, HttpSession session) {
        String phone = user.getPhone();
        if (StringUtils.isNotEmpty(phone)) {
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("code: {}", code);

            // 如果有短信服务的使用阿里云短信服务sdk
            // SMSUtils.sendMessage("瑞吉外卖", "", phone, code);

            // 使用session存入phone和code，用来登录时与用户输入的code进行匹配
            // session.setAttribute(phone, code);

            // 将生成的验证码用Redis缓存，并且设置有效期
            redisTemplate.opsForValue().set(phone, code, 5, TimeUnit.MINUTES);
            return Result.success("短信发送成功");
        }
        return Result.error("短信发送失败");
    }

    @PostMapping("login")
    public Result<User> login(@RequestBody Map<String, String> user, HttpSession session) {
        String phone = user.get("phone");
        String code = user.get("code");
        // 获取redis缓存中的值
        String cacheCode = redisTemplate.opsForValue().get(phone);
        log.info(cacheCode);
        if (cacheCode != null && cacheCode.equals(code)) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone, phone);
            User userDto = userService.getOne(queryWrapper);
            if (userDto == null) {
                userDto = new User();
                userDto.setPhone(phone);
                userDto.setStatus(1);
                userService.save(userDto);
            }
            // 删除redis缓存中的value
            redisTemplate.delete(phone);
            session.setAttribute("user", userDto.getId());
            return Result.success(userDto);
        }
        return Result.error("验证码错误");
    }
}
