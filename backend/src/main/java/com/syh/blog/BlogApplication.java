package com.syh.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 博客系统启动类
 *
 * @author sunyahui
 * @since 2024-01-01
 */
@SpringBootApplication
@MapperScan("com.syh.blog.mapper")
public class BlogApplication {

    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
        System.out.println("========================================");
        System.out.println("博客系统启动成功！");
        System.out.println("访问地址：http://localhost:8088/api");
        System.out.println("接口文档：http://localhost:8088/api/doc.html");
        System.out.println("========================================");
    }
}
