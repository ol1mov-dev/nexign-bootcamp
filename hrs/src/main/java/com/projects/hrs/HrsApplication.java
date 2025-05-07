package com.projects.hrs;

import com.projects.hrs.service.HrsService;
import lombok.AllArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HrsApplication {
    public static void main(String[] args) {
        SpringApplication.run(HrsApplication.class, args);
    }
}
