package com.projects.brt;

import com.projects.brt.dto.CallDto;
import com.projects.brt.entities.User;
import com.projects.brt.mappers.CallMapper;
import com.projects.brt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class BrtApplication {
    private static final Logger log = LoggerFactory.getLogger(BrtApplication.class);
    private final UserRepository userRepository;

    public static void main(String[] args) {
        SpringApplication.run(BrtApplication.class, args);
    }
}
