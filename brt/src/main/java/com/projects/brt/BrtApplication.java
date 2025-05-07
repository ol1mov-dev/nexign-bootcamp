package com.projects.brt;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
public class BrtApplication {
    public static void main(String[] args) {
        SpringApplication.run(BrtApplication.class, args);
    }
}
