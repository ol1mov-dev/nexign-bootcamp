package com.projects.brt.controllers;

import com.projects.brt.dto.CdrDto;
import com.projects.brt.service.CallService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BrtController {

    private final CallService callService;

    @RabbitListener(queues = "cdr.queue")
    public void brt(List<CdrDto> cdrs) {
        callService.saveCall(cdrs);
    }
}
