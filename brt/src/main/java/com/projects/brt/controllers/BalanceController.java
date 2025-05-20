package com.projects.brt.controllers;

import com.projects.brt.controllers.requests.TopUpBalanceRequest;
import com.projects.brt.controllers.responses.TopUpBalanceResponse;
import com.projects.brt.services.BalanceService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/top-up")
    public ResponseEntity<TopUpBalanceResponse> topUp(@Valid @RequestBody TopUpBalanceRequest topUpBalanceRequest){
        return balanceService.topUp(topUpBalanceRequest);
    }
}
