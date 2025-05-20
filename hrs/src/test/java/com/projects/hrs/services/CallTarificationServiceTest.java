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
import org.junit.jupiter.api.DisplayName;
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

    @Mock
    private RabbitMqConfiguration rabbitMqConfiguration;

    @InjectMocks
    private CallTarificationService callTarificationService;

    @Test
    @DisplayName("Длительность звонка округляется вверх до целых минут")
    void getTotalCallMinutes_shouldCalculateRoundedUpMinutes() {
        int result = callTarificationService.getTotalCallMinutes("01:02:30"); // 1 час 2 мин 30 сек = 63 минуты
        assertEquals(63, result);
    }

    @Test
    @DisplayName("Списание минут: при нехватке списываются все и создаётся счёт на превышение")
    void subtractMinutesFromBalance_shouldSubtractAndSendBill_whenOutOfMinutes() {
        // Подготовка абонента с 3 доступными входящими минутами
        Abonent abonent = new Abonent();
        Balance balance = new Balance();
        balance.setAmountOfMinutesForIncomingCall(3);
        abonent.setBalance(balance);

        // Тариф с ценой за дополнительные минуты
        Limit limit = new Limit();
        limit.setPricePerAdditionalMinuteIncoming(BigDecimal.valueOf(1.5));

        TariffParameter parameter = new TariffParameter();
        parameter.setLimit(limit);

        Tariff tariff = new Tariff();
        tariff.setTariffParameters(parameter);
        abonent.setTariff(tariff);
        abonent.setId(1L);

        // Мокаем сохранение
        when(abonentRepository.save(Mockito.any(Abonent.class))).thenReturn(abonent);

        // Вызов метода: запрашивается 5 минут, доступно только 3
        callTarificationService.subtractMinutesFromBalance(abonent, CallType.INCOMING.getCallType(), 5);

        // Оставшиеся минуты должны обнулиться
        assertEquals(0, abonent.getBalance().getAmountOfMinutesForIncomingCall());

        // Проверка вызова сохранения абонента и отправки счёта через RabbitMQ
        verify(abonentRepository).save(Mockito.any(Abonent.class));
        verify(rabbitTemplate).convertAndSend(
                Mockito.eq(rabbitMqConfiguration.EXCHANGE_NAME),
                Mockito.eq(rabbitMqConfiguration.BILL_CREATED_ROUTING_KEY),
                Mockito.any(BillDto.class)
        );
    }

    @Test
    @DisplayName("Списание минут: при достаточном остатке просто уменьшается количество минут")
    void subtractMinutesFromBalance_shouldJustSubtract_whenEnoughIncomingMinutes() {
        // Подготовка абонента с 10 входящими минутами
        Abonent abonent = new Abonent();
        Balance balance = new Balance();
        balance.setAmountOfMinutesForIncomingCall(10);
        abonent.setBalance(balance);

        // Настройка тарифа
        Limit limit = new Limit();
        limit.setPricePerAdditionalMinuteIncoming(BigDecimal.valueOf(1.5));

        TariffParameter parameter = new TariffParameter();
        parameter.setLimit(limit);

        Tariff tariff = new Tariff();
        tariff.setTariffParameters(parameter);
        abonent.setTariff(tariff);
        abonent.setId(1L);

        // Мокаем сохранение
        when(abonentRepository.save(Mockito.any(Abonent.class))).thenReturn(abonent);

        // Вызов метода: используем 5 из 10 минут
        callTarificationService.subtractMinutesFromBalance(abonent, CallType.INCOMING.getCallType(), 5);

        // Проверка: осталось 5 минут
        assertEquals(5, abonent.getBalance().getAmountOfMinutesForIncomingCall());

        // Проверка, что счёт не отправлялся
        verify(abonentRepository).save(Mockito.any(Abonent.class));
        verifyNoInteractions(rabbitTemplate);
    }
}

