package com.projects.nexignbootcamp;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.format.datetime.DateFormatter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
public class NexignBootcampApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexignBootcampApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {
            String et = "2023-11-06T00:00:00.000Z";
            String es = "2023-11-06T00:01:00.000Z";

            System.out.println(
                    ChronoUnit.MINUTES.between(et., es)
            );

        };
    }
}
