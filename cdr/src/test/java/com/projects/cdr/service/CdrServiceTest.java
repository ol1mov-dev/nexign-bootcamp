package com.projects.cdr.service;

import com.projects.cdr.commons.CallType;
import com.projects.cdr.configuration.RabbitMqConfiguration;
import com.projects.cdr.dto.CdrDto;
import com.projects.cdr.entities.User;
import com.projects.cdr.mapper.CdrMapper;
import com.projects.cdr.repository.CdrRepository;
import com.projects.cdr.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
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
public class CdrServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ThreadPoolTaskExecutor cdrTaskExecutor;

    @Mock
    private CdrRepository cdrRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private CdrMapper cdrMapper;

    @InjectMocks
    private CdrService cdrService;

    @Captor
    private ArgumentCaptor<Runnable> runnableCaptor;

    @Captor
    private ArgumentCaptor<List<CdrDto>> cdrListCaptor;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setNumber("+79001234567");
    }

    @Test
    void testGenerate() throws InterruptedException {
        // Given
        when(userRepository.findRandomUser()).thenReturn(testUser);
        when(cdrTaskExecutor.submit(any(Runnable.class))).thenAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        });

        // When
        cdrService.generate();

        // Then
        verify(cdrTaskExecutor, times(11)).submit(any(Runnable.class));
    }

    @Test
    void testGetRandomCallType() {
        // When
        String callType = cdrService.getRandomCallType();

        // Then
        assertTrue(callType.equals(CallType.INCOMING.callType) || callType.equals(CallType.OUTGOING.callType));
    }

    @Test
    void testGetRandomRecieverWithOurSubscriber() {
        // Given
        when(userRepository.findRandomUser()).thenReturn(testUser);

        // When
        String receiver = cdrService.getRandomReciever();

        // Then
        assertNotNull(receiver);
    }

    @Test
    void testSendCdrsQueue() {
        // Given
        List<CdrDto> cdrs = List.of(
                CdrDto.builder()
                        .callType(CallType.INCOMING.callType)
                        .firstMsisdn("+79001234567")
                        .secondMsisdn("+79001234568")
                        .startTime(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .endTime(LocalDateTime.now().plusMinutes(5).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                        .build()
        );

        // When
        cdrService.sendCdrsQueue(cdrs);

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMqConfiguration.EXCHANGE_NAME),
                eq(RabbitMqConfiguration.CDR_CREATED_ROUTING_KEY),
                eq(cdrs)
        );
    }

    @Test
    void splitCallIntervalAtMidnight_shouldSplitCallCrossingMidnight() {
        // Given
        LocalDateTime startCall = LocalDateTime.of(2025, 5, 10, 23, 50);
        LocalDateTime endCall = LocalDateTime.of(2025, 5, 11, 0, 10);

        User mockUser = new User();
        mockUser.setNumber("79001234567");
        when(userRepository.findRandomUser()).thenReturn(mockUser);

        // When
        Map<String, CdrDto> result = cdrService.splitCallIntervalAtMidnight(startCall, endCall);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("firstCdr"));
        assertTrue(result.containsKey("secondCdr"));

        CdrDto first = result.get("firstCdr");
        CdrDto second = result.get("secondCdr");

        assertEquals("2025-05-10T23:50:00", first.startTime());
        assertEquals("2025-05-10T23:59:59", first.endTime());

        assertEquals("2025-05-11T00:00:00", second.startTime());
        assertEquals("2025-05-11T00:10:00", second.endTime());
    }

    @Test
    void testBuildCdrDto() {
        // Given
        when(userRepository.findRandomUser()).thenReturn(testUser);
        LocalDateTime startTime = LocalDateTime.of(2023, 1, 1, 12, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(2023, 1, 1, 12, 5, 30);

        // When
        CdrDto result = cdrService.buildCdrDto(startTime, endTime);

        // Then
        assertNotNull(result);
        assertEquals(startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), result.startTime());
        assertEquals(endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), result.endTime());
        assertEquals(testUser.getNumber(), result.firstMsisdn());
        assertNotNull(result.secondMsisdn());
        assertNotNull(result.callType());
    }
}