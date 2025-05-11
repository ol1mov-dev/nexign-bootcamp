package com.projects.hrs.services;

import com.projects.hrs.controllers.requests.CreateAbonentHrsRequest;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.entities.Balance;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.repositories.AbonentRepository;
import com.projects.hrs.repositories.TariffRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class AbonentService {

    private final AbonentRepository abonentRepository;
    private final BalanceService balanceService;
    private final TariffRepository tariffRepository;

    public AbonentService(AbonentRepository abonentRepository, BalanceService balanceService, TariffRepository tariffRepository) {
        this.abonentRepository = abonentRepository;
        this.balanceService = balanceService;
        this.tariffRepository = tariffRepository;
    }


    public ResponseEntity<String> create(CreateAbonentHrsRequest createAbonentRequest) {
        Balance balance = balanceService.create(createAbonentRequest.tariffId());
        Tariff tariff = tariffRepository.findById(createAbonentRequest.tariffId()).get();
        abonentRepository.save(
                Abonent
                        .builder()
                        .userId(createAbonentRequest.userId())
                        .balance(balance)
                        .tariff(tariff)
                        .createdAt(LocalDateTime.now())
                        .build()
        );
        return ResponseEntity.ok(tariff.getName());
    }
}
