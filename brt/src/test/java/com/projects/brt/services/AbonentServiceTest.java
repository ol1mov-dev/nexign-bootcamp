package com.projects.brt.services;

import com.projects.brt.controllers.requests.CreateAbonentRequest;
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
        // Подготовка данных: создаем DTO абонента, который будет передан в сервис
        CreateAbonentRequest createAbonentRequest = CreateAbonentRequest
                .builder()
                .firstName("A")
                .lastName("B")
                .name("C")
                .msisdn("+79001234567")
                .balance(BigDecimal.valueOf(100))
                .build();

        // Создаем объект абонента, который должен вернуться из заглушки репозитория после сохранения
        Abonent savedAbonent = Abonent.builder()
                .id(1L) // Репозиторий вернет абонента с ID = 1
                .firstName(createAbonentRequest.firstName())
                .name(createAbonentRequest.name())
                .middleName(createAbonentRequest.lastName()) // Здесь предполагается, что lastName = отчество
                .msisdn(createAbonentRequest.msisdn())
                .balance(createAbonentRequest.balance())
                .build();

        // Заглушка поведения репозитория: возвращаем сохраненного абонента
        when(abonentRepository.save(Mockito.any(Abonent.class))).thenReturn(savedAbonent);

        // Вызов метода create() в сервисе
        ResponseEntity<Long> response = abonentService.create(createAbonentRequest);

        // Проверка ответа: статус должен быть 200 OK, тело — ID абонента
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1L, response.getBody());

        // Проверяем, что метод save() был вызван один раз и перехватываем аргумент
        ArgumentCaptor<Abonent> abonentCaptor = ArgumentCaptor.forClass(Abonent.class);
        verify(abonentRepository).save(abonentCaptor.capture());

        // Получаем абонента, переданного в save(), и проверяем его поля
        Abonent abonentSaved = abonentCaptor.getValue();
        assertEquals("+79001234567", abonentSaved.getMsisdn());
        assertEquals(BigDecimal.valueOf(100), abonentSaved.getBalance());
    }
}
