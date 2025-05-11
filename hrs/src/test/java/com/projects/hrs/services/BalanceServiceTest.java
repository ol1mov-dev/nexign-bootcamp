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
        // Подготовка данных
        Long tariffId = 1L;

        // Мокаем лимиты тарифа
        Limit limit = new Limit();
        limit.setMinutesForIncome(150);
        limit.setMinutesForOutcome(300);

        TariffParameter tariffParameter = new TariffParameter();
        tariffParameter.setLimit(limit);

        Tariff tariff = new Tariff();
        tariff.setTariffParameters(tariffParameter);

        Balance expectedBalance = Balance.builder()
                .amountOfMinutesForIncomingCall(150)
                .amountOfMinutesForOutcomingCall(300)
                .build();

        // Стаббинги
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));
        when(balanceRepository.save(Mockito.any(Balance.class))).thenReturn(expectedBalance);

        // Вызов
        Balance result = balanceService.create(tariffId);

        // Проверка
        assertEquals(expectedBalance.getAmountOfMinutesForIncomingCall(), result.getAmountOfMinutesForIncomingCall());
        assertEquals(expectedBalance.getAmountOfMinutesForOutcomingCall(), result.getAmountOfMinutesForOutcomingCall());

        verify(tariffRepository).findById(tariffId);
        verify(balanceRepository).save(Mockito.any(Balance.class));
    }
}

