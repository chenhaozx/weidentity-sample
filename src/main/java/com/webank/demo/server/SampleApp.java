package com.webank.demo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * 启动入口
 * 
 * @author v_wbgyang
 *
 */
@SpringBootApplication
@ComponentScan({"com.webank.demo", "com.webank.weid.service"})
public class SampleApp {

    public static void main(String[] args) {
        SpringApplication.run(SampleApp.class, args);
    }
}
