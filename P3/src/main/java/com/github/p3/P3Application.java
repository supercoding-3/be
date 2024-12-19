package com.github.p3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.github.p3.entity")
public class P3Application {

    public static void main(String[] args) {
        SpringApplication.run(P3Application.class, args);
    }

}
