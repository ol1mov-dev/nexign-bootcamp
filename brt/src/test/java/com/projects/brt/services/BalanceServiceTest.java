package com.projects.brt.services;

import com.projects.brt.dto.BillDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.repositories.AbonentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private AbonentRepository abonentRepository;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    @DisplayName("Списание суммы со счета абонента")
    void payBill_shouldSubtractBalanceAndSaveAbonent() {
        // Arrange: задаем ID абонента, начальный баланс и сумму счета
        Long abonentId = 1L;
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal totalPrice = new BigDecimal("30.00");

        // Создаем объект абонента с начальным балансом
        Abonent abonent = Abonent.builder()
                .id(abonentId)
                .balance(initialBalance)
                .build();

        // Создаем объект счета с суммой, которую нужно списать
        BillDto billDto = new BillDto(abonentId, totalPrice);

        // Мокаем поведение репозитория: абонент найден
        when(abonentRepository.findById(abonentId)).thenReturn(Optional.of(abonent));

        // Act: вызываем метод списания средств
        balanceService.payBill(billDto);

        // Capture: перехватываем сохраненного абонента
        ArgumentCaptor<Abonent> abonentCaptor = ArgumentCaptor.forClass(Abonent.class);
        verify(abonentRepository).save(abonentCaptor.capture());

        // Assert: проверяем, что баланс был уменьшен на сумму счета
        Abonent savedAbonent = abonentCaptor.getValue();
        assertEquals(new BigDecimal("70.00"), savedAbonent.getBalance());
    }

    @Test
    @DisplayName("Бросается исключение, если абонент не найден")
    void payBill_shouldThrowExceptionWhenAbonentNotFound() {
        // Arrange: создаем ID несуществующего абонента и объект счета
        Long abonentId = 99L;
        BillDto billDto = new BillDto(abonentId, new BigDecimal("20.00"));

        // Мокаем поведение репозитория: абонент не найден
        when(abonentRepository.findById(abonentId)).thenReturn(Optional.empty());

        // Act & Assert: ожидаем, что при оплате будет выброшено исключение EntityNotFoundException
        assertThrows(EntityNotFoundException.class, () -> balanceService.payBill(billDto));
    }
}

