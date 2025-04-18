package com.projects.cdr.service;

import com.projects.cdr.commons.CallType;
import com.projects.cdr.configuration.RabbitMqConfiguration;
import com.projects.cdr.dto.CdrDto;
import com.projects.cdr.entities.Cdr;
import com.projects.cdr.entities.User;
import com.projects.cdr.repository.CdrRepository;
import com.projects.cdr.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CdrService {

    private final CdrRepository repository;
    private final UserRepository userRepository;
    private final ThreadPoolTaskExecutor cdrTaskExecutor;
    private final CdrRepository cdrRepository;
    private final RabbitTemplate rabbitTemplate;

    @Value(value = "${cdr-reports.path}")
    private String REPORTS_DIR;

    public void sendCdrsQueue(List<Cdr> cdrs) {
        rabbitTemplate.convertAndSend(
                RabbitMqConfiguration.EXCHANGE_NAME,
                RabbitMqConfiguration.CDR_CREATED_ROUTING_KEY,
                cdrs
        );
    }

    /**
     * Создаем CDR
     * @throws InterruptedException
     */
    public void generate() throws InterruptedException {

        List<Cdr> cdrs = new ArrayList<>();
        LocalDateTime dateTime = LocalDateTime.now().minusYears(1);

        for (int i = 1; i <= 1000; i++) {
            dateTime = dateTime.plusDays(i);

            LocalDateTime startTime = dateTime.plusHours(4);
            LocalDateTime endTime = startTime.plusMinutes(i);

            cdrTaskExecutor.submit(() -> {
                User user = userRepository.findRandomUser();
                String callType = getRandomCallType();

                CdrDto cdr = CdrDto
                                .builder()
                                .callType(callType)
                                .msisdn1(user.getNumber())
                                .msisdn2(getRandomReciever())
                                .startTime(startTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .endTime(endTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                                .build();

                Cdr savedCdr = cdrRepository.save(
                        Cdr
                            .builder()
                            .callType(cdr.callType())
                            .msisdn1(cdr.msisdn1())
                            .msisdn2(cdr.msisdn2())
                            .startTime(cdr.startTime())
                            .endTime(cdr.endTime())
                            .build()
                );

                cdrs.add(savedCdr);

                if (cdrs.size() >= 10){
                    sendCdrsQueue(cdrs);
                    cdrs.clear();
                }
            });
        }
    }

    /**
     * Проверяем коллизию времени звонка
     * @return статус коллизии времени звонка (TRUE/FALSE)
     */
    public boolean isCallsTimeCollides(){
        return true;
    }

    /**
     * Получить тип звонка
     * @return тип звонка
     */
    public String getRandomCallType(){
        return new Random().nextBoolean() ?
                CallType.INCOMING.callType:
                CallType.OUTGOING.callType;
    }

    /**
     * Получаем рандомного получателя. Это может быть наш клиент, либо клиент других абонентов
     * @return номер абонента к которому осуществляется/принимается  звонок.
     */
    public String getRandomReciever(){
        boolean isOurSubscriber = new Random().nextBoolean();

        if(isOurSubscriber){
            return  userRepository.findRandomUser().getNumber();
        }
        return  "+7" + new Random().nextLong(9000000000L, 9999999999L);
    }

    public String splitIntervalAtMidnight(String startDateTime, String endDateTime){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDateTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endDateTime, formatter);

        if (!start.toLocalDate().equals(end.toLocalDate())) {
            // Создаем момент полуночи
            LocalDateTime midnight = start.toLocalDate().atTime(23, 59, 59);
            LocalDateTime nextDayStart = end.toLocalDate().atTime(0, 0, 0);

            System.out.println(start.format(formatter) + " - " + midnight.format(formatter));
            System.out.println(nextDayStart.format(formatter) + " - " + end.format(formatter));
        } else {
            System.out.println(start.format(formatter) + " - " + end.format(formatter));
        }
        return null;
    }

    public void generateFile(Cdr cdr) throws InterruptedException {
        File directory = new File(REPORTS_DIR);
        System.out.println(123);

        if (!directory.exists()) {
            directory.mkdirs();  // Создаем директорию, если она не существует
        }

        String fileName = UUID.randomUUID() + ".csv";
        File file = new File(directory, fileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(
                        String.format("%s,%s,%s,%s,%s\n",
                        cdr.getCallType(),
                        cdr.getMsisdn1(),
                        cdr.getMsisdn2(),
                        cdr.getStartTime(),
                        cdr.getEndTime())
                );
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
    }
}
