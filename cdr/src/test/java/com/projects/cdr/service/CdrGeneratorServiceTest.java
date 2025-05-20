package com.projects.cdr.service;

import com.projects.cdr.commons.CallType;
import com.projects.cdr.configurations.RabbitMqConfiguration;
import com.projects.cdr.dto.CdrDto;
import com.projects.cdr.entities.User;
import com.projects.cdr.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CdrGeneratorServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RabbitMqConfiguration rabbitMqConfiguration;

    @Mock
    private ThreadPoolTaskExecutor cdrTaskExecutor;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CdrGeneratorService cdrGeneratorService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setNumber("+79001234567");
    }

    @Test
    @DisplayName("Проверка генерации CDR: должно запускаться 11 потоков")
    void testGenerate() throws InterruptedException {
        // Выполняем тестируемый метод
        cdrGeneratorService.generate();

        // Проверяем, что submit был вызван 11 раз
        verify(cdrTaskExecutor, times(11)).submit(any(Runnable.class));
    }

    @Test
    @DisplayName("Случайный тип звонка должен быть входящим или исходящим")
    void testGetRandomCallType() {
        // Получаем случайный тип звонка
        String callType = cdrGeneratorService.getRandomCallType();

        // Проверяем равно ли одному из 2-х значений
        assertTrue(callType.equals(CallType.INCOMING.callType) || callType.equals(CallType.OUTGOING.callType));
    }

    @Test
    @DisplayName("Случайный абонент должен быть возвращён")
    void testGetRandomRecieverWithOurSubscriber() {
        // When
        when(userRepository.findRandomUser()).thenReturn(testUser);

        // Получаем случайного пользователя к которому идет звонок
        String receiver = cdrGeneratorService.getRandomReciever();

        // Проверяем не пустой ли наш пользователь системы
        assertNotNull(receiver);
    }

    @Test
    @DisplayName("CDR-дто должно быть отправлено в очередь RabbitMQ")
    void testSendCdrsQueue() {
        // Список с cdr
        List<CdrDto> cdrs = List.of(
                CdrDto.builder()
                        .callType(CallType.INCOMING.callType)
                        .firstMsisdn("+79001234567")
                        .secondMsisdn("+79001234568")
                        .startTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .endTime(LocalDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build()
        );

        // Отправляем в очередь все наши CDR
        cdrGeneratorService.sendCdrsQueue(cdrs);

        // Проверяем отправились ли сгенерированные CDR в брокер сообщений
        verify(rabbitTemplate).convertAndSend(
                eq(rabbitMqConfiguration.EXCHANGE_NAME),
                eq(rabbitMqConfiguration.CDR_CREATED_ROUTING_KEY),
                eq(cdrs)
        );
    }

    @Test
    @DisplayName("Разделение звонка, пересекающего полночь, должно возвращать два CDR")
    void splitCallIntervalAtMidnight_shouldSplitCallCrossingMidnight() {
        // Тестовые значения
        LocalDateTime startCall = LocalDateTime.of(2025, 5, 10, 23, 50);
        LocalDateTime endCall = LocalDateTime.of(2025, 5, 11, 0, 10);
        when(userRepository.findRandomUser()).thenReturn(testUser);

        // При вызове метода splitCallIntervalAtMidnight должны быть возвращены 2 CDR.
        // Один до полуночи, другой после полуночи
        Map<String, CdrDto> result = cdrGeneratorService.splitCallIntervalAtMidnight(startCall, endCall);

        // Проверяем не пустой ли результат метода splitCallIntervalAtMidnight()
        // и существуют ли две CDR
        assertNotNull(result);
        assertTrue(result.containsKey("firstCdr"));
        assertTrue(result.containsKey("secondCdr"));

        CdrDto first = result.get("firstCdr");
        CdrDto second = result.get("secondCdr");

        // Проверяем правильно ли разделен интервал времени
        assertEquals("2025-05-10T23:50:00", first.startTime());
        assertEquals("2025-05-10T23:59:59", first.endTime());
        assertEquals("2025-05-11T00:00:00", second.startTime());
        assertEquals("2025-05-11T00:10:00", second.endTime());
    }

    @Test
    @DisplayName("Создание CDR-дто по времени и пользователю")
    void testBuildCdrDto() {
        // Получаем случайного пользователя и задаем интервал времени
        when(userRepository.findRandomUser()).thenReturn(testUser);
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 1, 1, 12, 5, 30);

        // Получаем наш CdrDto
        CdrDto result = cdrGeneratorService.buildCdrDto(startTime, endTime);

        // Проверяем не пустой ли результат метода buildCdrDto и правильно ли собран CdrDto
        assertNotNull(result);
        assertEquals(startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), result.startTime());
        assertEquals(endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), result.endTime());
        assertEquals(testUser.getNumber(), result.firstMsisdn());
        assertNotNull(result.secondMsisdn());
        assertNotNull(result.callType());
    }
}
