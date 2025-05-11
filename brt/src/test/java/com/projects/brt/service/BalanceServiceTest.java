package com.projects.brt.service;

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
        Long abonentId = 1L;
        BigDecimal initialBalance = new BigDecimal("100.00");
        BigDecimal totalPrice = new BigDecimal("30.00");

        Abonent abonent = Abonent.builder()
                .id(abonentId)
                .balance(initialBalance)
                .build();

        BillDto billDto = new BillDto(abonentId, totalPrice);

        when(abonentRepository.findById(abonentId)).thenReturn(Optional.of(abonent));

        balanceService.payBill(billDto);

        ArgumentCaptor<Abonent> abonentCaptor = ArgumentCaptor.forClass(Abonent.class);
        verify(abonentRepository).save(abonentCaptor.capture());

        Abonent savedAbonent = abonentCaptor.getValue();
        assertEquals(new BigDecimal("70.00"), savedAbonent.getBalance());
    }

    @Test
    @DisplayName("Бросается исключение, если абонент не найден")
    void payBill_shouldThrowExceptionWhenAbonentNotFound() {
        Long abonentId = 99L;
        BillDto billDto = new BillDto(abonentId, new BigDecimal("20.00"));

        when(abonentRepository.findById(abonentId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> balanceService.payBill(billDto));
    }
}

