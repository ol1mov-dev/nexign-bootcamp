package com.projects.hrs.controllers;

import com.projects.hrs.dto.CallQueueDto;
import com.projects.hrs.services.HrsService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class HrsController {

    private final HrsService hrsService;

    @RabbitListener(queues = "call.queue")
    public void calculate(CallQueueDto callDto) {
        hrsService.calculate(callDto);
    }
}
