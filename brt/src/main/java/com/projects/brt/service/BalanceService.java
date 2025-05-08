package com.projects.brt.service;

import com.projects.brt.dto.BillDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.repositories.AbonentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AbonentRepository abonentRepository;

    @Transactional
    public void payBill(BillDto billDto){

        Abonent abonent = abonentRepository
                .findById(billDto.abonentId())
                .orElse(null);

        if(abonent != null){
            BigDecimal currentBalance = abonent
                                            .getBalance()
                                            .subtract(billDto.totalPrice());

            abonent.setBalance(currentBalance);
            abonentRepository.save(abonent);
            log.info("ID: " + billDto.abonentId() + " currentBalance: " + currentBalance);
        }
    }
}
