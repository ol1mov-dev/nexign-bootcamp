package com.projects.brt.controller;

import com.projects.brt.dto.Cdr;
import com.projects.brt.service.BrtService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class BrtController {

    private final BrtService brtService;

    @RabbitListener(queues = "cdr.queue")
    public ResponseEntity<String> brt(List<Cdr> cdrs) {
        return ResponseEntity.ok(brtService.saveCdrs(cdrs));
    }
}
