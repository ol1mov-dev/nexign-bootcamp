package com.projects.brt.service;

import com.projects.brt.dto.AbonentDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.repositories.AbonentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbonentServiceTest {

    @Mock
    private AbonentRepository abonentRepository;

    @InjectMocks
    private AbonentService abonentService;

    @Test
    @DisplayName("Создание абонента должно возвращать ID")
    void createAbonent_shouldReturnAbonentId() {
        // Подготовка данных
        AbonentDto abonentDto = AbonentDto
                .builder()
                .firstName("A")
                .lastName("B")
                .name("C")
                .msisdn("+79001234567")
                .balance(BigDecimal.valueOf(100))
                .build();

        Abonent savedAbonent = Abonent.builder()
                .id(1L)
                .firstName(abonentDto.firstName())
                .name(abonentDto.name())
                .lastName(abonentDto.lastName())
                .msisdn(abonentDto.msisdn())
                .balance(abonentDto.balance())
                .build();

        // Заглушка поведения репозитория
        when(abonentRepository.save(Mockito.any(Abonent.class))).thenReturn(savedAbonent);


        // Вызов метода
        ResponseEntity<Long> response = abonentService.create(abonentDto);

        // Проверка
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody());

        // Проверка что сохранение было вызвано
        ArgumentCaptor<Abonent> abonentCaptor = ArgumentCaptor.forClass(Abonent.class);
        verify(abonentRepository).save(abonentCaptor.capture());

        Abonent abonentSaved = abonentCaptor.getValue();
        assertEquals("+79001234567", abonentSaved.getMsisdn());
        assertEquals(BigDecimal.valueOf(100), abonentSaved.getBalance());
    }
}
