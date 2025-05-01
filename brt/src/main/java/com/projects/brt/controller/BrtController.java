package com.projects.brt.controller;

import com.projects.brt.dto.CdrDto;
import com.projects.brt.service.CallService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@AllArgsConstructor
public class BrtController {

    private final CallService brtService;

    @GetMapping("/test")
    public String test() {
        System.out.println(123);
        return "test";
    }

    @RabbitListener(queues = "cdr.queue")
    public void brt(List<CdrDto> cdrs) {
        brtService.saveCall(cdrs);
    }
}
