package com.projects.brt.controllers;

import com.projects.brt.dto.BillDto;
import com.projects.brt.service.BalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
public class BalanceController {

    private final BalanceService balanceService;

    @RabbitListener(queues = "bill.queue")
    public void bill(BillDto billDto) {
        log.info("Bill Received: {}", billDto.abonentId()  + " " + billDto.totalPrice());
        balanceService.payBill(billDto);
    }

}
