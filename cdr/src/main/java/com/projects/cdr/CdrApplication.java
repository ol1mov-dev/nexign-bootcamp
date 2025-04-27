package com.projects.cdr;

import com.projects.cdr.service.CdrService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
@RequiredArgsConstructor
public class CdrApplication {
    public static void main(String[] args) {
        SpringApplication.run(CdrApplication.class, args);
    }
}
