package com.projects.brt.controller;

import com.projects.brt.dto.CdrDto;
import com.projects.brt.repositories.UserRepository;
import com.projects.brt.service.BrtService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class BrtController {

    private final UserRepository userRepository;
    private final BrtService brtService;

    @RabbitListener(queues = "cdr.queue")
    public void brt(List<CdrDto> cdrs) {
        brtService.saveCdrs(cdrs);
    }

    @GetMapping("/a")
    public void a() {
        brtService.tes();
    }
}
