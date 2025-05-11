package com.projects.hrs.services;

import com.projects.hrs.entities.Balance;
import com.projects.hrs.entities.Limit;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.entities.TariffParameter;
import com.projects.hrs.repositories.BalanceRepository;
import com.projects.hrs.repositories.TariffRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class BalanceService {

    private static final Logger log = LoggerFactory.getLogger(BalanceService.class);
    private final BalanceRepository balanceRepository;
    private final TariffRepository tariffRepository;

    public BalanceService(BalanceRepository balanceRepository, TariffRepository tariffRepository) {
        this.balanceRepository = balanceRepository;
        this.tariffRepository = tariffRepository;
    }

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
