package com.projects.hrs.services;

import com.projects.hrs.commons.CallType;
import com.projects.hrs.configuration.RabbitMqConfiguration;
import com.projects.hrs.dto.BillDto;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.entities.Balance;
import com.projects.hrs.entities.Limit;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.entities.TariffParameter;
import com.projects.hrs.repositories.AbonentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CallTarificationServiceTest {

    @Mock
    private AbonentRepository abonentRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CallTarificationService callTarificationService;

    @Test
    void getTotalCallMinutes_shouldCalculateRoundedUpMinutes() {
        int result = callTarificationService.getTotalCallMinutes("01:02:30");
        assertEquals(63, result);
    }

    @Test
    void subtractMinutesFromBalance_shouldSubtractAndSendBill_whenOutOfMinutes() {
        // Подготовка
        Abonent abonent = new Abonent();
        Balance balance = new Balance();
        // Выставляем количество минут для входящих звонков
        balance.setAmountOfMinutesForIncomingCall(3);
        abonent.setBalance(balance);

        Limit limit = new Limit();
        limit.setPricePerAdditionalMinuteIncoming(BigDecimal.valueOf(1.5));

        TariffParameter parameter = new TariffParameter();
        parameter.setLimit(limit);

        Tariff tariff = new Tariff();
        tariff.setTariffParameters(parameter);
        abonent.setTariff(tariff);
        abonent.setId(1L);

        when(abonentRepository.save(Mockito.any(Abonent.class))).thenReturn(abonent);

        // Вызов
        callTarificationService.subtractMinutesFromBalance(abonent, CallType.INCOMING.getCallType(), 5);
        assertEquals(0, abonent.getBalance().getAmountOfMinutesForIncomingCall());

        // Проверка: минуты должны обнулиться и отправлен счет за 3 минуты
        verify(abonentRepository).save(Mockito.any(Abonent.class));
        verify(rabbitTemplate).convertAndSend(
                Mockito.eq(RabbitMqConfiguration.EXCHANGE_NAME),
                Mockito.eq(RabbitMqConfiguration.BILL_CREATED_ROUTING_KEY),
                Mockito.any(BillDto.class)
        );
    }

    @Test
    void subtractMinutesFromBalance_shouldJustSubtract_whenEnoughIncomingMinutes() {
        // Подготовка
        Abonent abonent = new Abonent();
        Balance balance = new Balance();
        // Достаточно входящих минут
        balance.setAmountOfMinutesForIncomingCall(10);
        abonent.setBalance(balance);

        Limit limit = new Limit();
        limit.setPricePerAdditionalMinuteIncoming(BigDecimal.valueOf(1.5));

        TariffParameter parameter = new TariffParameter();
        parameter.setLimit(limit);

        Tariff tariff = new Tariff();
        tariff.setTariffParameters(parameter);
        abonent.setTariff(tariff);
        abonent.setId(1L);

        when(abonentRepository.save(Mockito.any(Abonent.class))).thenReturn(abonent);

        // Вызов: используем меньше минут, чем есть
        callTarificationService.subtractMinutesFromBalance(abonent, CallType.INCOMING.getCallType(), 5);

        // Проверка: минуты должны уменьшиться, счет не отправляется
        assertEquals(5, abonent.getBalance().getAmountOfMinutesForIncomingCall());

        verify(abonentRepository).save(Mockito.any(Abonent.class));
        verifyNoInteractions(rabbitTemplate);
    }

}

