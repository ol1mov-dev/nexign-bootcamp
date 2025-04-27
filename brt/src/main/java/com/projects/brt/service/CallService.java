package com.projects.brt.service;

import com.projects.brt.configuration.RabbitMqConfiguration;
import com.projects.brt.dto.CallDto;
import com.projects.brt.dto.CallQueueDto;
import com.projects.brt.dto.CdrDto;
import com.projects.brt.entities.Call;
import com.projects.brt.entities.User;
import com.projects.brt.mappers.CallMapper;
import com.projects.brt.repositories.CallRepository;
import com.projects.brt.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CallService {

    private final UserRepository userRepository;
    private final CallRepository callRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CallMapper callMapper;

    /**
     * Сохраняем
     * @param cdrs
     */
    public void saveCall(List<CdrDto> cdrs) {
        cdrs.forEach(cdr -> {
            boolean isFirstMsisdnOurClient = isOurClient(cdr.firstMsisdn());
            boolean isSecondMsisdnOurClient = isOurClient(cdr.secondMsisdn());

            log.info(cdr.toString());

            if (isFirstMsisdnOurClient && isSecondMsisdnOurClient) {
                callRepository.save(buildCall(cdr, cdr.firstMsisdn(), cdr.secondMsisdn()));
                callRepository.save(buildCall(cdr, cdr.secondMsisdn(), cdr.firstMsisdn()));
            } else if (isFirstMsisdnOurClient) {
                callRepository.save(buildCall(cdr, cdr.firstMsisdn(), cdr.secondMsisdn()));
            } else if (isSecondMsisdnOurClient) {
                callRepository.save(buildCall(cdr, cdr.secondMsisdn(), cdr.firstMsisdn()));
            } else {
                log.info("Ни один из номеров не является клиентом нашей компании.");
            }
        });
    }

    public Long calculateDurationInSeconds(String start, String end) {
        DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
                .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
                .toFormatter();

        return ChronoUnit.SECONDS.between(
                LocalDateTime.parse(start, formatter),
                LocalDateTime.parse(end, formatter)
        );
    }

    /**
     * Проверяем наш клиент
     *
     * @param msisdn номер клиент
     * @return возвращает статус, являтся ли номер нашим клиентом
     */
    public boolean isOurClient(String msisdn) {
        return userRepository.existsByMsisdn(msisdn);
    }

    /**
     * Собираем звонок
     *
     * @param cdr            данные которые отправляются комутатором
     * @param userMsisdn     номер проверяемого пользователя
     * @param strangerMsisdn номер другого абонента
     * @return сущность Call для последующего сохранения в таблицу Calls
     */
    public Call buildCall(
            CdrDto cdr,
            String userMsisdn,
            String strangerMsisdn
    ){
        User user = userRepository.findByMsisdn(userMsisdn);
        CallDto callDto = CallDto
                                .builder()
                                .user(user)
                                .strangerMsisdn(strangerMsisdn)
                                .startTime(cdr.startTime())
                                .endTime(cdr.endTime())
                                .duration(calculateDurationInSeconds(cdr.startTime(), cdr.endTime()))
                                .callType(cdr.callType())
                                .build();

        sendCallQueue(user.getId(), callDto.duration());
        return callMapper.toCallEntity(callDto);
    }

    public void sendCallQueue(Long userId, Long callDuration) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfiguration.EXCHANGE_NAME,
                RabbitMqConfiguration.CALL_CREATED_ROUTING_KEY,
                CallQueueDto
                        .builder()
                        .userId(userId)
                        .callDurationInSeconds(callDuration)
                        .build()
        );
    }


}