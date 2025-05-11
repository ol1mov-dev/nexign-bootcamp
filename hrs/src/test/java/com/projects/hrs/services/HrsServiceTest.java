package com.projects.hrs.services;

import com.projects.hrs.commons.CallType;
import com.projects.hrs.dto.CallQueueDto;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.entities.Tariff;
import com.projects.hrs.entities.TariffParameter;
import com.projects.hrs.repositories.AbonentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HrsServiceTest {

    @Mock
    private AbonentRepository abonentRepository;

    @Mock
    private CallTarificationService callTarificationService;

    @InjectMocks
    private HrsService hrsService;

    @Test
    void calculate_shouldCallCalculateTotalPrice_whenPaymentPerCall() {
        // Подготовка
        CallQueueDto callDto = new CallQueueDto(1L, CallType.OUTGOING.getCallType(), "00:05:00");
        Abonent abonent = new Abonent();
        abonent.setId(1L);

        Tariff tariff = new Tariff();
        TariffParameter parameter = new TariffParameter();
        parameter.setPaymentPeriodInDays(0); // Оплата за каждый звонок
        tariff.setTariffParameters(parameter);
        abonent.setTariff(tariff);

        when(callTarificationService.getTotalCallMinutes("00:05:00")).thenReturn(5);
        when(abonentRepository.findById(1L)).thenReturn(Optional.of(abonent));

        // Вызов
        hrsService.calculate(callDto);

        // Проверка
        verify(callTarificationService).calculateTotalPriceForCall(5, abonent, CallType.OUTGOING.getCallType());
        verify(callTarificationService, never()).subtractMinutesFromBalance(any(), anyString(), anyInt());
    }

    @Test
    void calculate_shouldSubtractMinutes_whenPaymentByPeriod() {
        // Подготовка
        CallQueueDto callDto = new CallQueueDto(2L, "00:03:30", CallType.INCOMING.getCallType());
        Abonent abonent = new Abonent();
        abonent.setId(2L);

        Tariff tariff = new Tariff();
        TariffParameter parameter = new TariffParameter();
        parameter.setPaymentPeriodInDays(30); // Ежемесячная оплата
        tariff.setTariffParameters(parameter);
        abonent.setTariff(tariff);

        when(callTarificationService.getTotalCallMinutes("00:03:30")).thenReturn(4); // округлено вверх
        when(abonentRepository.findById(2L)).thenReturn(Optional.of(abonent));

        // Вызов
        hrsService.calculate(callDto);

        // Проверка
        verify(callTarificationService).subtractMinutesFromBalance(abonent, CallType.INCOMING.getCallType(), 4);
        verify(callTarificationService, never()).calculateTotalPriceForCall(anyInt(), any(), anyString());
    }

    @Test
    void calculate_shouldThrowException_whenAbonentNotFound() {
        // Подготовка
        CallQueueDto callDto = new CallQueueDto(999L, "00:01:00", CallType.INCOMING.getCallType());

        when(callTarificationService.getTotalCallMinutes("00:01:00")).thenReturn(1);
        when(abonentRepository.findById(999L)).thenReturn(Optional.empty());

        // Проверка
        assertThrows(EntityNotFoundException.class, () -> hrsService.calculate(callDto));
    }
}
