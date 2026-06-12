package com.qlvt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class QlvtApplication {
    public static void main(String[] args) {
        SpringApplication.run(QlvtApplication.class, args);
    }
}
