package com.projects.hrs.service;

import com.projects.hrs.commons.PaymentPeriod;
import com.projects.hrs.configuration.RabbitMqConfiguration;
import com.projects.hrs.dto.BillDto;
import com.projects.hrs.dto.CallDto;
import com.projects.hrs.dto.CallQueueDto;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.repositories.AbonentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Slf4j
@Service
@AllArgsConstructor
public class HrsService {
    private final AbonentRepository abonentRepository;
    private final CallTarificationService callTarificationService;

    public void calculate(CallQueueDto callDto) {

        int usedMinutes = callTarificationService
                                .getTotalCallMinutes(callDto.callDuration());

        log.info("AB ID: " + callDto.abonentId());
        Abonent abonent = abonentRepository
                                .findById(callDto.abonentId())
                                .orElseThrow(() -> new EntityNotFoundException("AAA"));

        int paymentPeriodInDays = abonent
                                    .getTariff()
                                    .getTariffParameters()
                                    .getPaymentPeriodInDays();

        // Если период оплаты равен PAY_FOR_SINGLE_CALL т.е. 0, то берем плату за каждый звонок
        if (paymentPeriodInDays == PaymentPeriod.PAY_FOR_SINGLE_CALL.getValue()){
             callTarificationService.calculateTotalPriceForCall(usedMinutes, abonent, callDto.callType());
        } else {
            callTarificationService.subtractMinutesFromBalance(abonent, callDto.callType(), usedMinutes);
        }
    }
}
