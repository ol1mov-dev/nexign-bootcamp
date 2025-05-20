package com.projects.cdr.service;

import com.projects.cdr.commons.CallType;
import com.projects.cdr.configurations.RabbitMqConfiguration;
import com.projects.cdr.dto.CdrDto;
import com.projects.cdr.mapper.CdrMapper;
import com.projects.cdr.repositories.CdrRepository;
import com.projects.cdr.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class CdrGeneratorService {
    private final UserRepository userRepository;
    private final ThreadPoolTaskExecutor cdrTaskExecutor;
    private final CdrRepository cdrRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CdrMapper cdrMapper;
    private final RabbitMqConfiguration rabbitMqConfiguration;
    private final Random random = new Random();

    // Количество CDR, которые должны сгенерироваться перед отправкой в брокер сообщений
    @Value(value = "${cdr.max-cdr-amount-before-sending-to-queue}")
    private int maxCdrAmountBeforeSendingToQueue;

    // Количество CDR, которые должны сгенерироваться за раз при вызове метода generate()
    @Value(value = "${cdr.amount-of-cdr-generations-at-one-time}")
    private int amountOfCdrGenerationsAtOneTime;

    public CdrGeneratorService(
            UserRepository userRepository,
            ThreadPoolTaskExecutor cdrTaskExecutor,
            CdrRepository cdrRepository,
            RabbitTemplate rabbitTemplate,
            CdrMapper cdrMapper,
            RabbitMqConfiguration rabbitMqConfiguration
    ){
        this.userRepository = userRepository;
        this.cdrTaskExecutor = cdrTaskExecutor;
        this.cdrRepository = cdrRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.cdrMapper = cdrMapper;
        this.rabbitMqConfiguration = rabbitMqConfiguration;
    }

    /**
     * Создаем CDR
     */
    public void generate() throws InterruptedException {
        List<CdrDto> cdrs = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now().minusYears(1);

        for (int i = 0; i < amountOfCdrGenerationsAtOneTime; i++) {
            dateTime = dateTime.plusHours(3);

            LocalDateTime startTime = dateTime
                    .plusHours(random.nextInt(24));

            LocalDateTime endTime = startTime
                    .plusMinutes(random.nextInt(60))
                    .plusSeconds(random.nextInt(60));

            cdrTaskExecutor.submit(() -> {
                // Если даты одинаковые → звонок не пересекает полночь.
                // Если даты разные → звонок переходит на следующий день.
                if (!startTime.toLocalDate().equals(endTime.toLocalDate())) {
                    Map<String, CdrDto> splittedIntervals = splitCallIntervalAtMidnight(startTime, endTime);

                    cdrRepository.save(cdrMapper.toCdrEntity(splittedIntervals.get("firstCdr")));
                    cdrRepository.save(cdrMapper.toCdrEntity(splittedIntervals.get("secondCdr")));

                    cdrs.add(splittedIntervals.get("firstCdr"));
                    cdrs.add(splittedIntervals.get("secondCdr"));
                } else {
                    cdrs.add(buildCdrDto(startTime, endTime));
                    cdrRepository.save(
                        cdrMapper.toCdrEntity(buildCdrDto(startTime, endTime))
                    );
                }
                log.info("Generated CDR: {}", cdrs.get(0).toString());

                if (cdrs.size() == maxCdrAmountBeforeSendingToQueue){
                    sendCdrsQueue(cdrs);
                    cdrs.clear();
                }
            });
        }
    }

    /**
     * Получить тип звонка
     * @return тип звонка
     */
    public String getRandomCallType(){
        return random.nextBoolean() ?
                CallType.INCOMING.callType:
                CallType.OUTGOING.callType;
    }

    /**
     * Получаем случайного получателя. Это может быть наш клиент, либо клиент других абонентов
     * @return номер абонента к которому осуществляется/принимается звонок.
     */
    public String getRandomReciever(){
        boolean isOurSubscriber = random.nextBoolean();

        if(isOurSubscriber){
            return userRepository.findRandomUser().getNumber();
        }
        return  "+7" + random.nextLong(9000000000L, 9999999999L);
    }

    /**
     * Отправляем CDR-ы в очередь RabbitMQ
     * @param cdrs звонки которые генерируются комутатором
     */
    public void sendCdrsQueue(List<CdrDto> cdrs) {
        rabbitTemplate.convertAndSend(
                rabbitMqConfiguration.EXCHANGE_NAME,
                rabbitMqConfiguration.CDR_CREATED_ROUTING_KEY,
                cdrs
        );
    }

    /**
     * Делим CDR на 2 CDR, если время звонка пересекается с 00:00:00
     * Например звонок произошел в 23:55:00, а закончился в 00:03:00. В таком случае делим этот звонок
     * на 2 CDR:
     * 23:55:00 - 23:59:59 и 00:00:00 - 00:03:00
     * @param startCall начало звонка
     * @param endCall конец звонка
     * @return два Cdr, если есть пересечение в 00:00:00, в противном случае, только один CDR
     */
    public Map<String, CdrDto> splitCallIntervalAtMidnight(LocalDateTime startCall, LocalDateTime endCall){
        LocalDateTime midnight = startCall.toLocalDate().atTime(23, 59, 59);
        LocalDateTime nextDayStart = endCall.toLocalDate().atTime(0, 0, 0);

        return Map.of(
                "firstCdr", buildCdrDto(startCall, midnight),
                "secondCdr", buildCdrDto(nextDayStart, endCall)
        );
    }

    /**
     * Создаем Cdr
     * @param startTime начало разговора
     * @param endTime конец разговора
     * @return CdrDto
     */
    public CdrDto buildCdrDto(
            LocalDateTime startTime,
            LocalDateTime endTime
    ){
        return CdrDto
                .builder()
                .callType(getRandomCallType())
                .firstMsisdn(userRepository.findRandomUser().getNumber())
                .secondMsisdn(getRandomReciever())
                .startTime(startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .endTime(endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
}
