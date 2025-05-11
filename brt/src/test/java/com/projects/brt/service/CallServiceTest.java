package com.projects.brt.service;

import com.projects.brt.configuration.RabbitMqConfiguration;
import com.projects.brt.dto.CallQueueDto;
import com.projects.brt.dto.CdrDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.entities.Call;
import com.projects.brt.repositories.AbonentRepository;
import com.projects.brt.repositories.CallRepository;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CallServiceTest {

    private static final Logger log = LoggerFactory.getLogger(CallServiceTest.class);
    @Mock
    private AbonentRepository abonentRepository;

    @Mock
    private CallRepository callRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CallService callService;

    @Captor
    private ArgumentCaptor<Call> callCaptor;

    @Captor
    private ArgumentCaptor<CallQueueDto> queueDtoCaptor;

    private Abonent abonent1;
    private Abonent abonent2;
    private CdrDto cdrDto;

    // Константы для тестов
    private final String FIRST_MSISDN = "+79262345678";
    private final String SECOND_MSISDN = "+79031234567";
    private final String START_TIME = "2023-05-10T14:30:00";
    private final String END_TIME = "2023-05-10T14:45:30";
    private final String CALL_TYPE = "01";

    @BeforeEach
    void setUp() {
        // Инициализация абонентов
        abonent1 = new Abonent();
        abonent1.setId(1L);
        abonent1.setMsisdn(FIRST_MSISDN);

        abonent2 = new Abonent();
        abonent2.setId(2L);
        abonent2.setMsisdn(SECOND_MSISDN);

        // Инициализация DTO звонка
        cdrDto = CdrDto
                .builder()
                .firstMsisdn(FIRST_MSISDN)
                .secondMsisdn(SECOND_MSISDN)
                .callType(CALL_TYPE)
                .startTime(START_TIME)
                .endTime(END_TIME)
                .build();
    }

    @Test
    @DisplayName("Сохранение звонка, когда оба абонента являются клиентами")
    void saveCall_whenBothNumbersAreClients_shouldSaveTwoCalls() {
        // Подготовка
        // Используем anyString() вместо конкретных значений для устранения ошибки Strict stubbing argument mismatch
        when(abonentRepository.existsByMsisdn(abonent1.getMsisdn())).thenReturn(Boolean.TRUE);
        when(abonentRepository.existsByMsisdn(abonent2.getMsisdn())).thenReturn(Boolean.TRUE);

        when(abonentRepository.findByMsisdn(abonent1.getMsisdn())).thenReturn(Optional.of(abonent1));
        when(abonentRepository.findByMsisdn(abonent2.getMsisdn())).thenReturn(Optional.of(abonent2));

        assertEquals(Boolean.TRUE, abonentRepository.existsByMsisdn(abonent1.getMsisdn()));
        assertEquals(Boolean.TRUE, abonentRepository.existsByMsisdn(abonent2.getMsisdn()));

        List<CdrDto> cdrs = new ArrayList<>();
        cdrs.add(cdrDto);

        callService.saveCall(cdrs);

        ArgumentCaptor<Call> callCaptor = ArgumentCaptor.forClass(Call.class);
        verify(callRepository, times(2)).save(callCaptor.capture());

        List<Call> savedCalls = callCaptor.getAllValues();
        assertEquals(2, savedCalls.size());

        // Проверка отправки сообщений в очередь
        ArgumentCaptor<CallQueueDto> queueCaptor = ArgumentCaptor.forClass(CallQueueDto.class);

        verify(rabbitTemplate, times(2)).convertAndSend(
                eq(RabbitMqConfiguration.EXCHANGE_NAME),
                eq(RabbitMqConfiguration.CALL_CREATED_ROUTING_KEY),
                queueCaptor.capture()
        );

        List<CallQueueDto> queueMessages = queueCaptor.getAllValues();
        assertEquals(2, queueMessages.size());

        // Примеры проверок
        assertEquals(abonent1.getId(), queueMessages.get(0).abonentId());
        assertEquals("01", queueMessages.get(0).callType());
        assertEquals("00:15:30", queueMessages.get(0).callDuration());
    }
    @Test
    @DisplayName("Сохранение звонка, когда первый абонент - клиент")
    void saveCall_whenOnlyFirstNumberIsClient_shouldSaveOneCall() {
        // Подготовка
        when(abonentRepository.existsByMsisdn(abonent1.getMsisdn())).thenReturn(true);
        when(abonentRepository.existsByMsisdn(abonent2.getMsisdn())).thenReturn(false);
        when(abonentRepository.findByMsisdn(abonent1.getMsisdn())).thenReturn(Optional.of(abonent1));

        List<CdrDto> cdrs = List.of(cdrDto);

        // Действие
        callService.saveCall(cdrs);

        // Проверка: должен быть сохранён только один Call
        ArgumentCaptor<Call> callCaptor = ArgumentCaptor.forClass(Call.class);
        verify(callRepository, times(1)).save(callCaptor.capture());

        Call savedCall = callCaptor.getValue();
        assertEquals(abonent1.getMsisdn(), savedCall.getAbonent().getMsisdn());
        assertEquals(abonent2.getMsisdn(), savedCall.getStrangerMsisdn());

        // Проверка: только одно сообщение отправлено в очередь
        ArgumentCaptor<CallQueueDto> queueCaptor = ArgumentCaptor.forClass(CallQueueDto.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMqConfiguration.EXCHANGE_NAME),
                eq(RabbitMqConfiguration.CALL_CREATED_ROUTING_KEY),
                queueCaptor.capture()
        );

        CallQueueDto queueMessage = queueCaptor.getValue();
        assertEquals(abonent1.getId(), queueMessage.abonentId());
        assertEquals(cdrDto.callType(), queueMessage.callType());
        assertEquals("00:15:30", queueMessage.callDuration());
    }

    @Test
    @DisplayName("Сохранение звонка, когда второй абонент - клиент")
    void saveCall_whenOnlySecondNumberIsClient_shouldSaveOneCall() {
        // Подготовка: только второй абонент — наш клиент
        when(abonentRepository.existsByMsisdn(abonent1.getMsisdn())).thenReturn(false);
        when(abonentRepository.existsByMsisdn(abonent2.getMsisdn())).thenReturn(true);
        when(abonentRepository.findByMsisdn(abonent2.getMsisdn())).thenReturn(Optional.of(abonent2));

        List<CdrDto> cdrs = List.of(cdrDto);

        // Действие
        callService.saveCall(cdrs);

        // Проверка: сохранён один звонок
        ArgumentCaptor<Call> callCaptor = ArgumentCaptor.forClass(Call.class);
        verify(callRepository, times(1)).save(callCaptor.capture());

        Call savedCall = callCaptor.getValue();
        assertEquals(abonent2.getMsisdn(), savedCall.getAbonent().getMsisdn());
        assertEquals(abonent1.getMsisdn(), savedCall.getStrangerMsisdn());

        // Проверка: одно сообщение отправлено в очередь
        ArgumentCaptor<CallQueueDto> queueCaptor = ArgumentCaptor.forClass(CallQueueDto.class);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMqConfiguration.EXCHANGE_NAME),
                eq(RabbitMqConfiguration.CALL_CREATED_ROUTING_KEY),
                queueCaptor.capture()
        );

        CallQueueDto queueMessage = queueCaptor.getValue();
        assertEquals(abonent2.getId(), queueMessage.abonentId());
        assertEquals(cdrDto.callType(), queueMessage.callType());
        assertEquals("00:15:30", queueMessage.callDuration());
    }

    @Test
    @DisplayName("Сохранение звонка, когда нет клиентов среди абонентов")
    void saveCall_whenNoClientsInvolved_shouldNotSave() {
        // Подготовка: оба абонента не являются клиентами
        when(abonentRepository.existsByMsisdn(abonent1.getMsisdn())).thenReturn(false);
        when(abonentRepository.existsByMsisdn(abonent2.getMsisdn())).thenReturn(false);

        List<CdrDto> cdrs = List.of(cdrDto);

        // Действие
        callService.saveCall(cdrs);

        // Проверка: звонки не сохраняются
        verify(callRepository, never()).save(any());

        // Проверка: сообщения не отправляются в очередь
        verify(rabbitTemplate, never()).convertAndSend( anyString(),
                anyString(),
                ArgumentMatchers.<CallQueueDto>any());
    }

    @Test
    @DisplayName("Сохранение нескольких звонков")
    void saveCall_multipleCallRecords_shouldProcessEach() {
        // Подготовка
        CdrDto cdrDto1 = new CdrDto("01", FIRST_MSISDN, SECOND_MSISDN, START_TIME, END_TIME);
        CdrDto cdrDto2 = new CdrDto("02", SECOND_MSISDN, FIRST_MSISDN, START_TIME, END_TIME);

        when(abonentRepository.existsByMsisdn(FIRST_MSISDN)).thenReturn(true);
        when(abonentRepository.existsByMsisdn(SECOND_MSISDN)).thenReturn(false);
        when(abonentRepository.findByMsisdn(FIRST_MSISDN)).thenReturn(Optional.of(abonent1));
        when(callRepository.save(any(Call.class))).thenReturn(new Call());

        // Вызов метода
        callService.saveCall(Arrays.asList(cdrDto1, cdrDto2));

        // Проверки - должно быть 2 вызова сохранения (по одному для каждого CdrDto)
        verify(callRepository, times(2)).save(any(Call.class));
    }

    @Test
    @DisplayName("Расчет длительности звонка - стандартный случай")
    void calculateCallDuration_standardCase_returnsCorrectDuration() {
        // Вызов метода
        LocalTime duration = callService.calculateCallDuration(START_TIME, END_TIME);

        // Проверка - разница между start и end должна быть 14 минут и 30 секунд
        assertEquals(0, duration.getHour());
        assertEquals(15, duration.getMinute());
        assertEquals(30, duration.getSecond());
    }

    @Test
    @DisplayName("Расчет длительности звонка с почасовой длительностью")
    void calculateCallDuration_multiHourCall_returnsCorrectDuration() {
        // Подготовка - звонок длительностью 2 часа 30 минут 15 секунд
        String startTime = "2023-05-10T12:30:00";
        String endTime = "2023-05-10T15:00:15";

        // Вызов метода
        LocalTime duration = callService.calculateCallDuration(startTime, endTime);

        // Проверка
        assertEquals(2, duration.getHour());
        assertEquals(30, duration.getMinute());
        assertEquals(15, duration.getSecond());
    }

    @Test
    @DisplayName("Расчет длительности звонка с миллисекундами")
    void calculateCallDuration_withMilliseconds_returnsCorrectDuration() {
        // Подготовка - звонок с миллисекундами в метках времени
        String startTime = "2023-05-10T14:30:45.123";
        String endTime = "2023-05-10T14:45:15.456";

        // Вызов метода
        LocalTime duration = callService.calculateCallDuration(startTime, endTime);

        // Проверка
        assertEquals(0, duration.getHour());
        assertEquals(14, duration.getMinute());
        assertEquals(30, duration.getSecond());
    }

    @Test
    @DisplayName("Проверка является ли номер нашим клиентом - положительный результат")
    void isOurClient_existingMsisdn_returnsTrue() {
        // Подготовка
        when(abonentRepository.existsByMsisdn(FIRST_MSISDN)).thenReturn(true);

        // Вызов и проверка
        assertTrue(callService.isOurClient(FIRST_MSISDN));

        // Проверка вызова метода репозитория
        verify(abonentRepository).existsByMsisdn(FIRST_MSISDN);
    }

    @Test
    @DisplayName("Проверка является ли номер нашим клиентом - отрицательный результат")
    void isOurClient_nonExistingMsisdn_returnsFalse() {
        // Подготовка
        when(abonentRepository.existsByMsisdn(FIRST_MSISDN)).thenReturn(false);

        // Вызов и проверка
        assertFalse(callService.isOurClient(FIRST_MSISDN));

        // Проверка вызова метода репозитория
        verify(abonentRepository).existsByMsisdn(FIRST_MSISDN);
    }

    @Test
    @DisplayName("Создание объекта звонка - успешный сценарий")
    void buildCall_validParams_returnsCorrectCallObject() {
        // Подготовка
        when(abonentRepository.findByMsisdn(FIRST_MSISDN)).thenReturn(Optional.of(abonent1));

        // Вызов метода
        Call call = callService.buildCall(cdrDto, FIRST_MSISDN, SECOND_MSISDN);

        // Проверки
        assertNotNull(call);
        assertEquals(abonent1, call.getAbonent());
        assertEquals(SECOND_MSISDN, call.getStrangerMsisdn());
        assertEquals(START_TIME, call.getStartTime());
        assertEquals(END_TIME, call.getEndTime());
        assertEquals(LocalTime.of(0, 15, 30), call.getDuration());
        assertEquals(CALL_TYPE, call.getCallType());

        // Проверка отправки сообщения в очередь
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfiguration.EXCHANGE_NAME),
                eq(RabbitMqConfiguration.CALL_CREATED_ROUTING_KEY),
                queueDtoCaptor.capture()
        );

        // Проверка содержимого отправленного сообщения
        CallQueueDto sentMessage = queueDtoCaptor.getValue();
        assertEquals(abonent1.getId(), sentMessage.abonentId());
        assertEquals(CALL_TYPE, sentMessage.callType());
        assertEquals("00:15:30", sentMessage.callDuration());
    }

    @Test
    @DisplayName("Создание объекта звонка - абонент не найден")
    void buildCall_abonentNotFound_throwsEntityNotFoundException() {
        // Подготовка
        when(abonentRepository.findByMsisdn(FIRST_MSISDN)).thenReturn(Optional.empty());

        // Вызов и проверка исключения
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> callService.buildCall(cdrDto, FIRST_MSISDN, SECOND_MSISDN)
        );

        // Проверка сообщения исключения
        assertTrue(exception.getMessage().contains("Абонент не найден: " + FIRST_MSISDN));

        // Проверка, что сообщения в очередь не отправлялось
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), Optional.ofNullable(any()));
    }

    @Test
    @DisplayName("Отправка сообщения в очередь - успешный сценарий")
    void sendCallQueue_validParams_sendsCorrectMessage() {
        // Подготовка
        Long abonentId = 1L;
        String callType = "INCOMING";
        String callDuration = "00:05:30";

        // Вызов метода
        callService.sendCallQueue(abonentId, callType, callDuration);

        // Проверка отправки сообщения в очередь
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfiguration.EXCHANGE_NAME),
                eq(RabbitMqConfiguration.CALL_CREATED_ROUTING_KEY),
                queueDtoCaptor.capture()
        );

        // Проверка содержимого отправленного сообщения
        CallQueueDto sentMessage = queueDtoCaptor.getValue();
        assertEquals(abonentId, sentMessage.abonentId());
        assertEquals(callType, sentMessage.callType());
        assertEquals(callDuration, sentMessage.callDuration());
    }
}