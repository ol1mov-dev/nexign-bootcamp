package com.projects.hrs.services;

import com.projects.hrs.commons.CallType;
import com.projects.hrs.configuration.RabbitMqConfiguration;
import com.projects.hrs.dto.BillDto;
import com.projects.hrs.entities.Abonent;
import com.projects.hrs.entities.Limit;
import com.projects.hrs.repositories.AbonentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
public class CallTarificationService {
    private final AbonentRepository abonentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final RabbitMqConfiguration rabbitMqConfiguration;

    public CallTarificationService(
            AbonentRepository abonentRepository,
            RabbitTemplate rabbitTemplate,
            RabbitMqConfiguration rabbitMqConfiguration
    ){
        this.abonentRepository = abonentRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.rabbitMqConfiguration = rabbitMqConfiguration;
    }

    /**
     * Рассчитываем общее количество минут звонка
     * @param callDuration длительность звонка
     * @return общее количество минут звонка
     */
    public int getTotalCallMinutes(String callDuration){
        LocalTime time = LocalTime.parse(callDuration);
        int totalMinutes = time.getHour() * 60 + time.getMinute();

        if (time.getSecond() > 0) {
            totalMinutes += 1;
        }
        return totalMinutes;
    }

    /**
     * Выставляем счет за 1 звонок
     * @param totalMinutes общее количество минут
     * @param abonent абонент
     * @param callType тип звонка (исходящий/входящий)
     */
    public void calculateTotalPriceForCall(int totalMinutes, Abonent abonent, String callType){
        Limit tariffLimits = abonent.getTariff().getTariffParameters().getLimit();

        BigDecimal totalPrice = callType.equals(CallType.INCOMING.getCallType()) ?
                BigDecimal.valueOf(totalMinutes).multiply(tariffLimits.getPricePerAdditionalMinuteIncoming()):
                BigDecimal.valueOf(totalMinutes).multiply(tariffLimits.getPricePerAdditionalMinuteOutcoming());

        log.info("Total call price: {}; Total minutes: {}; Call Type: {}",
                totalPrice,
                totalMinutes,
                callType
        );
        sendBillQueue(abonent.getId(), totalPrice);
    }

    public void subtractMinutesFromBalance(Abonent abonent, String callType, int usedMinutes) {
        int balance = callType.equals(CallType.INCOMING.getCallType()) ?
                abonent.getBalance().getAmountOfMinutesForIncomingCall() :
                abonent.getBalance().getAmountOfMinutesForOutcomingCall();

        balance -= usedMinutes;

        if (balance < 0) {
            if (callType.equals(CallType.INCOMING.getCallType())) {

                abonent.getBalance().setAmountOfMinutesForIncomingCall(0);
                abonentRepository.save(abonent);
                calculateTotalPriceForCall(Math.abs(balance),abonent, callType);
            } else {
                abonent.getBalance().setAmountOfMinutesForOutcomingCall(0);
                abonentRepository.save(abonent);
                calculateTotalPriceForCall(Math.abs(balance), abonent, callType);
            }
        } else {
            if (callType.equals(CallType.INCOMING.getCallType())) {
                abonent.getBalance().setAmountOfMinutesForIncomingCall(balance);
                log.info("{}", abonent.getBalance().getAmountOfMinutesForIncomingCall());
                abonentRepository.save(abonent);
            } else {
                abonent.getBalance().setAmountOfMinutesForOutcomingCall(balance);
                log.info("{}", abonent.getBalance().getAmountOfMinutesForOutcomingCall());
                abonentRepository.save(abonent);
            }
        }
    }

    public void payMonthlyPayment(Long abonentId, int paymentPeriodInDays) {
        Abonent abonent = abonentRepository.findById(abonentId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));

        LocalDateTime payDay = abonent.getNextPayDay();

        if (payDay != null && Duration.between(payDay, LocalDateTime.now()).toDays() >= 30) {
            abonent.setNextPayDay(payDay.plusDays(paymentPeriodInDays));
            abonentRepository.save(abonent);
            sendBillQueue(abonent.getId(), abonent.getTariff().getTariffParameters().getPrice());
        }
    }

    public void sendBillQueue(Long abonentId, BigDecimal totalPrice){
        rabbitTemplate.convertAndSend(
                rabbitMqConfiguration.EXCHANGE_NAME,
                rabbitMqConfiguration.BILL_CREATED_ROUTING_KEY,
                BillDto.builder().abonentId(abonentId).totalPrice(totalPrice).build()
        );
    }
}
