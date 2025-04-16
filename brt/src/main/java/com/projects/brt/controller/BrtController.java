package com.projects.brt.controller;

import com.projects.brt.entities.Cdr;
import org.bouncycastle.util.test.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BrtController {
    public String brt(Test test) {
        System.out.println(test);
        return test.toString();
    }
}
