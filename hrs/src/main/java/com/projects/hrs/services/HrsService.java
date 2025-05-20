package com.projects.hrs.services;

import com.projects.hrs.commons.PaymentPeriod;
import com.projects.hrs.dto.CallQueueDto;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.repositories.AbonentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class HrsService {
    private final AbonentRepository abonentRepository;
    private final CallTarificationService callTarificationService;

    public HrsService(
            AbonentRepository abonentRepository,
            CallTarificationService callTarificationService
    ){
        this.abonentRepository = abonentRepository;
        this.callTarificationService = callTarificationService;
    }

    public void calculate(CallQueueDto callDto) {
        int usedMinutes = callTarificationService
                                .getTotalCallMinutes(callDto.callDuration());

        Abonent abonent = abonentRepository
                                .findById(callDto.abonentId())
                                .orElseThrow(() -> new EntityNotFoundException("Такого абонента не существует"));

        int paymentPeriodInDays = abonent
                                    .getTariff()
                                    .getTariffParameters()
                                    .getPaymentPeriodInDays();

        log.info("[ {}, {} ]", usedMinutes, paymentPeriodInDays);

        // Если период оплаты равен PAY_FOR_SINGLE_CALL т.е. 0, то берем плату за каждый звонок
        if (paymentPeriodInDays == PaymentPeriod.PAY_FOR_SINGLE_CALL.getValue()){
             callTarificationService.calculateTotalPriceForCall(usedMinutes, abonent, callDto.callType());
        } else {
            callTarificationService.payMonthlyPayment(abonent.getId(), paymentPeriodInDays);
            callTarificationService.subtractMinutesFromBalance(abonent, callDto.callType(), usedMinutes);

        }
    }
}
