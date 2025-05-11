package com.projects.hrs.service;

import com.projects.hrs.controller.requests.CreateBalanceRequest;
import com.projects.hrs.entities.Balance;
import com.projects.hrs.entities.Limit;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.entities.TariffParameter;
import com.projects.hrs.repositories.BalanceRepository;
import com.projects.hrs.repositories.TariffRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private static final Logger log = LoggerFactory.getLogger(BalanceService.class);
    @Autowired
    private final BalanceRepository balanceRepository;
    @Autowired
    private TariffRepository tariffRepository;

    public Balance create(Long tariffId){
        Optional<Tariff> tariff = tariffRepository.findById(tariffId);
        TariffParameter tariffParameter = tariff.get().getTariffParameters();
        Limit tariffLimits = tariffParameter.getLimit();

        return balanceRepository.save(
                Balance
                        .builder()
                        .amountOfMinutesForOutcomingCall(tariffLimits.getMinutesForOutcome())
                        .amountOfMinutesForIncomingCall(tariffLimits.getMinutesForIncome())
                        .build()
        );
    }
}
