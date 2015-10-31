package com.naturalprogrammer.spring.lemondemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.naturalprogrammer.spring.lemon.LemonConfig;

@SpringBootApplication(scanBasePackageClasses = {LemonDemoApplication.class, LemonConfig.class})
public class LemonDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemonDemoApplication.class, args);
    }
}
