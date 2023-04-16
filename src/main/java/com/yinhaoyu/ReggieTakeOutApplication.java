package com.yinhaoyu;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * SpringBoot启动类
 *
 * @author Vastness
 */
@Slf4j
@SpringBootApplication
/*
 在SpringBootApplication上使用@ServletComponentScan注解后，
 Servlet、Filter、Listener可以直接通过@WebServlet、@WebFilter、@WebListener注解自动注册，
 无需其他代码。
 */
@ServletComponentScan
@EnableTransactionManagement
@EnableCaching
public class ReggieTakeOutApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReggieTakeOutApplication.class, args);
        log.info("项目启动成功");
    }

}
