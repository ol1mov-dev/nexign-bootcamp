package com.projects.hrs.service;

import com.projects.hrs.controller.requests.CreateAbonentHrsRequest;
import com.projects.hrs.controller.requests.CreateAbonentRequest;
import com.projects.hrs.controller.responses.UserCreatedResponse;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.entities.Balance;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.repositories.AbonentRepository;
import com.projects.hrs.repositories.TariffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AbonentService {

    @Autowired
    private final AbonentRepository abonentRepository;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private TariffRepository tariffRepository;

    public ResponseEntity<String> create(CreateAbonentHrsRequest createAbonentRequest) {
        log.info(createAbonentRequest.tariffId() + " aaa ");
        Balance balance = balanceService.create(createAbonentRequest.tariffId());
        Tariff tariff = tariffRepository.findById(createAbonentRequest.tariffId()).get();
        abonentRepository.save(
                Abonent
                        .builder()
                        .userId(createAbonentRequest.userId())
                        .balance(balance)
                        .tariff(tariff)
                        .build()
        );
        return ResponseEntity.ok(tariff.getName());
    }
}
