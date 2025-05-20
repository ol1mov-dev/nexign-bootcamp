package com.projects.brt.controllers;

import com.projects.brt.dto.BillDto;
import com.projects.brt.services.BalanceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/balance")
public class BalanceController {
    private final BalanceService balanceService;

    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    /**
     * Оплата счета, который приходит из HRS
     * @param billDto информация о счете которая приходит из HRS
     */
    @RabbitListener(queues = "${rabbitmq.bill-created-queue}")
    public void payBill(BillDto billDto) {
        log.info("Bill Received: {}", billDto.abonentId()  + " " + billDto.totalPrice());
        balanceService.payBill(billDto);
    }
}
