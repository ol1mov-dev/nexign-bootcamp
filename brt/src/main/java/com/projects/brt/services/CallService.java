package com.projects.brt.services;

import com.projects.brt.configuration.RabbitMqConfiguration;
import com.projects.brt.dto.CallQueueDto;
import com.projects.brt.dto.CdrDto;
import com.projects.brt.entities.Abonent;
import com.projects.brt.entities.Call;
import com.projects.brt.repositories.AbonentRepository;
import com.projects.brt.repositories.CallRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class CallService {
    private final AbonentRepository abonentRepository;
    private final CallRepository callRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqConfiguration rabbitMqConfiguration;

    public CallService(
            AbonentRepository abonentRepository,
            CallRepository callRepository,
            RabbitTemplate rabbitTemplate,
            RabbitMqConfiguration rabbitMqConfiguration
    ){
        this.abonentRepository = abonentRepository;
        this.callRepository = callRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMqConfiguration = rabbitMqConfiguration;
    }

    /**
     * Сохраняем
     * @param cdrs данные о звонках
     */
    @RabbitListener(queues = "${rabbitmq.cdrs-created-queue}")
    public void save(List<CdrDto> cdrs) {
        cdrs.forEach(cdr -> {
            boolean isFirstMsisdnOurClient = isOurClient(cdr.firstMsisdn());
            boolean isSecondMsisdnOurClient = isOurClient(cdr.secondMsisdn());

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

    /**
     * Рассчитать длительность разговора в формате hh:mm:ss
     * @param start начало разговора
     * @param end конец разговора
     * @return длительность разговора в формате hh:mm:ss
     */
    public LocalTime calculateCallDuration(String start, String end) {
        LocalDateTime startTime = LocalDateTime.parse(start, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        LocalDateTime endTime = LocalDateTime.parse(end, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        Duration duration = Duration.between(startTime, endTime);

        return LocalTime.of(
                (int)duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart()
        );
    }

    /**
     * Проверяем наш клиент
     * @param msisdn номер клиент
     * @return возвращает статус, являтся ли номер нашим клиентом
     */
    public boolean isOurClient(String msisdn) {
        return abonentRepository.existsByMsisdn(msisdn);
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
        Abonent abonent = abonentRepository
                .findByMsisdn(userMsisdn)
                .orElseThrow(() -> new EntityNotFoundException("Абонент не найден: " + userMsisdn));


        LocalTime duration = calculateCallDuration(cdr.startTime(), cdr.endTime());

        sendCallQueue(
                abonent.getId(),
                cdr.callType(),
                duration.toString());

        Call call = Call
                .builder()
                .abonent(abonent)
                .strangerMsisdn(strangerMsisdn)
                .startTime(cdr.startTime())
                .endTime(cdr.endTime())
                .duration(duration)
                .callType(cdr.callType())
                .build();

        log.info("[ {}, {}, {}, {}, {} ]",
                abonent.getId(),
                call.getStrangerMsisdn(),
                call.getDuration(),
                call.getStartTime(),
                call.getEndTime()
        );

        return call;
    }

    /**
     * Отправляем данные о звонке в очередь
     * @param abonentId идентификатор пользователя
     * @param callDuration длительность звонка в секундах
     */
    public void sendCallQueue(Long abonentId, String callType, String callDuration) {
        rabbitTemplate.convertAndSend(
                rabbitMqConfiguration.EXCHANGE_NAME,
                rabbitMqConfiguration.CALL_CREATED_ROUTING_KEY,
                CallQueueDto
                        .builder()
                        .abonentId(abonentId)
                        .callType(callType)
                        .callDuration(callDuration)
                        .build()
        );
    }
}