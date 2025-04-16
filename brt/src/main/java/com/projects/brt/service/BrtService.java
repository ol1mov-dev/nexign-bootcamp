package com.projects.brt.service;

import com.projects.brt.entities.Cdr;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BrtService {
    @RabbitListener(queues = "cdr.queue")
    public void aaaaa(List<Cdr> cdrs) {
        cdrs.forEach(cdr -> {
            System.out.println(cdr.id() + " " + cdr.msisdn1() + " " + cdr.msisdn2());
        });
    }
}
