package com.projects.hrs.services;

import com.projects.hrs.controllers.requests.CreateAbonentHrsRequest;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.entities.Balance;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.repositories.AbonentRepository;
import com.projects.hrs.repositories.TariffRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbonentServiceTest {

    @Mock
    private AbonentRepository abonentRepository;

    @Mock
    private BalanceService balanceService;

    @Mock
    private TariffRepository tariffRepository;

    @InjectMocks
    private AbonentService abonentService;

    @Test
    @DisplayName("Создание абонента: должен вернуть имя тарифа")
    void createAbonent_shouldReturnTariffName() {
        // Подготовка данных
        Long userId = 1L;
        Long tariffId = 100L;
        String expectedTariffName = "Premium";

        CreateAbonentHrsRequest request = new CreateAbonentHrsRequest(userId, tariffId);
        Balance balance = new Balance(); // Подставь валидный объект, если надо
        Tariff tariff = new Tariff();
        tariff.setId(tariffId);
        tariff.setName(expectedTariffName);

        // Стаббинги
        when(balanceService.create(tariffId)).thenReturn(balance);
        when(tariffRepository.findById(tariffId)).thenReturn(Optional.of(tariff));

        // Вызов
        ResponseEntity<String> response = abonentService.create(request);

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedTariffName, response.getBody());

        verify(abonentRepository).save(Mockito.any(Abonent.class));
    }
}
