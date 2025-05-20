package com.projects.hrs.services;

import com.projects.hrs.entities.Balance;
import com.projects.hrs.entities.Limit;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.entities.TariffParameter;
import com.projects.hrs.repositories.BalanceRepository;
import com.projects.hrs.repositories.TariffRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private BalanceRepository balanceRepository;

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    @DisplayName("Создание баланса: должен использовать лимиты тарифа и сохранить баланс")
    void create_shouldUseTariffLimitsAndSaveBalance() {
        // Подготовка идентификатора тарифа
        Long tariffId = 1L;

        // Мокаем лимиты тарифа (например, 150 минут входящих и 300 исходящих)
        Limit limit = new Limit();
        limit.setMinutesForIncome(150);
        limit.setMinutesForOutcome(300);

        // Оборачиваем лимиты в тарифные параметры
        TariffParameter tariffParameter = new TariffParameter();
        tariffParameter.setLimit(limit);

        // Создаем тариф с параметрами
        Tariff tariff = new Tariff();
        tariff.setTariffParameters(tariffParameter);

        // Ожидаемый объект баланса, который должен быть сохранён
        Balance expectedBalance = Balance.builder()
                .amountOfMinutesForIncomingCall(150)
                .amountOfMinutesForOutcomingCall(300)
                .build();

        // Стаббинги: находим тариф и сохраняем баланс
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));
        when(balanceRepository.save(Mockito.any(Balance.class))).thenReturn(expectedBalance);

        // Вызов метода создания баланса по тарифу
        Balance result = balanceService.create(tariffId);

        // Проверка, что минуты из лимита корректно проставлены в баланс
        assertEquals(expectedBalance.getAmountOfMinutesForIncomingCall(), result.getAmountOfMinutesForIncomingCall());
        assertEquals(expectedBalance.getAmountOfMinutesForOutcomingCall(), result.getAmountOfMinutesForOutcomingCall());

        // Проверка, что были вызваны нужные методы репозиториев
        verify(tariffRepository).findById(tariffId);
        verify(balanceRepository).save(Mockito.any(Balance.class));
    }
}

